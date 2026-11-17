import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# We will generate a new TemplateRenderer.kt
new_kt = """package com.example.utils

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.example.R
import com.google.zxing.BarcodeFormat

// --- PARAMETERIZED ARCHITECTURE ---

data class TemplateCanvas(val width: Float = 1024f, val height: Float = 1280f)

data class TemplateBackground(
    val type: String = "vector", // "solid", "vector"
    val color: String? = null
)

data class TemplateQrArea(
    val x: Float = 152f,
    val y: Float = 360f,
    val width: Float = 720f,
    val height: Float = 720f,
    val radius: Float = 40f,
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

// --- EXISTING COMPATIBILITY CLASSES ---

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
    val defaultFooterAccent: String? = null,
    
    // Parameterized components
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
    val logoSizeMultiplier: Float = 1.0f,
    val textOffsetX: Float = 0f,
    val textOffsetY: Float = 0f,
    val qrForegroundColor: Int = Color.BLACK,
    val qrBackgroundColor: Int = Color.WHITE
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
            
            // Parameterized
            qrArea = TemplateQrArea(x = 179f, y = 400f, width = 665f, height = 665f, radius = 40f, background = "#FFFFFF"),
            elements = listOf(
                TemplateTextElement(id = "title", x = 128f, y = 227f, width = 768f, height = 100f, text = "Escanea Aquí", fontSize = 48f, align = Paint.Align.CENTER, color = "#FDFEFF", isBold = true),
                TemplateTextElement(id = "subtext", x = 128f, y = 1100f, width = 768f, height = 100f, text = "", fontSize = 28f, align = Paint.Align.CENTER, color = "#D4062A", isBold = true)
            )
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
            
            qrArea = TemplateQrArea(x = 179f, y = 400f, width = 665f, height = 665f, radius = 40f, background = "#FFFFFF"),
            elements = listOf(
                TemplateTextElement(id = "header", x = 128f, y = 147f, width = 768f, height = 50f, text = "", fontSize = 24f, align = Paint.Align.CENTER, color = "#FFFFFF", isBold = true),
                TemplateTextElement(id = "title", x = 128f, y = 211f, width = 768f, height = 100f, text = "SCAN HERE", fontSize = 48f, align = Paint.Align.CENTER, color = "#FFFFFF", isBold = true),
                TemplateTextElement(id = "subtext", x = 128f, y = 1100f, width = 768f, height = 80f, text = "www.123.com", fontSize = 28f, align = Paint.Align.CENTER, color = "#4ADE80", isBold = true),
                TemplateTextElement(id = "footer", x = 128f, y = 1190f, width = 768f, height = 50f, text = "", fontSize = 22f, align = Paint.Align.CENTER, color = "#FFFFFF", isBold = false)
            )
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
            
            qrArea = TemplateQrArea(x = 179f, y = 359f, width = 665f, height = 665f, radius = 24f, background = "#FFFFFF"),
            elements = listOf(
                TemplateTextElement(id = "header", x = 128f, y = 160f, width = 768f, height = 50f, text = "", fontSize = 24f, align = Paint.Align.CENTER, color = "#000000", isBold = true),
                TemplateTextElement(id = "title", x = 128f, y = 224f, width = 768f, height = 100f, text = "ESCANEA PARA PAGAR", fontSize = 48f, align = Paint.Align.CENTER, color = "#000000", isBold = true),
                TemplateTextElement(id = "subtext", x = 128f, y = 1088f, width = 768f, height = 80f, text = "¡GRACIAS POR ELEGIRNOS!", fontSize = 28f, align = Paint.Align.CENTER, color = "#000000", isBold = false),
                TemplateTextElement(id = "footer", x = 128f, y = 1139f, width = 768f, height = 50f, text = "", fontSize = 22f, align = Paint.Align.CENTER, color = "#000000", isBold = false)
            )
        ),
        TemplateItemDef(
            id = "modern_blue",
            name = "Modern Blue",
            category = "Moderno",
            vectorResId = R.drawable.template_frame_modern,
            primaryColor = Color.parseColor("#3B82F6"),
            cardColor = Color.WHITE,
            defaultText = "ESCANÉAME",
            defaultSubtext = "Apunta con tu cámara para abrir",
            defaultTextColor = Color.WHITE,
            isDarkTheme = true,
            
            qrArea = TemplateQrArea(x = 179f, y = 409f, width = 665f, height = 665f, radius = 30f, background = "#FFFFFF"),
            elements = listOf(
                TemplateTextElement(id = "header", x = 128f, y = 128f, width = 768f, height = 50f, text = "", fontSize = 24f, align = Paint.Align.CENTER, color = "#FFFFFF", isBold = true),
                TemplateTextElement(id = "title", x = 128f, y = 185f, width = 768f, height = 100f, text = "ESCANÉAME", fontSize = 48f, align = Paint.Align.CENTER, color = "#FFFFFF", isBold = true),
                TemplateTextElement(id = "subtext", x = 128f, y = 1130f, width = 768f, height = 80f, text = "Apunta con tu cámara", fontSize = 28f, align = Paint.Align.CENTER, color = "#FFFFFF", isBold = false)
            )
        ),
        TemplateItemDef(
            id = "scan_me_neon",
            name = "Neon Violet",
            category = "Moderno",
            vectorResId = R.drawable.template_frame_scan_me,
            primaryColor = Color.parseColor("#8B5CF6"),
            cardColor = Color.parseColor("#1E1B4B"),
            defaultText = "SCAN ME",
            defaultSubtext = "Para más información",
            defaultTextColor = Color.WHITE,
            isDarkTheme = true,
            
            qrArea = TemplateQrArea(x = 179f, y = 359f, width = 665f, height = 665f, radius = 30f, background = "#FFFFFF"),
            elements = listOf(
                TemplateTextElement(id = "header", x = 128f, y = 128f, width = 768f, height = 50f, text = "", fontSize = 24f, align = Paint.Align.CENTER, color = "#FFFFFF", isBold = true),
                TemplateTextElement(id = "title", x = 128f, y = 192f, width = 768f, height = 100f, text = "SCAN ME", fontSize = 48f, align = Paint.Align.CENTER, color = "#FFFFFF", isBold = true),
                TemplateTextElement(id = "subtext", x = 128f, y = 1177f, width = 768f, height = 80f, text = "Para más información", fontSize = 28f, align = Paint.Align.CENTER, color = "#FFFFFF", isBold = false),
                TemplateTextElement(id = "footer", x = 128f, y = 1216f, width = 768f, height = 50f, text = "", fontSize = 22f, align = Paint.Align.CENTER, color = "#FFFFFF", isBold = false)
            )
        ),
        TemplateItemDef(
            id = "minimal_clean",
            name = "Minimal Clean",
            category = "Minimalista",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#E5E7EB"),
            cardColor = Color.parseColor("#F9FAFB"),
            defaultText = "Información",
            defaultSubtext = "Escanea el código",
            defaultTextColor = Color.parseColor("#111827"),
            isDarkTheme = false,
            
            qrArea = TemplateQrArea(x = 179f, y = 409f, width = 665f, height = 665f, radius = 24f, background = "#FFFFFF"),
            elements = listOf(
                TemplateTextElement(id = "header", x = 128f, y = 128f, width = 768f, height = 50f, text = "", fontSize = 24f, align = Paint.Align.CENTER, color = "#111827", isBold = true),
                TemplateTextElement(id = "title", x = 128f, y = 192f, width = 768f, height = 100f, text = "Información", fontSize = 48f, align = Paint.Align.CENTER, color = "#111827", isBold = true),
                TemplateTextElement(id = "subtext", x = 128f, y = 1100f, width = 768f, height = 80f, text = "Escanea el código", fontSize = 28f, align = Paint.Align.CENTER, color = "#111827", isBold = false),
                TemplateTextElement(id = "footer", x = 128f, y = 1190f, width = 768f, height = 50f, text = "", fontSize = 22f, align = Paint.Align.CENTER, color = "#111827", isBold = false)
            )
        ),
        TemplateItemDef(
            id = "vip_gold",
            name = "VIP Gold",
            category = "Elegante",
            vectorResId = R.drawable.template_frame_vip_gold,
            primaryColor = Color.parseColor("#B8860B"),
            cardColor = Color.parseColor("#111111"),
            defaultText = "PREMIUM ACCESS",
            defaultSubtext = "Beneficios Exclusivos",
            defaultTextColor = Color.parseColor("#DAA520"),
            isDarkTheme = true,
            
            qrArea = TemplateQrArea(x = 179f, y = 435f, width = 665f, height = 665f, radius = 10f, background = "#FFFFFF"),
            elements = listOf(
                TemplateTextElement(id = "header", x = 128f, y = 128f, width = 768f, height = 50f, text = "", fontSize = 24f, align = Paint.Align.CENTER, color = "#DAA520", isBold = true),
                TemplateTextElement(id = "title", x = 128f, y = 192f, width = 768f, height = 100f, text = "PREMIUM ACCESS", fontSize = 48f, align = Paint.Align.CENTER, color = "#DAA520", isBold = true),
                TemplateTextElement(id = "subtext", x = 128f, y = 1141f, width = 768f, height = 80f, text = "Beneficios Exclusivos", fontSize = 28f, align = Paint.Align.CENTER, color = "#DAA520", isBold = false),
                TemplateTextElement(id = "footer", x = 128f, y = 1190f, width = 768f, height = 50f, text = "", fontSize = 22f, align = Paint.Align.CENTER, color = "#DAA520", isBold = false)
            )
        ),
        TemplateItemDef(
            id = "cafe_menu",
            name = "Menú Café",
            category = "Gastronomía",
            vectorResId = R.drawable.template_frame_cafe,
            primaryColor = Color.parseColor("#8D6E63"),
            cardColor = Color.parseColor("#FFF3E0"),
            defaultText = "NUESTRO MENÚ",
            defaultSubtext = "Escanea para ordenar",
            defaultTextColor = Color.parseColor("#3E2723"),
            isDarkTheme = false,
            
            qrArea = TemplateQrArea(x = 179f, y = 400f, width = 665f, height = 665f, radius = 24f, background = "#FFFFFF"),
            elements = listOf(
                TemplateTextElement(id = "header", x = 128f, y = 128f, width = 768f, height = 50f, text = "", fontSize = 24f, align = Paint.Align.CENTER, color = "#3E2723", isBold = true),
                TemplateTextElement(id = "title", x = 128f, y = 217f, width = 768f, height = 100f, text = "NUESTRO MENÚ", fontSize = 48f, align = Paint.Align.CENTER, color = "#3E2723", isBold = true),
                TemplateTextElement(id = "subtext", x = 128f, y = 1158f, width = 768f, height = 80f, text = "Escanea para ordenar", fontSize = 28f, align = Paint.Align.CENTER, color = "#3E2723", isBold = false),
                TemplateTextElement(id = "footer", x = 128f, y = 1190f, width = 768f, height = 50f, text = "", fontSize = 22f, align = Paint.Align.CENTER, color = "#3E2723", isBold = false)
            )
        ),
        TemplateItemDef(
            id = "tech_blue",
            name = "Tech Blue",
            category = "Tecnología",
            vectorResId = R.drawable.template_frame_tech,
            primaryColor = Color.parseColor("#0284C7"),
            cardColor = Color.parseColor("#0F172A"),
            defaultText = "WIFI INVITADOS",
            defaultSubtext = "Conexión Segura",
            defaultTextColor = Color.parseColor("#38BDF8"),
            isDarkTheme = true,
            
            qrArea = TemplateQrArea(x = 179f, y = 360f, width = 665f, height = 665f, radius = 16f, background = "#FFFFFF"),
            elements = listOf(
                TemplateTextElement(id = "header", x = 128f, y = 128f, width = 768f, height = 50f, text = "", fontSize = 24f, align = Paint.Align.CENTER, color = "#38BDF8", isBold = true),
                TemplateTextElement(id = "title", x = 128f, y = 198f, width = 768f, height = 100f, text = "WIFI INVITADOS", fontSize = 48f, align = Paint.Align.CENTER, color = "#38BDF8", isBold = true),
                TemplateTextElement(id = "subtext", x = 128f, y = 1184f, width = 768f, height = 80f, text = "Conexión Segura", fontSize = 28f, align = Paint.Align.CENTER, color = "#38BDF8", isBold = false),
                TemplateTextElement(id = "footer", x = 128f, y = 1190f, width = 768f, height = 50f, text = "", fontSize = 22f, align = Paint.Align.CENTER, color = "#38BDF8", isBold = false)
            )
        ),
        TemplateItemDef(
            id = "eco_nature",
            name = "Eco Nature",
            category = "Natural",
            vectorResId = R.drawable.template_frame_eco,
            primaryColor = Color.parseColor("#4CAF50"),
            cardColor = Color.parseColor("#E8F5E9"),
            defaultText = "SOSTENIBILIDAD",
            defaultSubtext = "Conoce más aquí",
            defaultTextColor = Color.parseColor("#1B5E20"),
            isDarkTheme = false,
            
            qrArea = TemplateQrArea(x = 179f, y = 400f, width = 665f, height = 665f, radius = 40f, background = "#FFFFFF"),
            elements = listOf(
                TemplateTextElement(id = "header", x = 128f, y = 128f, width = 768f, height = 50f, text = "", fontSize = 24f, align = Paint.Align.CENTER, color = "#1B5E20", isBold = true),
                TemplateTextElement(id = "title", x = 128f, y = 256f, width = 768f, height = 100f, text = "SOSTENIBILIDAD", fontSize = 48f, align = Paint.Align.CENTER, color = "#1B5E20", isBold = true),
                TemplateTextElement(id = "subtext", x = 128f, y = 1209f, width = 768f, height = 80f, text = "Conoce más aquí", fontSize = 28f, align = Paint.Align.CENTER, color = "#1B5E20", isBold = false),
                TemplateTextElement(id = "footer", x = 128f, y = 1190f, width = 768f, height = 50f, text = "", fontSize = 22f, align = Paint.Align.CENTER, color = "#1B5E20", isBold = false)
            )
        ),
        TemplateItemDef(
            id = "romantic_love",
            name = "Romantic",
            category = "Eventos",
            vectorResId = R.drawable.template_frame_love,
            primaryColor = Color.parseColor("#E91E63"),
            cardColor = Color.parseColor("#FCE4EC"),
            defaultText = "NUESTRA BODA",
            defaultSubtext = "Confirma tu asistencia",
            defaultTextColor = Color.parseColor("#880E4F"),
            isDarkTheme = false,
            
            qrArea = TemplateQrArea(x = 179f, y = 400f, width = 665f, height = 665f, radius = 24f, background = "#FFFFFF"),
            elements = listOf(
                TemplateTextElement(id = "header", x = 128f, y = 128f, width = 768f, height = 50f, text = "", fontSize = 24f, align = Paint.Align.CENTER, color = "#880E4F", isBold = true),
                TemplateTextElement(id = "title", x = 128f, y = 262f, width = 768f, height = 100f, text = "NUESTRA BODA", fontSize = 48f, align = Paint.Align.CENTER, color = "#880E4F", isBold = true),
                TemplateTextElement(id = "subtext", x = 128f, y = 1145f, width = 768f, height = 80f, text = "Confirma tu asistencia", fontSize = 28f, align = Paint.Align.CENTER, color = "#880E4F", isBold = false),
                TemplateTextElement(id = "footer", x = 128f, y = 1190f, width = 768f, height = 50f, text = "", fontSize = 22f, align = Paint.Align.CENTER, color = "#880E4F", isBold = false)
            )
        ),
        TemplateItemDef(
            id = "party_event",
            name = "Party Event",
            category = "Eventos",
            vectorResId = R.drawable.template_frame_party,
            primaryColor = Color.parseColor("#9C27B0"),
            cardColor = Color.parseColor("#311B92"),
            defaultText = "FIESTA NEÓN",
            defaultSubtext = "Escanea para entrar",
            defaultTextColor = Color.parseColor("#E040FB"),
            isDarkTheme = true,
            
            qrArea = TemplateQrArea(x = 179f, y = 400f, width = 665f, height = 665f, radius = 16f, background = "#FFFFFF"),
            elements = listOf(
                TemplateTextElement(id = "header", x = 128f, y = 128f, width = 768f, height = 50f, text = "", fontSize = 24f, align = Paint.Align.CENTER, color = "#E040FB", isBold = true),
                TemplateTextElement(id = "title", x = 128f, y = 256f, width = 768f, height = 100f, text = "FIESTA NEÓN", fontSize = 48f, align = Paint.Align.CENTER, color = "#E040FB", isBold = true),
                TemplateTextElement(id = "subtext", x = 128f, y = 1171f, width = 768f, height = 80f, text = "Escanea para entrar", fontSize = 28f, align = Paint.Align.CENTER, color = "#E040FB", isBold = false),
                TemplateTextElement(id = "footer", x = 128f, y = 1190f, width = 768f, height = 50f, text = "", fontSize = 22f, align = Paint.Align.CENTER, color = "#E040FB", isBold = false)
            )
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
        val density = context.resources.displayMetrics.density

        // 1. Draw Background
        if (custom.templateDef.background.type == "vector") {
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

        // 2. Generate and Draw QR Code
        val qrArea = custom.templateDef.qrArea
        val qrWidth = (qrArea.width * scaleX).toInt()
        val qrHeight = (qrArea.height * scaleY).toInt()
        
        // Optional Background for QR area
        if (qrArea.background != null) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor(qrArea.background) }
            val rectF = RectF(
                qrArea.x * scaleX, 
                qrArea.y * scaleY, 
                (qrArea.x + qrArea.width) * scaleX, 
                (qrArea.y + qrArea.height) * scaleY
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
                logoBitmap = custom.logoBitmap,
                logoSizeRatio = custom.logoSizeMultiplier
            )
        } catch (e: Exception) {
            null
        }

        if (qrBitmap != null) {
            canvas.drawBitmap(qrBitmap, qrArea.x * scaleX, qrArea.y * scaleY, null)
        }

        // 3. Render Parameterized Elements
        // We override text properties with user input from TemplateCustomization 
        // to maintain backward compatibility with the UI.
        
        val overridenElements = custom.templateDef.elements.map { el ->
            if (el is TemplateTextElement) {
                var newText = el.text
                var newColor = el.color
                var newAlign = el.align
                var newSize = el.fontSize
                
                // Inject UI Overrides dynamically based on element ID
                when (el.id) {
                    "header" -> {
                        newText = custom.headerAccentText ?: custom.templateDef.defaultHeaderAccent ?: ""
                        newColor = String.format("#%06X", 0xFFFFFF and custom.textColor)
                        newSize = custom.textSizeSp * 0.5f * (1024f / 512f)
                    }
                    "title" -> {
                        newText = custom.titleText
                        newColor = String.format("#%06X", 0xFFFFFF and custom.textColor)
                        newAlign = custom.textAlign
                        newSize = custom.textSizeSp * (1024f / 512f)
                    }
                    "subtext" -> {
                        newText = custom.subtext
                        
                        // Preserve exact template specific hardcoded colors from old logic
                        if (custom.templateDef.id == "navy_geo") {
                            newColor = "#4ADE80"
                            newSize = (custom.subtextSizeSp * 1.1f) * (1024f / 512f)
                        } else if (custom.templateDef.id == "escanea_aqui") {
                            newColor = "#D4062A"
                            newSize = (custom.subtextSizeSp * 1.2f) * (1024f / 512f)
                        } else {
                            newColor = String.format("#%06X", 0xFFFFFF and custom.subtextColor)
                            newSize = custom.subtextSizeSp * (1024f / 512f)
                        }
                    }
                    "footer" -> {
                        newText = custom.footerAccentText ?: custom.templateDef.defaultFooterAccent ?: ""
                        newColor = String.format("#%06X", 0xFFFFFF and custom.subtextColor)
                        newSize = custom.subtextSizeSp * 0.8f * (1024f / 512f)
                    }
                }
                
                el.copy(text = newText, color = newColor, align = newAlign, fontSize = newSize)
            } else {
                el
            }
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        for (el in overridenElements) {
            if (el is TemplateTextElement && el.text.isNotBlank()) {
                textPaint.color = Color.parseColor(el.color)
                textPaint.textSize = el.fontSize * scaleX
                textPaint.textAlign = el.align
                textPaint.typeface = if (el.isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                if (el.id == "header" || el.id == "footer") {
                    textPaint.alpha = 200
                } else {
                    textPaint.alpha = 255
                }
                
                // Adjust offsets
                var adjustedX = el.x * scaleX
                var adjustedY = el.y * scaleY
                
                if (el.id == "title" || el.id == "header") {
                    adjustedX += custom.textOffsetX * scaleX * 2f
                    adjustedY += custom.textOffsetY * scaleY * 2f
                    
                    // Specific title X alignment logic based on old behavior
                    if (el.id == "title") {
                        val baseTitleX = if (custom.templateDef.id == "escanea_aqui") {
                            when (el.align) {
                                Paint.Align.LEFT -> targetWidth * 0.255f
                                Paint.Align.RIGHT -> targetWidth * 0.86f
                                Paint.Align.CENTER -> targetWidth * 0.56f
                            }
                        } else {
                            when (el.align) {
                                Paint.Align.LEFT -> targetWidth * 0.12f
                                Paint.Align.RIGHT -> targetWidth * 0.88f
                                Paint.Align.CENTER -> targetWidth * 0.5f
                            }
                        }
                        adjustedX = baseTitleX + (custom.textOffsetX * scaleX * 2f)
                    } else if (el.id == "header") {
                        adjustedX = targetWidth / 2f
                    }
                }
                
                if (el.id == "subtext" || el.id == "footer") {
                    adjustedY += (custom.textOffsetY * 0.5f) * scaleY * 2f
                    adjustedX = targetWidth / 2f
                }

                val maxTextWidth = targetWidth * 0.85f
                fitTextSize(textPaint, el.text, maxTextWidth)
                drawFormattedText(canvas, el.text, adjustedX, adjustedY, textPaint, maxTextWidth)
            }
        }

        return result
    }

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
    f.write(new_kt)

print("TemplateRenderer.kt replaced with parameterized architecture.")
