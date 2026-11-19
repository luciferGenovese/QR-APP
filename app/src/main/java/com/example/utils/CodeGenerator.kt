package com.example.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

enum class BodyShape { SQUARE, CIRCLE, ROUNDED, DIAMOND, CROSS, STAR, DOT, HEXAGON, HEART, TRIANGLE, PENTAGON, OCTAGON, EMOJI }

enum class LogoBadgeShape { ROUNDED_SQUARE, CIRCLE, SQUARE, NONE }

enum class ScanQualityLevel { EXCELLENT, GOOD, WARNING, POOR }

data class ScanQualityResult(
    val contrastRatio: Double,
    val level: ScanQualityLevel,
    val label: String,
    val recommendation: String = ""
)

object CodeGenerator {

    fun calculateContrastRatio(fgColor: Int, bgColor: Int): Double {
        if (bgColor == Color.TRANSPARENT) {
            // Assume white surface for contrast testing if transparent
            return calculateContrastRatio(fgColor, Color.WHITE)
        }
        val lum1 = calculateLuminance(fgColor)
        val lum2 = calculateLuminance(bgColor)
        val brightest = maxOf(lum1, lum2)
        val darkest = minOf(lum1, lum2)
        return (brightest + 0.05) / (darkest + 0.05)
    }

    private fun calculateLuminance(color: Int): Double {
        fun channelLum(channel: Int): Double {
            val s = channel / 255.0
            return if (s <= 0.03928) s / 12.92 else Math.pow((s + 0.055) / 1.055, 2.4)
        }
        val r = channelLum(Color.red(color))
        val g = channelLum(Color.green(color))
        val b = channelLum(Color.blue(color))
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    fun getScanQuality(fgColor: Int, bgColor: Int, hasLogo: Boolean, eccLevel: ErrorCorrectionLevel): ScanQualityResult {
        val contrast = calculateContrastRatio(fgColor, bgColor)
        val fgLum = calculateLuminance(fgColor)
        val bgLum = calculateLuminance(if (bgColor == Color.TRANSPARENT) Color.WHITE else bgColor)
        val isDarkOnLight = fgLum < bgLum

        return when {
            contrast >= 4.5 && (isDarkOnLight || contrast >= 7.0) -> {
                ScanQualityResult(
                    contrastRatio = contrast,
                    level = ScanQualityLevel.EXCELLENT,
                    label = "Lectura Óptima",
                    recommendation = if (hasLogo && eccLevel != ErrorCorrectionLevel.H) "Sugerencia: Usa nivel de corrección H para máxima fiabilidad con logo." else ""
                )
            }
            contrast >= 3.0 -> {
                ScanQualityResult(
                    contrastRatio = contrast,
                    level = ScanQualityLevel.GOOD,
                    label = "Lectura Buena",
                    recommendation = "Para cámaras de baja iluminación se recomienda contrastar más el fondo."
                )
            }
            contrast >= 2.0 -> {
                ScanQualityResult(
                    contrastRatio = contrast,
                    level = ScanQualityLevel.WARNING,
                    label = "Lectura Difícil",
                    recommendation = "El contraste es bajo. Algunas cámaras podrían tardar en enfocar el código."
                )
            }
            else -> {
                ScanQualityResult(
                    contrastRatio = contrast,
                    level = ScanQualityLevel.POOR,
                    label = "Bajo Contraste",
                    recommendation = "El código y el fondo tienen colores muy similares. Podría ser ilegible."
                )
            }
        }
    }

    fun generateBitmapOrThrow(
        content: String,
        format: BarcodeFormat,
        width: Int = 512,
        height: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE,
        bodyShape: BodyShape = BodyShape.SQUARE,
        logoBitmap: Bitmap? = null,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.H,
        logoSizeRatio: Float = 0.22f,
        logoBadgeShape: LogoBadgeShape = LogoBadgeShape.ROUNDED_SQUARE,
        customEmoji: String = "😀"
    ): Bitmap {
        if (content.isBlank()) throw IllegalArgumentException("Content cannot be blank")
        
        val isAnimQr = content.startsWith("ANIM_QR|")
        val hints = mutableMapOf<EncodeHintType, Any>()
        if (format == BarcodeFormat.QR_CODE) {
            hints[EncodeHintType.ERROR_CORRECTION] = if (isAnimQr) ErrorCorrectionLevel.M else errorCorrectionLevel
        }
        hints[EncodeHintType.MARGIN] = if (isAnimQr) 1 else 2

        val is1DBarcode = format in listOf(
            BarcodeFormat.EAN_8, BarcodeFormat.EAN_13, BarcodeFormat.UPC_E, 
            BarcodeFormat.UPC_A, BarcodeFormat.CODE_39, BarcodeFormat.CODE_93, 
            BarcodeFormat.CODE_128, BarcodeFormat.ITF, BarcodeFormat.CODABAR
        )
        val barcodeHeight = if (is1DBarcode) height - 60 else height

        val writer = MultiFormatWriter()
        val bitMatrix = writer.encode(content, format, width, barcodeHeight, hints)
        
        val w = bitMatrix.width
        val h = bitMatrix.height

        val bitmap = Bitmap.createBitmap(w, if (is1DBarcode) h + 60 else h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        canvas.drawColor(backgroundColor)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = foregroundColor

        val safeLogoRatio = logoSizeRatio.coerceIn(0.12f, 0.28f)

        if (format == BarcodeFormat.QR_CODE && !isAnimQr && (bodyShape != BodyShape.SQUARE || logoBitmap != null)) {
            // To draw shapes, we need the raw matrix size
            val rawMatrix = writer.encode(content, format, 0, 0, hints)
            val rawW = rawMatrix.width
            val rawH = rawMatrix.height

            val modWidth = w / rawW.toFloat()
            val modHeight = h / rawH.toFloat()

            val logoBoxSize = w * safeLogoRatio
            val logoRect = if (logoBitmap != null) {
                RectF(
                    (w - logoBoxSize) / 2f - 4f,
                    (h - logoBoxSize) / 2f - 4f,
                    (w + logoBoxSize) / 2f + 4f,
                    (h + logoBoxSize) / 2f + 4f
                )
            } else {
                null
            }

            for (y in 0 until rawH) {
                for (x in 0 until rawW) {
                    if (rawMatrix[x, y]) {
                        val left = x * modWidth
                        val top = y * modHeight
                        val right = left + modWidth
                        val bottom = top + modHeight

                        if (logoRect != null) {
                            val moduleRect = RectF(left, top, right, bottom)
                            if (RectF.intersects(logoRect, moduleRect)) {
                                continue
                            }
                        }

                        val isMarker = (x < 7 && y < 7) || (x > rawW - 8 && y < 7) || (x < 7 && y > rawH - 8)
                        
                        if (isMarker || bodyShape == BodyShape.SQUARE) {
                            canvas.drawRect(left, top, right, bottom, paint)
                        } else {
                            val scale = when (bodyShape) {
                                BodyShape.HEART -> 0.85f
                                BodyShape.STAR -> 0.85f
                                BodyShape.CROSS -> 0.9f
                                BodyShape.DIAMOND -> 0.85f
                                BodyShape.TRIANGLE -> 0.9f
                                BodyShape.PENTAGON -> 0.9f
                                BodyShape.OCTAGON -> 0.85f
                                BodyShape.HEXAGON -> 0.85f
                                else -> 0.75f
                            }
                            val cx = left + modWidth / 2f
                            val cy = top + modHeight / 2f
                            val vLeft = cx - (modWidth / 2f) * scale
                            val vTop = cy - (modHeight / 2f) * scale
                            val vRight = cx + (modWidth / 2f) * scale
                            val vBottom = cy + (modHeight / 2f) * scale
                            val vWidth = modWidth * scale
                            val vHeight = modHeight * scale

                            when (bodyShape) {
                                BodyShape.CIRCLE -> canvas.drawOval(vLeft, vTop, vRight, vBottom, paint)
                                BodyShape.ROUNDED -> canvas.drawRoundRect(vLeft, vTop, vRight, vBottom, vWidth / 3, vHeight / 3, paint)
                                BodyShape.DOT -> {
                                    val dInsetX = vWidth / 4
                                    val dInsetY = vHeight / 4
                                    canvas.drawOval(vLeft + dInsetX, vTop + dInsetY, vRight - dInsetX, vBottom - dInsetY, paint)
                                }
                                BodyShape.DIAMOND -> {
                                    val path = Path()
                                    path.moveTo(cx, vTop)
                                    path.lineTo(vRight, cy)
                                    path.lineTo(cx, vBottom)
                                    path.lineTo(vLeft, cy)
                                    path.close()
                                    canvas.drawPath(path, paint)
                                }
                                BodyShape.CROSS -> {
                                    val qW = vWidth / 4
                                    val qH = vHeight / 4
                                    val path = Path()
                                    path.moveTo(vLeft + qW, vTop)
                                    path.lineTo(vRight - qW, vTop)
                                    path.lineTo(vRight - qW, vTop + qH)
                                    path.lineTo(vRight, vTop + qH)
                                    path.lineTo(vRight, vBottom - qH)
                                    path.lineTo(vRight - qW, vBottom - qH)
                                    path.lineTo(vRight - qW, vBottom)
                                    path.lineTo(vLeft + qW, vBottom)
                                    path.lineTo(vLeft + qW, vBottom - qH)
                                    path.lineTo(vLeft, vBottom - qH)
                                    path.lineTo(vLeft, vTop + qH)
                                    path.lineTo(vLeft + qW, vTop + qH)
                                    path.close()
                                    canvas.drawPath(path, paint)
                                }
                                BodyShape.STAR -> {
                                    val outerRadius = Math.min(vWidth, vHeight) / 2f
                                    val innerRadius = outerRadius / 2.5f
                                    val path = Path()
                                    val points = 4
                                    for (i in 0 until points * 2) {
                                        val radius = if (i % 2 == 0) outerRadius else innerRadius
                                        val angle = (i * Math.PI / points) - Math.PI / 2
                                        val px = (cx + radius * Math.cos(angle)).toFloat()
                                        val py = (cy + radius * Math.sin(angle)).toFloat()
                                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                    }
                                    path.close()
                                    canvas.drawPath(path, paint)
                                }
                                BodyShape.HEXAGON -> {
                                    val radius = Math.min(vWidth, vHeight) / 2f
                                    val path = Path()
                                    for (i in 0 until 6) {
                                        val angle = (i * Math.PI / 3) - Math.PI / 2
                                        val px = (cx + radius * Math.cos(angle)).toFloat()
                                        val py = (cy + radius * Math.sin(angle)).toFloat()
                                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                    }
                                    path.close()
                                    canvas.drawPath(path, paint)
                                }
                                BodyShape.HEART -> {
                                    val path = Path()
                                    val mw = vWidth
                                    val mh = vHeight
                                    path.moveTo(cx, vTop + mh * 0.9f)
                                    path.cubicTo(vLeft - mw * 0.15f, vTop + mh * 0.3f, cx - mw * 0.15f, vTop - mh * 0.25f, cx, vTop + mh * 0.2f)
                                    path.cubicTo(cx + mw * 0.15f, vTop - mh * 0.25f, vRight + mw * 0.15f, vTop + mh * 0.3f, cx, vTop + mh * 0.9f)
                                    path.close()
                                    canvas.drawPath(path, paint)
                                }
                                BodyShape.TRIANGLE -> {
                                    val path = Path()
                                    path.moveTo(cx, vTop)
                                    path.lineTo(vRight, vBottom)
                                    path.lineTo(vLeft, vBottom)
                                    path.close()
                                    canvas.drawPath(path, paint)
                                }
                                BodyShape.PENTAGON -> {
                                    val radius = Math.min(vWidth, vHeight) / 2f
                                    val path = Path()
                                    for (i in 0 until 5) {
                                        val angle = (i * Math.PI * 2 / 5) - Math.PI / 2
                                        val px = (cx + radius * Math.cos(angle)).toFloat()
                                        val py = (cy + radius * Math.sin(angle)).toFloat()
                                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                    }
                                    path.close()
                                    canvas.drawPath(path, paint)
                                }
                                BodyShape.OCTAGON -> {
                                    val radius = Math.min(vWidth, vHeight) / 2f
                                    val path = Path()
                                    for (i in 0 until 8) {
                                        val angle = (i * Math.PI * 2 / 8) - Math.PI / 2 + Math.PI / 8
                                        val px = (cx + radius * Math.cos(angle)).toFloat()
                                        val py = (cy + radius * Math.sin(angle)).toFloat()
                                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                    }
                                    path.close()
                                    canvas.drawPath(path, paint)
                                }
                                BodyShape.EMOJI -> {
                                    val textPaint = android.graphics.Paint().apply {
                                        color = foregroundColor
                                        textSize = vHeight * 1.2f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                    val textBounds = android.graphics.Rect()
                                    textPaint.getTextBounds(customEmoji, 0, customEmoji.length, textBounds)
                                    canvas.drawText(customEmoji, cx, cy - textBounds.exactCenterY(), textPaint)
                                }
                                else -> canvas.drawRect(vLeft, vTop, vRight, vBottom, paint)
                            }
                        }
                    }
                }
            }
        } else {
            val pixels = IntArray(w * h)
            for (y in 0 until h) {
                val offset = y * w
                for (x in 0 until w) {
                    pixels[offset + x] = if (bitMatrix[x, y]) foregroundColor else backgroundColor
                }
            }
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
            
            if (is1DBarcode) {
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = foregroundColor
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                    typeface = android.graphics.Typeface.MONOSPACE
                }
                canvas.drawText(content, w / 2f, h + 45f, textPaint)
            }
        }

        if (logoBitmap != null && !logoBitmap.isRecycled && format == BarcodeFormat.QR_CODE) {
            val logoBoxSize = w * safeLogoRatio
            val left = (w - logoBoxSize) / 2f
            val top = (h - logoBoxSize) / 2f
            val right = left + logoBoxSize
            val bottom = top + logoBoxSize
            val cx = w / 2f
            val cy = h / 2f

            if (logoBadgeShape != LogoBadgeShape.NONE) {
                // 1. Draw clean contrasting badge background
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(45, 0, 0, 0)
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f
                }

                when (logoBadgeShape) {
                    LogoBadgeShape.CIRCLE -> {
                        val radius = logoBoxSize / 2f
                        canvas.drawCircle(cx, cy, radius, bgPaint)
                        canvas.drawCircle(cx, cy, radius, borderPaint)
                    }
                    LogoBadgeShape.SQUARE -> {
                        canvas.drawRect(left, top, right, bottom, bgPaint)
                        canvas.drawRect(left, top, right, bottom, borderPaint)
                    }
                    LogoBadgeShape.ROUNDED_SQUARE -> {
                        val cornerR = logoBoxSize * 0.22f
                        canvas.drawRoundRect(RectF(left, top, right, bottom), cornerR, cornerR, bgPaint)
                        canvas.drawRoundRect(RectF(left, top, right, bottom), cornerR, cornerR, borderPaint)
                    }
                    else -> {}
                }
            }

            // 3. Draw scaled logo inside with padding
            val pad = if (logoBadgeShape == LogoBadgeShape.NONE) 0f else logoBoxSize * 0.12f
            val innerLogoW = (logoBoxSize - pad * 2).toInt().coerceAtLeast(1)
            val innerLogoH = (logoBoxSize - pad * 2).toInt().coerceAtLeast(1)
            val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, innerLogoW, innerLogoH, true)
            canvas.drawBitmap(scaledLogo, left + pad, top + pad, null)
            if (scaledLogo !== logoBitmap && !scaledLogo.isRecycled) {
                scaledLogo.recycle()
            }
        }

        return bitmap
    }

    /**
     * Renders a centered protective watermark stamp over a QR code bitmap while
     * keeping the overlay compact enough to remain scannable with high ECC.
     */
    fun applyWatermarkOverlay(source: Bitmap, watermarkText: String): Bitmap {
        if (source.isRecycled) return source
        val cleanText = watermarkText.trim().ifBlank { "OFICIAL" }.take(24)
        val result = source.copy(Bitmap.Config.ARGB_8888, true) ?: return source
        val canvas = Canvas(result)
        val w = result.width.toFloat()
        val h = result.height.toFloat()

        // Diagonal subtle protective watermarks across the quiet middle band
        val subtlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(65, 15, 23, 42)
            textSize = (w * 0.065f).coerceIn(14f, 44f)
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        canvas.save()
        canvas.rotate(-22f, w / 2f, h / 2f)
        canvas.drawText(cleanText.uppercase(), w / 2f, h * 0.34f, subtlePaint)
        canvas.drawText(cleanText.uppercase(), w / 2f, h * 0.68f, subtlePaint)
        canvas.restore()

        // Crisp central seal badge sized inside the QR ECC recovery budget
        val badgeW = w * 0.42f
        val badgeH = (h * 0.12f).coerceIn(32f, 96f)
        val cx = w / 2f
        val cy = h / 2f
        val rect = RectF(cx - badgeW / 2f, cy - badgeH / 2f, cx + badgeW / 2f, cy + badgeH / 2f)

        val badgeBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(238, 255, 255, 255)
            style = Paint.Style.FILL
        }
        val badgeBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            style = Paint.Style.STROKE
            strokeWidth = (w * 0.006f).coerceAtLeast(2f)
        }
        val radius = badgeH * 0.28f
        canvas.drawRoundRect(rect, radius, radius, badgeBg)
        canvas.drawRoundRect(rect, radius, radius, badgeBorder)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = badgeH * 0.46f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        val maxTextW = badgeW * 0.86f
        val measured = labelPaint.measureText(cleanText.uppercase())
        if (measured > maxTextW && measured > 0f) {
            labelPaint.textSize = labelPaint.textSize * (maxTextW / measured)
        }
        val bounds = android.graphics.Rect()
        val label = cleanText.uppercase()
        labelPaint.getTextBounds(label, 0, label.length, bounds)
        canvas.drawText(label, cx, cy - bounds.exactCenterY(), labelPaint)

        return result
    }

    fun generateBitmap(
        content: String,
        format: BarcodeFormat,
        width: Int = 512,
        height: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE,
        bodyShape: BodyShape = BodyShape.SQUARE,
        logoBitmap: Bitmap? = null,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.H,
        logoSizeRatio: Float = 0.22f,
        logoBadgeShape: LogoBadgeShape = LogoBadgeShape.ROUNDED_SQUARE,
        customEmoji: String = "😀"
    ): Bitmap? {
        return try {
            generateBitmapOrThrow(
                content, format, width, height, foregroundColor, backgroundColor, bodyShape, logoBitmap,
                errorCorrectionLevel, logoSizeRatio, logoBadgeShape, customEmoji
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates a hybrid Scalable Vector Graphic (SVG) string.
     * Supports pure vector QR codes as well as embedded PNG/JPG custom logos and badges
     * using RFC 2397 Data URI (base64) lossless embedding.
     */
    fun generateSvg(
        content: String,
        format: BarcodeFormat = BarcodeFormat.QR_CODE,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE,
        bodyShape: BodyShape = BodyShape.SQUARE,
        ecc: ErrorCorrectionLevel = ErrorCorrectionLevel.H,
        logoBitmap: Bitmap? = null,
        logoSizeRatio: Float = 0.22f,
        logoBadgeShape: LogoBadgeShape = LogoBadgeShape.ROUNDED_SQUARE
    ): String {
        if (content.isBlank()) return ""
        val hints = mutableMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.ERROR_CORRECTION] = ecc
        hints[EncodeHintType.MARGIN] = 2

        val writer = MultiFormatWriter()
        val bitMatrix = writer.encode(content, format, 0, 0, hints)
        val w = bitMatrix.width
        val h = bitMatrix.height

        val fgHex = String.format("#%06X", (0xFFFFFF and foregroundColor))
        val bgHex = if (backgroundColor == Color.TRANSPARENT) "none" else String.format("#%06X", (0xFFFFFF and backgroundColor))

        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" viewBox=\"0 0 $w $h\" width=\"100%\" height=\"100%\">\n")
        if (bgHex != "none") {
            sb.append("  <rect width=\"$w\" height=\"$h\" fill=\"$bgHex\"/>\n")
        }
        sb.append("  <path fill=\"$fgHex\" d=\"")
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (bitMatrix[x, y]) {
                    sb.append("M${x},${y}h1v1h-1z ")
                }
            }
        }
        sb.append("\"/>\n")

        // Hybrid PNG / Logo Support: Embeds uploaded PNG / bitmap cleanly via base64
        if (logoBitmap != null && !logoBitmap.isRecycled) {
            val safeRatio = logoSizeRatio.coerceIn(0.12f, 0.32f)
            val boxSize = w * safeRatio
            val cx = w / 2f
            val cy = h / 2f
            val left = cx - (boxSize / 2f)
            val top = cy - (boxSize / 2f)

            // 1. Badge container to guarantee scanning contrast
            if (logoBadgeShape != LogoBadgeShape.NONE) {
                when (logoBadgeShape) {
                    LogoBadgeShape.CIRCLE -> {
                        val r = boxSize / 2f
                        sb.append("  <circle cx=\"$cx\" cy=\"$cy\" r=\"$r\" fill=\"#FFFFFF\" stroke=\"#CCCCCC\" stroke-width=\"0.5\"/>\n")
                    }
                    LogoBadgeShape.SQUARE -> {
                        sb.append("  <rect x=\"$left\" y=\"$top\" width=\"$boxSize\" height=\"$boxSize\" fill=\"#FFFFFF\" stroke=\"#CCCCCC\" stroke-width=\"0.5\"/>\n")
                    }
                    LogoBadgeShape.ROUNDED_SQUARE -> {
                        val cr = boxSize * 0.22f
                        sb.append("  <rect x=\"$left\" y=\"$top\" width=\"$boxSize\" height=\"$boxSize\" rx=\"$cr\" ry=\"$cr\" fill=\"#FFFFFF\" stroke=\"#CCCCCC\" stroke-width=\"0.5\"/>\n")
                    }
                    else -> {}
                }
            }

            // 2. Encode Bitmap to Base64 PNG
            val pad = if (logoBadgeShape == LogoBadgeShape.NONE) 0f else boxSize * 0.12f
            val imgLeft = left + pad
            val imgTop = top + pad
            val imgSize = boxSize - (pad * 2f)

            try {
                val byteStream = java.io.ByteArrayOutputStream()
                logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
                val base64Data = android.util.Base64.encodeToString(byteStream.toByteArray(), android.util.Base64.NO_WRAP)
                sb.append("  <image x=\"$imgLeft\" y=\"$imgTop\" width=\"$imgSize\" height=\"$imgSize\" preserveAspectRatio=\"xMidYMid meet\" href=\"data:image/png;base64,$base64Data\"/>\n")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        sb.append("</svg>")
        return sb.toString()
    }
}

