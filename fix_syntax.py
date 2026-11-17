import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# Let's completely restore the file from a known working state.
# We will use git to fetch the last known working state from history if possible, but there's no git.
# Let's just rewrite the whole object.

new_renderer = """package com.example.utils

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.example.R
import com.google.zxing.BarcodeFormat

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
    val isDarkTheme: Boolean = false,
    val defaultHeaderAccent: String? = null,
    val defaultFooterAccent: String? = null
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
    val logoSizeMultiplier: Float = 1.0f,
    val textOffsetX: Float = 0f,
    val textOffsetY: Float = 0f,
    
    // QR generation details
    val qrForegroundColor: Int = Color.BLACK,
    val qrBackgroundColor: Int = Color.WHITE,
    val qrType: String = "SQUARE",
    val qrEyeStyle: String = "SQUARE",
    val enableGradient: Boolean = false,
    val gradientStartColor: Int = Color.BLACK,
    val gradientEndColor: Int = Color.BLACK,
    val gradientAngle: Float = 0f
)

object TemplateRenderer {

    val AVAILABLE_TEMPLATES = listOf(
        TemplateItemDef(
            id = "escanea_aqui",
            name = "Escanea Aquí",
            category = "Destacado",
            vectorResId = R.drawable.template_frame_escanea_aqui,
            primaryColor = Color.parseColor("#D4062A"),
            cardColor = Color.parseColor("#012322"),
            defaultText = "Escanea Aquí",
            defaultSubtext = "",
            defaultTextColor = Color.parseColor("#FDFEFF"),
            isDarkTheme = true,
            defaultHeaderAccent = null,
            defaultFooterAccent = null
        ),
        TemplateItemDef(
            id = "navy_geo",
            name = "Navy Geometric",
            category = "Corporativo",
            vectorResId = R.drawable.template_frame_navy_geo,
            primaryColor = Color.parseColor("#00A3FF"),
            cardColor = Color.parseColor("#093C68"),
            defaultText = "SCAN HERE",
            defaultSubtext = "www.123.com",
            defaultTextColor = Color.WHITE,
            isDarkTheme = true,
            defaultHeaderAccent = null,
            defaultFooterAccent = null
        ),
        TemplateItemDef(
            id = "pay_minimal",
            name = "Escanea Para Pagar",
            category = "Comercio",
            vectorResId = R.drawable.template_frame_pay_minimal,
            primaryColor = Color.BLACK,
            cardColor = Color.WHITE,
            defaultText = "ESCANEA PARA PAGAR",
            defaultSubtext = "¡GRACIAS POR ELEGIRNOS!",
            defaultTextColor = Color.BLACK,
            isDarkTheme = false,
            defaultHeaderAccent = null,
            defaultFooterAccent = null
        ),
        TemplateItemDef(
            id = "modern_blue",
            name = "Modern Blue",
            category = "Moderno",
            vectorResId = R.drawable.template_frame_modern,
            primaryColor = Color.parseColor("#3B82F6"),
            cardColor = Color.WHITE,
            defaultText = "ESCANÉAME",
            defaultSubtext = "Apunta con la cámara",
            defaultTextColor = Color.WHITE,
            isDarkTheme = false
        ),
        TemplateItemDef(
            id = "scan_me_neon",
            name = "Cyber Neon",
            category = "Neón",
            vectorResId = R.drawable.template_frame_scanme,
            primaryColor = Color.parseColor("#06B6D4"),
            cardColor = Color.parseColor("#0F172A"),
            defaultText = "SCAN ME",
            defaultSubtext = "Acceso instantáneo con tu móvil",
            defaultTextColor = Color.parseColor("#22D3EE"),
            isDarkTheme = true
        ),
        TemplateItemDef(
            id = "minimal_clean",
            name = "Minimal Clean",
            category = "Minimalista",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#475569"),
            cardColor = Color.parseColor("#F8FAFC"),
            defaultText = "MENÚ DIGITAL",
            defaultSubtext = "Escanea el código para ver",
            defaultTextColor = Color.parseColor("#1E293B"),
            isDarkTheme = false
        ),
        TemplateItemDef(
            id = "vip_gold",
            name = "VIP Gold",
            category = "Elegante",
            vectorResId = R.drawable.template_frame_gold,
            primaryColor = Color.parseColor("#F59E0B"),
            cardColor = Color.parseColor("#18181B"),
            defaultText = "VIP ACCESS",
            defaultSubtext = "Escanea para beneficios exclusivos",
            defaultTextColor = Color.parseColor("#FBBF24"),
            isDarkTheme = true
        ),
        TemplateItemDef(
            id = "cafe_menu",
            name = "Café & Menú",
            category = "Gastronomía",
            vectorResId = R.drawable.template_frame_cafe,
            primaryColor = Color.parseColor("#4A3B32"),
            cardColor = Color.parseColor("#D4C3A3"),
            defaultText = "NUESTRO MENÚ",
            defaultSubtext = "Escanea para ordenar",
            defaultTextColor = Color.parseColor("#4A3B32"),
            isDarkTheme = false
        ),
        TemplateItemDef(
            id = "tech_blue",
            name = "Tech Blueprint",
            category = "Tecnología",
            vectorResId = R.drawable.template_frame_tech,
            primaryColor = Color.parseColor("#38BDF8"),
            cardColor = Color.parseColor("#0F172A"),
            defaultText = "WIFI ACCESS",
            defaultSubtext = "Conéctate rápidamente",
            defaultTextColor = Color.WHITE,
            isDarkTheme = true
        ),
        TemplateItemDef(
            id = "eco_nature",
            name = "Eco Nature",
            category = "Natural",
            vectorResId = R.drawable.template_frame_nature,
            primaryColor = Color.parseColor("#10B981"),
            cardColor = Color.parseColor("#D1FAE5"),
            defaultText = "100% NATURAL",
            defaultSubtext = "Conoce nuestro origen",
            defaultTextColor = Color.parseColor("#14532D"),
            isDarkTheme = false
        ),
        TemplateItemDef(
            id = "romantic_love",
            name = "Romantic Love",
            category = "Especial",
            vectorResId = R.drawable.template_frame_love,
            primaryColor = Color.parseColor("#F43F5E"),
            cardColor = Color.parseColor("#FDF2F8"),
            defaultText = "SAVE THE DATE",
            defaultSubtext = "Confirma tu asistencia",
            defaultTextColor = Color.parseColor("#BE185D"),
            isDarkTheme = false
        ),
        TemplateItemDef(
            id = "party_event",
            name = "Party Event",
            category = "Eventos",
            vectorResId = R.drawable.template_frame_party,
            primaryColor = Color.parseColor("#9333EA"),
            cardColor = Color.parseColor("#F3E8FF"),
            defaultText = "VIP TICKETS",
            defaultSubtext = "Escanea tu entrada",
            defaultTextColor = Color.parseColor("#581C87"),
            isDarkTheme = false
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

        // 1. Draw Vector Background Frame
        val vectorDrawable = ContextCompat.getDrawable(context, custom.templateDef.vectorResId)
        if (vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, targetWidth, targetHeight)
            vectorDrawable.draw(canvas)
        } else {
            canvas.drawColor(custom.templateDef.primaryColor)
        }

        // 2. Generate and Draw QR Code inside the designated area
        val qrSize = (targetWidth * 0.65f).toInt()
        val qrBitmap = try {
            SmartQrFeatures.generateCustomQrCode(
                content = custom.qrContent,
                width = qrSize,
                height = qrSize,
                foregroundColor = custom.qrForegroundColor,
                backgroundColor = custom.qrBackgroundColor,
                shape = custom.qrType,
                eyeShape = custom.qrEyeStyle,
                enableGradient = custom.enableGradient,
                gradientStartColor = custom.gradientStartColor,
                gradientEndColor = custom.gradientEndColor,
                gradientAngle = custom.gradientAngle,
                logoBitmap = custom.logoBitmap,
                logoSizeMultiplier = custom.logoSizeMultiplier
            )
        } catch (e: Exception) {
            null
        }

        if (qrBitmap != null) {
            // Calculate center for QR code
            // The QR panel in templates is usually centered horizontally.
            // Vertically, it usually starts around Y=180 on a 640 canvas (0.281 ratio).
            val baseQrY = when (custom.templateDef.id) {
                "escanea_aqui" -> targetHeight * 0.3125f
                "navy_geo" -> targetHeight * 0.3125f
                "modern_blue" -> targetHeight * 0.32f
                "minimal_clean" -> targetHeight * 0.32f
                "vip_gold" -> targetHeight * 0.34f
                "cafe_menu" -> targetHeight * 0.3125f
                "tech_blue" -> targetHeight * 0.28125f
                "eco_nature" -> targetHeight * 0.3125f
                "romantic_love" -> targetHeight * 0.3125f
                "party_event" -> targetHeight * 0.3125f
                else -> targetHeight * 0.28125f
            }
            val qrX = (targetWidth - qrSize) / 2f
            val qrY = baseQrY
            canvas.drawBitmap(qrBitmap, qrX, qrY, null)
        }

        // --- TEXT RENDERING ---
        val density = context.resources.displayMetrics.density
        val maxTextWidth = targetWidth * 0.85f

        // 3. Draw Header Accent (e.g. "¡OFERTA ESPECIAL!")
        val headerAccent = custom.headerAccentText ?: custom.templateDef.defaultHeaderAccent
        val hasHeaderAccent = !headerAccent.isNullOrBlank()

        if (hasHeaderAccent && headerAccent != null) {
            val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = custom.textColor
                textSize = (custom.textSizeSp * 0.5f) * density * (targetWidth / 512f)
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                alpha = 200
            }
            fitTextSize(accentPaint, headerAccent, maxTextWidth)

            val accentY = when (custom.templateDef.id) {
                "navy_geo" -> (targetHeight * 0.115f) + (custom.textOffsetY * density * (targetHeight / 640f))
                "pay_minimal" -> (targetHeight * 0.125f) + (custom.textOffsetY * density * (targetHeight / 640f))
                else -> (targetHeight * 0.100f) + (custom.textOffsetY * density * (targetHeight / 640f))
            }
            drawFormattedText(canvas, headerAccent, targetWidth / 2f, accentY, accentPaint, maxTextWidth)
        }

        // 4. Draw Main Title (e.g. "SCAN HERE" or "ESCANEA PARA PAGAR")
        val titleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = custom.textColor
            textSize = custom.textSizeSp * density * (targetWidth / 512f)
            textAlign = custom.textAlign
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val titleToDraw = custom.titleText

        val baseTitleY = when (custom.templateDef.id) {
            "escanea_aqui" -> targetHeight * 0.178f
            "navy_geo" -> if (hasHeaderAccent) targetHeight * 0.190f else targetHeight * 0.165f
            "pay_minimal" -> if (hasHeaderAccent) targetHeight * 0.195f else targetHeight * 0.175f
            "modern_blue" -> if (hasHeaderAccent) targetHeight * 0.170f else targetHeight * 0.145f
            "scan_me_neon" -> if (hasHeaderAccent) targetHeight * 0.175f else targetHeight * 0.150f
            "minimal_clean" -> if (hasHeaderAccent) targetHeight * 0.175f else targetHeight * 0.150f
            "vip_gold" -> if (hasHeaderAccent) targetHeight * 0.175f else targetHeight * 0.150f
            "cafe_menu" -> targetHeight * 0.170f
            "tech_blue" -> targetHeight * 0.155f
            "eco_nature" -> targetHeight * 0.200f
            "romantic_love" -> targetHeight * 0.205f
            "party_event" -> targetHeight * 0.200f
            else -> targetHeight * 0.150f
        }
        val titleY = baseTitleY + (custom.textOffsetY * density * (targetHeight / 640f))

        val baseTitleX = if (custom.templateDef.id == "escanea_aqui") {
            when (custom.textAlign) {
                Paint.Align.LEFT -> targetWidth * 0.255f
                Paint.Align.RIGHT -> targetWidth * 0.86f
                Paint.Align.CENTER -> targetWidth * 0.56f
            }
        } else {
            when (custom.textAlign) {
                Paint.Align.LEFT -> targetWidth * 0.12f
                Paint.Align.RIGHT -> targetWidth * 0.88f
                Paint.Align.CENTER -> targetWidth * 0.5f
            }
        }
        val titleX = baseTitleX + (custom.textOffsetX * density * (targetWidth / 512f))

        if (titleToDraw.isNotBlank()) {
            fitTextSize(titleTextPaint, titleToDraw, maxTextWidth)
            drawFormattedText(canvas, titleToDraw, titleX, titleY, titleTextPaint, maxTextWidth)
        }

        // 5. Draw Subtext (e.g. "¡GRACIAS POR ELEGIRNOS!" or "www.123.com")
        val footerAccent = custom.footerAccentText ?: custom.templateDef.defaultFooterAccent
        val hasFooterAccent = !footerAccent.isNullOrBlank()

        if (custom.subtext.isNotBlank()) {
            val subtextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                if (custom.templateDef.id == "navy_geo") {
                    color = Color.parseColor("#4ADE80")
                    textSize = (custom.subtextSizeSp * 1.1f) * density * (targetWidth / 512f)
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                } else if (custom.templateDef.id == "escanea_aqui") {
                    color = Color.parseColor("#D4062A")
                    textSize = (custom.subtextSizeSp * 1.2f) * density * (targetWidth / 512f)
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                } else {
                    color = custom.subtextColor
                    textSize = custom.subtextSizeSp * density * (targetWidth / 512f)
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
            }

            fitTextSize(subtextPaint, custom.subtext, maxTextWidth)

            val baseSubY = when (custom.templateDef.id) {
                "pay_minimal" -> if (hasFooterAccent) targetHeight * 0.835f else targetHeight * 0.850f
                "navy_geo" -> if (hasFooterAccent) targetHeight * 0.845f else targetHeight * 0.860f
                "modern_blue" -> if (hasFooterAccent) targetHeight * 0.868f else targetHeight * 0.883f
                "scan_me_neon" -> if (hasFooterAccent) targetHeight * 0.905f else targetHeight * 0.920f
                "minimal_clean" -> if (hasFooterAccent) targetHeight * 0.845f else targetHeight * 0.860f
                "vip_gold" -> if (hasFooterAccent) targetHeight * 0.875f else targetHeight * 0.892f
                "cafe_menu" -> targetHeight * 0.905f
                "tech_blue" -> targetHeight * 0.925f
                "eco_nature" -> targetHeight * 0.945f
                "romantic_love" -> targetHeight * 0.895f
                "party_event" -> targetHeight * 0.915f
                else -> targetHeight * 0.860f
            }
            val subY = baseSubY + (custom.textOffsetY * 0.5f * density * (targetHeight / 640f))
            drawFormattedText(canvas, custom.subtext, targetWidth / 2f, subY, subtextPaint, maxTextWidth)
        }

        // 6. Draw Footer Accent (e.g. "TU LOGO AQUÍ" or website)
        if (hasFooterAccent && footerAccent != null) {
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = custom.subtextColor
                textSize = (custom.subtextSizeSp * 0.8f) * density * (targetWidth / 512f)
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                alpha = 200
            }
            fitTextSize(footerPaint, footerAccent, maxTextWidth)

            val baseFooterY = when (custom.templateDef.id) {
                "pay_minimal" -> targetHeight * 0.890f
                "navy_geo" -> targetHeight * 0.930f
                "scan_me_neon" -> targetHeight * 0.950f
                "minimal_clean" -> targetHeight * 0.930f
                "vip_gold" -> targetHeight * 0.930f
                else -> targetHeight * 0.930f
            }
            val footerY = baseFooterY + (custom.textOffsetY * 0.5f * density * (targetHeight / 640f))
            drawFormattedText(canvas, footerAccent, targetWidth / 2f, footerY, footerPaint, maxTextWidth)
        }

        return result
    }

    /**
     * Scales the Paint's text size down if the text exceeds maxWidth.
     */
    private fun fitTextSize(paint: Paint, text: String, maxWidth: Float) {
        val lines = text.split("\\n")
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

    /**
     * Draws multi-line text with clean line spacing and proper vertical baseline alignment.
     */
    private fun drawFormattedText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint, maxWidth: Float) {
        val lines = text.split("\\n")
        val fontSpacing = paint.fontSpacing
        var currentY = y
        for (line in lines) {
            canvas.drawText(line, x, currentY, paint)
            currentY += fontSpacing
        }
    }
}
"""

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(new_renderer)
