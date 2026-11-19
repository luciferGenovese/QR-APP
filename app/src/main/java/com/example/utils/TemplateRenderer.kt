package com.example.utils

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.example.R
import com.google.zxing.BarcodeFormat

// --- PARAMETERIZED ARCHITECTURE ---

data class TemplateCanvas(val width: Float = 1024f, val height: Float = 1280f)

data class TemplateBackground(
    val type: String = "vector", // "solid", "vector", "bitmap"
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
    val qrBackgroundColor: Int = Color.WHITE,
    val customFrameBitmap: Bitmap? = null // Hybrid Custom PNG/JPG Frame/Background
)

object TemplateRenderer {
    private fun themedTemplate(
        id: String, name: String, category: String, background: String, accent: String,
        title: String, subtitle: String
    ): TemplateItemDef {
        val ink = Color.parseColor("#182720")
        return TemplateItemDef(
            id = id, name = name, category = category,
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor(accent),
            cardColor = Color.parseColor(background),
            defaultText = title, defaultSubtext = subtitle,
            defaultTextColor = ink,
            background = TemplateBackground(type = "themed", color = background),
            qrArea = TemplateQrArea(x = 182f, y = 365f, width = 660f, height = 660f, radius = 28f, background = "#FFFFFF"),
            elements = listOf(
                TemplateTextElement("header", 512f, 118f, 800f, 50f, "", 24f),
                TemplateTextElement("title", 512f, 240f, 820f, 100f, title, 56f, isBold = true),
                TemplateTextElement("subtext", 512f, 1124f, 820f, 70f, subtitle, 28f),
                TemplateTextElement("footer", 512f, 1215f, 820f, 40f, "", 22f)
            )
        )
    }

    val AVAILABLE_TEMPLATES = listOf(
        themedTemplate("business_contact", "Tarjeta de contacto", "Negocios", "#E9F3EC", "#1B6A50", "CONTACTO", "Escanea para guardar mis datos"),
        themedTemplate("business_website", "Sitio web", "Negocios", "#E8EFF7", "#315C7A", "VISITA NUESTRA WEB", "Descubre más sobre nosotros"),
        themedTemplate("shop_payment", "Pago", "Comercio", "#F5EEE2", "#95692D", "PAGA AQUÍ", "Escanea para pagar"),
        themedTemplate("shop_catalog", "Catálogo", "Comercio", "#F3E8E5", "#9D5C49", "NUESTROS PRODUCTOS", "Explora nuestro catálogo"),
        themedTemplate("food_menu", "Menú", "Gastronomía", "#F5EDDF", "#9A6542", "NUESTRO MENÚ", "Escanea para ver la carta"),
        themedTemplate("food_wifi", "Wi-Fi del local", "Gastronomía", "#E7F1E5", "#4E7955", "CONÉCTATE", "Escanea para acceder al Wi-Fi"),
        themedTemplate("event_invitation", "Invitación", "Eventos", "#F5E9EC", "#A05C74", "ESTÁS INVITADO", "Escanea para ver los detalles"),
        themedTemplate("event_tickets", "Entrada", "Eventos", "#ECEAF7", "#68599A", "TU ENTRADA", "Escanea para acceder"),
        themedTemplate("social_profile", "Perfil social", "Redes sociales", "#EAEAF7", "#5C5C9A", "SÍGUENOS", "Encuéntranos en redes"),
        themedTemplate("social_share", "Compartir", "Redes sociales", "#E5F1F2", "#367E83", "COMPARTAMOS", "Escanea para conectar"),
        themedTemplate("personal_info", "Información", "Personal", "#F1F1EC", "#687767", "ESCANEA AQUÍ", "Toda la información en un lugar"),
        themedTemplate("personal_link", "Enlace personal", "Personal", "#F2EBDD", "#A2753A", "MI ENLACE", "Escanea para descubrir más")
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

        // 1. Draw Background (Hybrid Vector / Custom PNG image)
        if (custom.customFrameBitmap != null && !custom.customFrameBitmap.isRecycled) {
            // Draw custom uploaded PNG / JPG frame preserving aspect ratio without distortion or cropping
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
        } else if (custom.templateDef.background.type == "themed") {
            canvas.drawColor(custom.templateDef.cardColor)
            val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = custom.templateDef.primaryColor }
            canvas.drawRoundRect(
                RectF(70f * scaleX, 55f * scaleY, 160f * scaleX, 68f * scaleY),
                8f * scaleX, 8f * scaleY, accent
            )
            accent.alpha = 24
            canvas.drawCircle(935f * scaleX, 92f * scaleY, 110f * scaleX, accent)
            canvas.drawCircle(65f * scaleX, 1215f * scaleY, 95f * scaleX, accent)
            val white = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
            canvas.drawRoundRect(
                RectF(142f * scaleX, 325f * scaleY, 882f * scaleX, 1065f * scaleY),
                50f * scaleX, 50f * scaleY, white
            )
        } else if (custom.templateDef.background.type == "bitmap") {
            try {
                val bmp = BitmapFactory.decodeResource(context.resources, custom.templateDef.vectorResId)
                if (bmp != null) {
                    val destRect = RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat())
                    val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                    canvas.drawBitmap(bmp, null, destRect, bmpPaint)
                } else {
                    canvas.drawColor(custom.templateDef.primaryColor)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                canvas.drawColor(custom.templateDef.primaryColor)
            }
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
                        newSize = custom.textSizeSp * 2.5f
                    }
                    "title" -> {
                        newText = custom.titleText
                        newColor = String.format("#%06X", 0xFFFFFF and custom.textColor)
                        newAlign = custom.textAlign
                        newSize = custom.textSizeSp * 5.0f
                    }
                    "subtext" -> {
                        newText = custom.subtext
                        
                        // Preserve exact template specific hardcoded colors from old logic
                        if (custom.templateDef.id == "navy_geo") {
                            newColor = "#4ADE80"
                            newSize = custom.subtextSizeSp * 5.5f
                        } else if (custom.templateDef.id == "escanea_aqui") {
                            newColor = "#D4062A"
                            newSize = custom.subtextSizeSp * 6.0f
                        } else {
                            newColor = String.format("#%06X", 0xFFFFFF and custom.subtextColor)
                            newSize = custom.subtextSizeSp * 5.0f
                        }
                    }
                    "footer" -> {
                        newText = custom.footerAccentText ?: custom.templateDef.defaultFooterAccent ?: ""
                        newColor = String.format("#%06X", 0xFFFFFF and custom.subtextColor)
                        newSize = custom.subtextSizeSp * 4.0f
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
                
                // Base coordinates from the template
                var baseAnchorX = el.x
                
                // If the user changed the text alignment from the UI, we adapt the anchor point
                // Assuming el.x is the CENTER anchor when width is defined, we can shift left/right
                if (el.id == "title" || el.align != Paint.Align.CENTER) {
                    when (el.align) {
                        Paint.Align.LEFT -> baseAnchorX = el.x - (el.width / 2f) + (1024f * 0.12f)
                        Paint.Align.RIGHT -> baseAnchorX = el.x + (el.width / 2f) - (1024f * 0.12f)
                        Paint.Align.CENTER -> baseAnchorX = el.x
                    }
                }
                
                var adjustedX = baseAnchorX * scaleX
                var adjustedY = el.y * scaleY
                
                // Apply user UI offsets (sliders)
                if (el.id == "title" || el.id == "header") {
                    adjustedX += custom.textOffsetX * scaleX * 2f
                    adjustedY += custom.textOffsetY * scaleY * 2f
                } else if (el.id == "subtext" || el.id == "footer") {
                    // Subtext and footer move slightly slower
                    adjustedY += (custom.textOffsetY * 0.5f) * scaleY * 2f
                }

                val maxTextWidth = targetWidth * 0.85f
                fitTextSize(textPaint, el.text, maxTextWidth)
                drawFormattedText(canvas, el.text, adjustedX, adjustedY, textPaint, maxTextWidth)
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

    private fun drawFormattedText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint, maxWidth: Float) {
        val lines = text.split("\n")
        val fontSpacing = paint.fontSpacing
        var currentY = y
        for (line in lines) {
            canvas.drawText(line, x, currentY, paint)
            currentY += fontSpacing
        }
    }
}
