package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Base64
import com.google.zxing.BarcodeFormat
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

// ==========================================
// 1. SECRET QR ENCRYPTION & DECRYPTION
// ==========================================
object SecretQrHelper {
    private const val PREFIX = "SECQR|v1|"
    private const val ITERATIONS = 5000
    private const val KEY_LENGTH = 256

    fun isSecretQr(content: String): Boolean {
        return content.trim().startsWith("SECQR|")
    }

    fun encrypt(plainText: String, pinOrPass: String): String {
        if (plainText.isEmpty() || pinOrPass.isEmpty()) return ""
        try {
            val random = SecureRandom()
            val salt = ByteArray(16)
            random.nextBytes(salt)
            val iv = ByteArray(16)
            random.nextBytes(iv)

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(pinOrPass.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
            val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val cipherB64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)

            return "$PREFIX$saltB64:$ivB64:$cipherB64"
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun decrypt(encryptedPayload: String, pinOrPass: String): String? {
        val trimmedPayload = encryptedPayload.trim()
        val result = tryDecryptWithPin(trimmedPayload, pinOrPass)
        if (result != null) return result
        if (pinOrPass != pinOrPass.trim()) {
            return tryDecryptWithPin(trimmedPayload, pinOrPass.trim())
        }
        return null
    }

    private fun tryDecryptWithPin(encryptedPayload: String, pinOrPass: String): String? {
        if (encryptedPayload.isEmpty() || pinOrPass.isEmpty()) return null
        return try {
            val raw = if (encryptedPayload.startsWith(PREFIX)) {
                encryptedPayload.removePrefix(PREFIX)
            } else if (encryptedPayload.startsWith("SECQR|")) {
                val parts = encryptedPayload.split("|", limit = 3)
                if (parts.size >= 3) parts[2] else encryptedPayload
            } else {
                encryptedPayload
            }

            val parts = raw.split(":")
            if (parts.size != 3) return null

            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            val iv = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipherBytes = Base64.decode(parts[2], Base64.NO_WRAP)

            // Try standard iterations (5000 and legacy 10000)
            val iterationsList = listOf(ITERATIONS, 10000, 1000)
            for (iter in iterationsList) {
                try {
                    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    val spec = PBEKeySpec(pinOrPass.toCharArray(), salt, iter, KEY_LENGTH)
                    val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

                    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
                    val decryptedBytes = cipher.doFinal(cipherBytes)
                    return String(decryptedBytes, Charsets.UTF_8)
                } catch (e: Exception) {
                    // continue to next iteration
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}

// ==========================================
// 2. SOUND & HAPTIC FEEDBACK FX ENGINE
// ==========================================
object SoundFxHelper {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    fun playFeedback(context: Context, preset: String, vibrate: Boolean = true, sound: Boolean = true) {
        if (vibrate) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    when (preset) {
                        "haptic_heavy" -> vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
                        "double_tick" -> {
                            val timings = longArrayOf(0, 40, 60, 40)
                            vibrator?.vibrate(VibrationEffect.createWaveform(timings, -1))
                        }
                        else -> vibrator?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(60)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (sound) {
            try {
                when (preset) {
                    "chaching" -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
                    }
                    "laser" -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 120)
                    }
                    "bell" -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_SUP_CONFIRM, 250)
                    }
                    "gameboy" -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 150)
                    }
                    "haptic_only" -> {
                        // Silent
                    }
                    else -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

// ==========================================
// 3. ARTISTIC AI QR STYLES
// ==========================================
data class ArtisticQrPreset(
    val id: String,
    val name: String,
    val description: String,
    val foregroundColor: Int,
    val backgroundColor: Int,
    val bodyShape: BodyShape,
    val tag: String,
    val badgeLabel: String? = null
)

object ArtisticQrPresets {
    val presets = listOf(
        ArtisticQrPreset(
            id = "cyber_neon",
            name = "⚡ Cyberpunk Neon",
            description = "Obsidiana con diamantes cian eléctrico de alto contraste",
            foregroundColor = Color.parseColor("#00F0FF"),
            backgroundColor = Color.parseColor("#0D0E15"),
            bodyShape = BodyShape.DIAMOND,
            tag = "CYBER",
            badgeLabel = "CYBERPUNK"
        ),
        ArtisticQrPreset(
            id = "liquid_glass",
            name = "🌊 Liquid Ice Blue",
            description = "Efecto traslúcido con nodos líquidos redondeados",
            foregroundColor = Color.parseColor("#0284C7"),
            backgroundColor = Color.parseColor("#F0F9FF"),
            bodyShape = BodyShape.ROUNDED,
            tag = "FROST",
            badgeLabel = "LIQUID"
        ),
        ArtisticQrPreset(
            id = "royal_gold",
            name = "👑 Oro Metálico VIP",
            description = "Dorado de lujo sobre fondo negro carbón",
            foregroundColor = Color.parseColor("#F59E0B"),
            backgroundColor = Color.parseColor("#18181B"),
            bodyShape = BodyShape.OCTAGON,
            tag = "LUXURY",
            badgeLabel = "VIP GOLD"
        ),
        ArtisticQrPreset(
            id = "emerald_zen",
            name = "🌿 Esmeralda Zen",
            description = "Puntos orgánicos circulares verde bosque sobre menta",
            foregroundColor = Color.parseColor("#047857"),
            backgroundColor = Color.parseColor("#ECFDF5"),
            bodyShape = BodyShape.CIRCLE,
            tag = "ORGANIC",
            badgeLabel = "ORGANIC"
        ),
        ArtisticQrPreset(
            id = "retro_arcade",
            name = "👾 Retro 8-Bit Arcade",
            description = "Estilo clásico Gameboy con puntos de fósforo verde",
            foregroundColor = Color.parseColor("#15803D"),
            backgroundColor = Color.parseColor("#DCFCE7"),
            bodyShape = BodyShape.DOT,
            tag = "RETRO",
            badgeLabel = "8-BIT"
        ),
        ArtisticQrPreset(
            id = "cherry_sakura",
            name = "🌸 Cherry Blossom",
            description = "Rosa pastel cálido con corazones sobre porcelana blanca",
            foregroundColor = Color.parseColor("#E11D48"),
            backgroundColor = Color.parseColor("#FFF1F2"),
            bodyShape = BodyShape.HEART,
            tag = "PASTEL",
            badgeLabel = "SAKURA"
        ),
        ArtisticQrPreset(
            id = "solar_flare",
            name = "🌋 Solar Flare",
            description = "Hexágonos naranja fuego ardiente sobre blanco puro",
            foregroundColor = Color.parseColor("#EA580C"),
            backgroundColor = Color.parseColor("#FFF7ED"),
            bodyShape = BodyShape.HEXAGON,
            tag = "VIBRANT",
            badgeLabel = "SOLAR"
        )
    )
}

// ==========================================
// 4. MULTI-QR STICKER SHEET PDF EXPORTER
// ==========================================
object StickerSheetPdfHelper {
    enum class SheetLayout(val title: String, val cols: Int, val rows: Int) {
        SOLO_POSTER("1 Póster Grande (1x1)", 1, 1),
        GRID_4("4 Stickers Grandes (2x2)", 2, 2),
        GRID_6("6 Etiquetas / Mesas (2x3)", 2, 3),
        GRID_9("9 Stickers Cuadrados (3x3)", 3, 3),
        GRID_12("12 Etiquetas / Productos (3x4)", 3, 4)
    }

    fun generateStickerSheetPdf(
        context: Context,
        qrBitmap: Bitmap,
        title: String,
        sublabel: String,
        layout: SheetLayout
    ): File {
        // Standard A4 dimensions in PostScript points: 595 x 842 pt
        val pageWidth = 595
        val pageHeight = 842
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(Color.WHITE)

        val margin = 36f // 0.5 inch margins
        val availableWidth = pageWidth - (margin * 2)
        val availableHeight = pageHeight - (margin * 2)

        val cellWidth = availableWidth / layout.cols
        val cellHeight = availableHeight / layout.rows

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = if (layout.rows > 2) 11f else 16f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = if (layout.rows > 2) 8f else 11f
            textAlign = Paint.Align.CENTER
        }

        for (r in 0 until layout.rows) {
            for (c in 0 until layout.cols) {
                val left = margin + (c * cellWidth)
                val top = margin + (r * cellHeight)
                val right = left + cellWidth
                val bottom = top + cellHeight

                // Draw dashed cut border
                canvas.drawRect(left, top, right, bottom, borderPaint)

                // Calculate item box inside cell
                val padding = 12f
                val contentLeft = left + padding
                val contentTop = top + padding
                val contentRight = right - padding
                val contentBottom = bottom - padding

                val centerX = (left + right) / 2f

                // Draw Title
                if (title.isNotBlank()) {
                    canvas.drawText(title, centerX, contentTop + titlePaint.textSize, titlePaint)
                }

                // Draw QR Code
                val qrTopOffset = if (title.isNotBlank()) titlePaint.textSize + 8f else 0f
                val qrBottomOffset = if (sublabel.isNotBlank()) subPaint.textSize + 8f else 0f
                val maxQrSize = Math.min(contentRight - contentLeft, (contentBottom - contentTop) - (qrTopOffset + qrBottomOffset))
                
                if (maxQrSize > 20 && !qrBitmap.isRecycled && qrBitmap.width > 0 && qrBitmap.height > 0) {
                    val qrLeft = centerX - (maxQrSize / 2f)
                    val qrTop = contentTop + qrTopOffset + (((contentBottom - contentTop - qrTopOffset - qrBottomOffset) - maxQrSize) / 2f)
                    val qrDestRect = RectF(qrLeft, qrTop, qrLeft + maxQrSize, qrTop + maxQrSize)
                    canvas.drawBitmap(qrBitmap, null, qrDestRect, null)
                }

                // Draw Sublabel
                if (sublabel.isNotBlank()) {
                    canvas.drawText(sublabel, centerX, contentBottom - 4f, subPaint)
                }
            }
        }

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "Stickers_${System.currentTimeMillis()}.pdf")
        try {
            FileOutputStream(outputFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
                outputStream.flush()
            }
        } finally {
            pdfDocument.close()
        }

        return outputFile
    }
}

// ==========================================
// 5. PRODUCT & FOOD SCAN LOOKUP HELPER
// ==========================================
object ProductLookupHelper {
    fun getSearchUrl(barcode: String, engine: String = "Google"): String {
        return when (engine.lowercase()) {
            "openfoodfacts" -> "https://world.openfoodfacts.org/product/$barcode"
            "amazon" -> "https://www.amazon.com/s?k=$barcode"
            "ebay" -> "https://www.ebay.com/sch/i.html?_nkw=$barcode"
            "barcodelookup" -> "https://www.barcodelookup.com/$barcode"
            else -> "https://www.google.com/search?q=$barcode"
        }
    }

    fun is1DProductBarcode(format: String): Boolean {
        val upper = format.uppercase()
        return upper.contains("EAN") || upper.contains("UPC") || upper.contains("128") || upper.contains("ITF") || upper.contains("39")
    }
}
