package com.example.utils

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.example.R
import com.google.zxing.BarcodeFormat

// --- PARAMETERIZED ARCHITECTURE ---

data class TemplateCanvas(val width: Float = 1024f, val height: Float = 1280f)

data class TemplateBackground(
    val type: String = "artwork", // "artwork", "solid", "vector", "bitmap"
    val color: String? = null
)

data class TemplateQrArea(
    val x: Float = 192f,
    val y: Float = 360f,
    val width: Float = 640f,
    val height: Float = 640f,
    val radius: Float = 28f,
    val background: String? = "#FFFFFF",
    val quietZone: Boolean = true
)

sealed class TemplateElement {
    abstract val id: String
    abstract val x: Float
    abstract val y: Float
    abstract val width: Float
    abstract val height: Float
}

data class TemplateTextElement(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    val text: String,
    val fontSize: Float = 48f,
    val align: Paint.Align = Paint.Align.CENTER,
    val color: String = "#000000",
    val isBold: Boolean = false
) : TemplateElement()

// --- TEMPLATE DEFINITIONS & CUSTOMIZATION ---

data class TemplateItemDef(
    val id: String,
    val name: String,
    val category: String,
    val vectorResId: Int,
    val primaryColor: Int,
    val cardColor: Int,

    val defaultText: String,
    val defaultSubtext: String = "Escanea con tu cámara",
    val defaultTextColor: Int = Color.BLACK,
    val defaultSubtextColor: Int = Color.DKGRAY,
    val isDarkTheme: Boolean = false,
    val defaultHeaderAccent: String? = null,
    val defaultFooterAccent: String? = null,

    val canvas: TemplateCanvas = TemplateCanvas(),
    val background: TemplateBackground = TemplateBackground(),
    val qrArea: TemplateQrArea = TemplateQrArea(),
    val elements: List<TemplateElement> = emptyList()
)

data class TemplateCustomization(
    val templateDef: TemplateItemDef,
    val qrContent: String = "https://google.com",
    val titleText: String = "ESCANÉAME",
    val headerAccentText: String? = null,
    val textSizeSp: Float = 24f,
    val textAlign: Paint.Align = Paint.Align.CENTER,
    val textColor: Int = Color.BLACK,
    val subtext: String = "Escanea con tu cámara",
    val subtextSizeSp: Float = 14f,
    val subtextColor: Int = Color.DKGRAY,
    val footerAccentText: String? = null,
    val logoBitmap: Bitmap? = null,
    val logoSizeMultiplier: Float = 0.22f,
    val textOffsetX: Float = 0f,
    val textOffsetY: Float = 0f,
    val qrForegroundColor: Int = Color.BLACK,
    val qrBackgroundColor: Int = Color.WHITE,
    val customFrameBitmap: Bitmap? = null,
    val isBold: Boolean = true,
    val isUppercase: Boolean = false,
    val showSubtext: Boolean = true,
    val qrBodyShape: BodyShape = BodyShape.SQUARE,
    val qrScale: Float = 1.0f,
    val qrOffsetY: Float = 0f,
    val watermarkText: String? = null
)

object TemplateRenderer {

    private fun standardElements(
        title: String,
        subtitle: String,
        headerY: Float = 115f,
        titleY: Float = 228f,
        subtextY: Float = 1135f,
        footerY: Float = 1215f
    ): List<TemplateElement> = listOf(
        TemplateTextElement("header", 512f, headerY, 780f, 50f, "", 24f),
        TemplateTextElement("title", 512f, titleY, 820f, 100f, title, 56f, isBold = true),
        TemplateTextElement("subtext", 512f, subtextY, 820f, 70f, subtitle, 28f),
        TemplateTextElement("footer", 512f, footerY, 780f, 40f, "", 22f)
    )

    val AVAILABLE_TEMPLATES = listOf(
        TemplateItemDef(
            id = "bento_studio",
            name = "Estudio Bento",
            category = "Negocios",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#0D9488"),
            cardColor = Color.parseColor("#F6F4EF"),
            defaultText = "CONECTA CONMIGO",
            defaultSubtext = "Guarda mi tarjeta digital",
            defaultHeaderAccent = "TARJETA DIGITAL",
            defaultTextColor = Color.parseColor("#18181B"),
            defaultSubtextColor = Color.parseColor("#52525B"),
            isDarkTheme = false,
            background = TemplateBackground(type = "artwork", color = "#F6F4EF"),
            qrArea = TemplateQrArea(x = 192f, y = 360f, width = 640f, height = 640f, radius = 24f),
            elements = standardElements("CONECTA CONMIGO", "Guarda mi tarjeta digital", headerY = 135f, titleY = 235f)
        ),
        TemplateItemDef(
            id = "executive_obsidian",
            name = "Obsidiana",
            category = "Negocios",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#EAB308"),
            cardColor = Color.parseColor("#090D16"),
            defaultText = "CONTACTO VIP",
            defaultSubtext = "Escanea para agendar reunión",
            defaultHeaderAccent = "EJECUTIVO",
            defaultTextColor = Color.parseColor("#F8FAFC"),
            defaultSubtextColor = Color.parseColor("#CBD5E1"),
            isDarkTheme = true,
            background = TemplateBackground(type = "artwork", color = "#090D16"),
            qrArea = TemplateQrArea(x = 196f, y = 355f, width = 632f, height = 632f, radius = 24f),
            elements = standardElements("CONTACTO VIP", "Escanea para agendar reunión", headerY = 128f, titleY = 232f, subtextY = 1118f, footerY = 1190f)
        ),
        TemplateItemDef(
            id = "express_checkout",
            name = "Caja Express",
            category = "Comercio",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#2563EB"),
            cardColor = Color.parseColor("#F8FAFC"),
            defaultText = "PAGA CON QR",
            defaultSubtext = "Rápido, seguro y sin contacto",
            defaultHeaderAccent = "CAJA RÁPIDA",
            defaultFooterAccent = "ACEPTAMOS PAGOS QR",
            defaultTextColor = Color.WHITE,
            defaultSubtextColor = Color.parseColor("#1E293B"),
            isDarkTheme = false,
            background = TemplateBackground(type = "artwork", color = "#F8FAFC"),
            qrArea = TemplateQrArea(x = 192f, y = 380f, width = 640f, height = 640f, radius = 28f),
            elements = standardElements("PAGA CON QR", "Rápido, seguro y sin contacto", headerY = 105f, titleY = 225f, subtextY = 1142f, footerY = 1222f)
        ),
        TemplateItemDef(
            id = "boutique_tag",
            name = "Etiqueta Boutique",
            category = "Comercio",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#C2410C"),
            cardColor = Color.parseColor("#F1EAE0"),
            defaultText = "CATÁLOGO",
            defaultSubtext = "Descubre la nueva colección",
            defaultFooterAccent = "EDICIÓN LIMITADA",
            defaultTextColor = Color.parseColor("#292524"),
            defaultSubtextColor = Color.parseColor("#78716C"),
            isDarkTheme = false,
            background = TemplateBackground(type = "artwork", color = "#F1EAE0"),
            qrArea = TemplateQrArea(x = 202f, y = 350f, width = 620f, height = 620f, radius = 20f),
            elements = standardElements("CATÁLOGO", "Descubre la nueva colección", headerY = 205f, titleY = 288f, subtextY = 1072f, footerY = 1148f)
        ),
        TemplateItemDef(
            id = "bistro_chalkboard",
            name = "Pizarra Bistró",
            category = "Gastronomía",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#D97706"),
            cardColor = Color.parseColor("#1B2420"),
            defaultText = "MENÚ DEL DÍA",
            defaultSubtext = "Escanea para ver nuestra carta",
            defaultHeaderAccent = "CAFÉ & BISTRÓ",
            defaultTextColor = Color.parseColor("#F5F2EB"),
            defaultSubtextColor = Color.parseColor("#D6D3CD"),
            isDarkTheme = true,
            background = TemplateBackground(type = "artwork", color = "#1B2420"),
            qrArea = TemplateQrArea(x = 202f, y = 365f, width = 620f, height = 620f, radius = 24f),
            elements = standardElements("MENÚ DEL DÍA", "Escanea para ver nuestra carta", headerY = 128f, titleY = 228f, subtextY = 1118f, footerY = 1188f)
        ),
        TemplateItemDef(
            id = "botanical_wifi",
            name = "Mesa Botánica",
            category = "Gastronomía",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#065F46"),
            cardColor = Color.parseColor("#ECFDF5"),
            defaultText = "WI-FI GRATIS",
            defaultSubtext = "Apunta tu cámara para conectarte",
            defaultHeaderAccent = "BIENVENIDO",
            defaultTextColor = Color.parseColor("#064E3B"),
            defaultSubtextColor = Color.parseColor("#047857"),
            isDarkTheme = false,
            background = TemplateBackground(type = "artwork", color = "#ECFDF5"),
            qrArea = TemplateQrArea(x = 192f, y = 360f, width = 640f, height = 640f, radius = 32f),
            elements = standardElements("WI-FI GRATIS", "Apunta tu cámara para conectarte", headerY = 118f, titleY = 228f, subtextY = 1130f, footerY = 1210f)
        ),
        TemplateItemDef(
            id = "vip_ticket",
            name = "Boleto VIP",
            category = "Eventos",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#7C3AED"),
            cardColor = Color.parseColor("#120924"),
            defaultText = "PASE DE ACCESO",
            defaultSubtext = "Presenta este código en entrada",
            defaultHeaderAccent = "EVENTO ESPECIAL",
            defaultFooterAccent = "FILA PREFERENTE",
            defaultTextColor = Color.WHITE,
            defaultSubtextColor = Color.parseColor("#1E1B4B"),
            isDarkTheme = true,
            background = TemplateBackground(type = "artwork", color = "#120924"),
            qrArea = TemplateQrArea(x = 202f, y = 335f, width = 620f, height = 620f, radius = 24f),
            elements = standardElements("PASE DE ACCESO", "Presenta este código en entrada", headerY = 128f, titleY = 228f, subtextY = 1102f, footerY = 1178f)
        ),
        TemplateItemDef(
            id = "gala_arch",
            name = "Arco Gala",
            category = "Eventos",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#881337"),
            cardColor = Color.parseColor("#FDFBF7"),
            defaultText = "ESTÁS INVITADO",
            defaultSubtext = "Confirma tu asistencia aquí",
            defaultHeaderAccent = "CELEBRACIÓN",
            defaultTextColor = Color.parseColor("#881337"),
            defaultSubtextColor = Color.parseColor("#78350F"),
            isDarkTheme = false,
            background = TemplateBackground(type = "artwork", color = "#FDFBF7"),
            qrArea = TemplateQrArea(x = 212f, y = 385f, width = 600f, height = 600f, radius = 28f),
            elements = standardElements("ESTÁS INVITADO", "Confirma tu asistencia aquí", headerY = 165f, titleY = 262f, subtextY = 1098f, footerY = 1164f)
        ),
        TemplateItemDef(
            id = "neon_cyber",
            name = "Cyber Neón",
            category = "Redes sociales",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#06B6D4"),
            cardColor = Color.parseColor("#070A12"),
            defaultText = "SÍGUENOS",
            defaultSubtext = "Conecta en nuestras redes",
            defaultHeaderAccent = "ONLINE",
            defaultTextColor = Color.parseColor("#22D3EE"),
            defaultSubtextColor = Color.parseColor("#E2E8F0"),
            isDarkTheme = true,
            background = TemplateBackground(type = "artwork", color = "#070A12"),
            qrArea = TemplateQrArea(x = 197f, y = 365f, width = 630f, height = 630f, radius = 16f),
            elements = standardElements("SÍGUENOS", "Conecta en nuestras redes", headerY = 118f, titleY = 222f, subtextY = 1140f, footerY = 1218f)
        ),
        TemplateItemDef(
            id = "creator_pop",
            name = "Creador Pop",
            category = "Redes sociales",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#DD2476"),
            cardColor = Color.parseColor("#6D28D9"),
            defaultText = "MI PERFIL",
            defaultSubtext = "Escanea para seguirme",
            defaultHeaderAccent = "CREADOR",
            defaultTextColor = Color.WHITE,
            defaultSubtextColor = Color.parseColor("#FDF2F8"),
            isDarkTheme = true,
            background = TemplateBackground(type = "artwork", color = "#DD2476"),
            qrArea = TemplateQrArea(x = 192f, y = 360f, width = 640f, height = 640f, radius = 36f),
            elements = standardElements("MI PERFIL", "Escanea para seguirme", headerY = 118f, titleY = 228f, subtextY = 1135f, footerY = 1215f)
        ),
        TemplateItemDef(
            id = "polaroid_snap",
            name = "Instantánea",
            category = "Personal",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#0284C7"),
            cardColor = Color.parseColor("#E6E1D8"),
            defaultText = "MI ENLACE",
            defaultSubtext = "Escanea este recuerdo",
            defaultTextColor = Color.parseColor("#1C1917"),
            defaultSubtextColor = Color.parseColor("#57534E"),
            isDarkTheme = false,
            background = TemplateBackground(type = "artwork", color = "#E6E1D8"),
            qrArea = TemplateQrArea(x = 187f, y = 171f, width = 650f, height = 650f, radius = 8f),
            elements = standardElements("MI ENLACE", "Escanea este recuerdo", headerY = 126f, titleY = 982f, subtextY = 1072f, footerY = 1152f)
        ),
        TemplateItemDef(
            id = "swiss_poster",
            name = "Póster Suizo",
            category = "Personal",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#DC2626"),
            cardColor = Color.parseColor("#F4EFE6"),
            defaultText = "ESCANEA AQUÍ",
            defaultSubtext = "Información directa",
            defaultHeaderAccent = "N.º 01",
            defaultTextColor = Color.parseColor("#111827"),
            defaultSubtextColor = Color.parseColor("#374151"),
            isDarkTheme = false,
            background = TemplateBackground(type = "artwork", color = "#F4EFE6"),
            qrArea = TemplateQrArea(x = 197f, y = 360f, width = 630f, height = 630f, radius = 0f),
            elements = standardElements("ESCANEA AQUÍ", "Información directa", headerY = 118f, titleY = 228f, subtextY = 1142f, footerY = 1215f)
        )
    )

    fun renderTemplateBitmap(
        context: Context,
        custom: TemplateCustomization,
        targetWidth: Int = 1024,
        targetHeight: Int = 1280
    ): Bitmap {
        val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val scaleX = targetWidth / custom.templateDef.canvas.width
        val scaleY = targetHeight / custom.templateDef.canvas.height

        // 1. Draw Background
        if (custom.customFrameBitmap != null && !custom.customFrameBitmap.isRecycled) {
            canvas.drawColor(Color.WHITE)
            val srcBmp = custom.customFrameBitmap
            val srcW = srcBmp.width.toFloat()
            val srcH = srcBmp.height.toFloat()
            val scale = kotlin.math.min(targetWidth.toFloat() / srcW, targetHeight.toFloat() / srcH)
            val drawnW = srcW * scale
            val drawnH = srcH * scale
            val dx = (targetWidth - drawnW) / 2f
            val dy = (targetHeight - drawnH) / 2f
            val destRect = RectF(dx, dy, dx + drawnW, dy + drawnH)
            val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(srcBmp, null, destRect, bmpPaint)
        } else if (custom.templateDef.background.type == "artwork") {
            TemplateArtwork.drawTemplateBackground(
                canvas = canvas,
                templateId = custom.templateDef.id,
                targetWidth = targetWidth,
                targetHeight = targetHeight,
                fallbackColor = custom.templateDef.cardColor
            )
        } else if (custom.templateDef.background.type == "vector") {
            val vectorDrawable = ContextCompat.getDrawable(context, custom.templateDef.vectorResId)
            if (vectorDrawable != null) {
                vectorDrawable.setBounds(0, 0, targetWidth, targetHeight)
                vectorDrawable.draw(canvas)
            } else {
                canvas.drawColor(custom.templateDef.primaryColor)
            }
        } else {
            val bgColor = custom.templateDef.background.color?.let { Color.parseColor(it) } ?: custom.templateDef.primaryColor
            canvas.drawColor(bgColor)
        }

        // 2. Generate and Draw QR Code (with optional scale and vertical offset)
        val qrArea = custom.templateDef.qrArea
        val clampedScale = custom.qrScale.coerceIn(0.65f, 1.15f)
        val baseQrW = qrArea.width * scaleX
        val baseQrH = qrArea.height * scaleY
        val qrWidth = (baseQrW * clampedScale).toInt().coerceAtLeast(64)
        val qrHeight = (baseQrH * clampedScale).toInt().coerceAtLeast(64)

        val qrCenterX = (qrArea.x + qrArea.width / 2f) * scaleX
        val qrCenterY = (qrArea.y + qrArea.height / 2f) * scaleY + (custom.qrOffsetY * scaleY * 2f)
        val qrLeft = qrCenterX - qrWidth / 2f
        val qrTop = qrCenterY - qrHeight / 2f

        if (qrArea.background != null) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor(qrArea.background) }
            val pad = 12f * scaleX
            val rectF = RectF(
                qrLeft - pad,
                qrTop - pad,
                qrLeft + qrWidth + pad,
                qrTop + qrHeight + pad
            )
            canvas.drawRoundRect(rectF, qrArea.radius * scaleX, qrArea.radius * scaleY, bgPaint)
        }

        val qrBitmap = try {
            CodeGenerator.generateBitmap(
                content = custom.qrContent,
                format = BarcodeFormat.QR_CODE,
                width = qrWidth,
                height = qrHeight,
                foregroundColor = custom.qrForegroundColor,
                backgroundColor = custom.qrBackgroundColor,
                bodyShape = custom.qrBodyShape,
                logoBitmap = custom.logoBitmap,
                logoSizeRatio = custom.logoSizeMultiplier
            )
        } catch (e: Exception) {
            null
        }

        if (qrBitmap != null && !qrBitmap.isRecycled) {
            val bitmapToDraw = if (!custom.watermarkText.isNullOrBlank()) {
                val marked = CodeGenerator.applyWatermarkOverlay(qrBitmap, custom.watermarkText)
                if (marked !== qrBitmap && !qrBitmap.isRecycled) qrBitmap.recycle()
                marked
            } else {
                qrBitmap
            }
            if (!bitmapToDraw.isRecycled) {
                canvas.drawBitmap(bitmapToDraw, qrLeft, qrTop, null)
                bitmapToDraw.recycle()
            }
        }

        // 3. Render Parameterized Text Elements
        val overriddenElements = custom.templateDef.elements.map { el ->
            if (el is TemplateTextElement) {
                var newText = el.text
                var newColor = el.color
                var newAlign = el.align
                var newSize = el.fontSize
                var newBold = el.isBold

                when (el.id) {
                    "header" -> {
                        newText = custom.headerAccentText ?: custom.templateDef.defaultHeaderAccent ?: ""
                        newColor = String.format("#%06X", 0xFFFFFF and custom.textColor)
                        newSize = custom.textSizeSp * 2.1f
                        newBold = true
                    }
                    "title" -> {
                        val baseTitle = if (custom.isUppercase) custom.titleText.uppercase() else custom.titleText
                        newText = baseTitle
                        newColor = String.format("#%06X", 0xFFFFFF and custom.textColor)
                        newAlign = custom.textAlign
                        newSize = custom.textSizeSp * 4.5f
                        newBold = custom.isBold
                    }
                    "subtext" -> {
                        newText = if (custom.showSubtext) custom.subtext else ""
                        newColor = String.format("#%06X", 0xFFFFFF and custom.subtextColor)
                        newSize = custom.subtextSizeSp * 4.2f
                        newBold = false
                    }
                    "footer" -> {
                        newText = custom.footerAccentText ?: custom.templateDef.defaultFooterAccent ?: ""
                        newColor = String.format("#%06X", 0xFFFFFF and custom.subtextColor)
                        newSize = custom.subtextSizeSp * 3.4f
                        newBold = true
                    }
                }

                el.copy(text = newText, color = newColor, align = newAlign, fontSize = newSize, isBold = newBold)
            } else {
                el
            }
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        for (el in overriddenElements) {
            if (el is TemplateTextElement && el.text.isNotBlank()) {
                textPaint.color = Color.parseColor(el.color)
                textPaint.textSize = el.fontSize * scaleX
                textPaint.textAlign = el.align
                textPaint.typeface = if (el.isBold) {
                    Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                textPaint.alpha = if (el.id == "header" || el.id == "footer") 215 else 255

                var baseAnchorX = el.x
                if (el.id == "title" || el.align != Paint.Align.CENTER) {
                    when (el.align) {
                        Paint.Align.LEFT -> baseAnchorX = el.x - (el.width / 2f) + (1024f * 0.12f)
                        Paint.Align.RIGHT -> baseAnchorX = el.x + (el.width / 2f) - (1024f * 0.12f)
                        Paint.Align.CENTER -> baseAnchorX = el.x
                    }
                }

                var adjustedX = baseAnchorX * scaleX
                var adjustedY = el.y * scaleY

                if (el.id == "title" || el.id == "header") {
                    adjustedX += custom.textOffsetX * scaleX * 2f
                    adjustedY += custom.textOffsetY * scaleY * 2f
                } else if (el.id == "subtext" || el.id == "footer") {
                    adjustedY += (custom.textOffsetY * 0.5f) * scaleY * 2f
                }

                val maxTextWidth = targetWidth * 0.80f
                fitTextSize(textPaint, el.text, maxTextWidth)
                drawFormattedText(canvas, el.text, adjustedX, adjustedY, textPaint)
            }
        }

        return result
    }

    private fun fitTextSize(paint: Paint, text: String, maxWidth: Float) {
        val lines = text.split("\n")
        var maxLineWidth = 0f
        for (line in lines) {
            val w = paint.measureText(line)
            if (w > maxLineWidth) maxLineWidth = w
        }
        if (maxLineWidth > maxWidth && maxLineWidth > 0f) {
            val factor = maxWidth / maxLineWidth
            paint.textSize = paint.textSize * factor
        }
    }

    private fun drawFormattedText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint) {
        val lines = text.split("\n")
        val fontSpacing = paint.fontSpacing
        var currentY = y
        for (line in lines) {
            canvas.drawText(line, x, currentY, paint)
            currentY += fontSpacing
        }
    }
}
