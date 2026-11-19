package com.example.utils

import android.graphics.*

/**
 * Draws bespoke, high-contrast Canvas artwork for each built-in QR template
 * on a logical 1024 x 1280 coordinate space scaled to targetWidth x targetHeight.
 */
object TemplateArtwork {

    fun drawTemplateBackground(
        canvas: Canvas,
        templateId: String,
        targetWidth: Int,
        targetHeight: Int,
        fallbackColor: Int
    ) {
        val sx = targetWidth / 1024f
        val sy = targetHeight / 1280f
        when (templateId) {
            "bento_studio" -> drawBentoStudio(canvas, sx, sy, targetWidth, targetHeight)
            "executive_obsidian" -> drawExecutiveObsidian(canvas, sx, sy, targetWidth, targetHeight)
            "express_checkout" -> drawExpressCheckout(canvas, sx, sy, targetWidth, targetHeight)
            "boutique_tag" -> drawBoutiqueTag(canvas, sx, sy, targetWidth, targetHeight)
            "bistro_chalkboard" -> drawBistroChalkboard(canvas, sx, sy, targetWidth, targetHeight)
            "botanical_wifi" -> drawBotanicalWifi(canvas, sx, sy, targetWidth, targetHeight)
            "vip_ticket" -> drawVipTicket(canvas, sx, sy, targetWidth, targetHeight)
            "gala_arch" -> drawGalaArch(canvas, sx, sy, targetWidth, targetHeight)
            "neon_cyber" -> drawNeonCyber(canvas, sx, sy, targetWidth, targetHeight)
            "creator_pop" -> drawCreatorPop(canvas, sx, sy, targetWidth, targetHeight)
            "polaroid_snap" -> drawPolaroidSnap(canvas, sx, sy, targetWidth, targetHeight)
            "swiss_poster" -> drawSwissPoster(canvas, sx, sy, targetWidth, targetHeight)
            else -> canvas.drawColor(fallbackColor)
        }
    }

    private fun drawBentoStudio(canvas: Canvas, sx: Float, sy: Float, w: Int, h: Int) {
        canvas.drawColor(Color.parseColor("#F6F4EF"))

        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6E1D6")
            strokeWidth = 2f * sx
        }
        for (gx in listOf(72f, 952f)) {
            canvas.drawLine(gx * sx, 0f, gx * sx, h.toFloat(), gridPaint)
        }
        for (gy in listOf(280f, 1070f)) {
            canvas.drawLine(0f, gy * sy, w.toFloat(), gy * sy, gridPaint)
        }

        // Accent emerald bar top-left
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0D9488")
        }
        canvas.drawRoundRect(
            RectF(72f * sx, 64f * sy, 196f * sx, 80f * sy),
            8f * sx, 8f * sy, accentPaint
        )

        // Small geometric dots top-right
        accentPaint.alpha = 55
        canvas.drawCircle(900f * sx, 110f * sy, 42f * sx, accentPaint)
        accentPaint.alpha = 255
        canvas.drawCircle(930f * sx, 138f * sy, 14f * sx, accentPaint)

        // Offset brutalist-light shadow behind QR card
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D6D3CD")
        }
        canvas.drawRoundRect(
            RectF(158f * sx, 326f * sy, 898f * sx, 1066f * sy),
            36f * sx, 36f * sy, shadowPaint
        )

        // Main white QR card
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        canvas.drawRoundRect(
            RectF(142f * sx, 310f * sy, 882f * sx, 1050f * sy),
            36f * sx, 36f * sy, cardPaint
        )

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f * sx
            color = Color.parseColor("#18181B")
        }
        canvas.drawRoundRect(
            RectF(142f * sx, 310f * sy, 882f * sx, 1050f * sy),
            36f * sx, 36f * sy, borderPaint
        )
    }

    private fun drawExecutiveObsidian(canvas: Canvas, sx: Float, sy: Float, w: Int, h: Int) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(Color.parseColor("#090D16"), Color.parseColor("#162033")),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Subtle diagonal lines
        val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 12
            strokeWidth = 2f * sx
        }
        var offset = -h.toFloat()
        while (offset < w + h) {
            canvas.drawLine(offset, 0f, offset + h * 0.6f, h.toFloat(), stripePaint)
            offset += 64f * sx
        }

        // Champagne gold double frame
        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#EAB308")
            strokeWidth = 3.5f * sx
        }
        canvas.drawRoundRect(
            RectF(48f * sx, 48f * sy, 976f * sx, 1232f * sy),
            28f * sx, 28f * sy, goldPaint
        )
        goldPaint.strokeWidth = 1.5f * sx
        goldPaint.alpha = 140
        canvas.drawRoundRect(
            RectF(64f * sx, 64f * sy, 960f * sx, 1216f * sy),
            20f * sx, 20f * sy, goldPaint
        )

        // White QR pad with gold border
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(
            RectF(156f * sx, 315f * sy, 868f * sx, 1027f * sy),
            32f * sx, 32f * sy, whitePaint
        )
        goldPaint.alpha = 255
        goldPaint.strokeWidth = 3f * sx
        canvas.drawRoundRect(
            RectF(156f * sx, 315f * sy, 868f * sx, 1027f * sy),
            32f * sx, 32f * sy, goldPaint
        )
    }

    private fun drawExpressCheckout(canvas: Canvas, sx: Float, sy: Float, w: Int, h: Int) {
        canvas.drawColor(Color.parseColor("#F8FAFC"))

        // Curved cobalt top banner
        val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), 320f * sy,
                Color.parseColor("#1E3A8A"),
                Color.parseColor("#2563EB"),
                Shader.TileMode.CLAMP
            )
        }
        val wavePath = Path().apply {
            moveTo(0f, 0f)
            lineTo(w.toFloat(), 0f)
            lineTo(w.toFloat(), 275f * sy)
            quadTo(512f * sx, 355f * sy, 0f, 275f * sy)
            close()
        }
        canvas.drawPath(wavePath, bannerPaint)

        // Soft shadow + white QR card
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            alpha = 160
        }
        canvas.drawRoundRect(
            RectF(152f * sx, 352f * sy, 872f * sx, 1072f * sy),
            40f * sx, 40f * sy, shadowPaint
        )
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(
            RectF(152f * sx, 340f * sy, 872f * sx, 1060f * sy),
            40f * sx, 40f * sy, whitePaint
        )

        // Cobalt viewfinder corner brackets
        val bracketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 10f * sx
            strokeCap = Paint.Cap.ROUND
            color = Color.parseColor("#2563EB")
        }
        drawCornerBrackets(canvas, 132f * sx, 320f * sy, 892f * sx, 1080f * sy, 56f * sx, bracketPaint)

        // Bottom pill accent
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DBEAFE")
        }
        canvas.drawRoundRect(
            RectF(212f * sx, 1185f * sy, 812f * sx, 1236f * sy),
            26f * sx, 26f * sy, pillPaint
        )
    }

    private fun drawBoutiqueTag(canvas: Canvas, sx: Float, sy: Float, w: Int, h: Int) {
        canvas.drawColor(Color.parseColor("#F1EAE0"))

        val tagPath = Path().apply {
            moveTo(240f * sx, 68f * sy)
            lineTo(784f * sx, 68f * sy)
            lineTo(924f * sx, 208f * sy)
            lineTo(924f * sx, 1212f * sy)
            lineTo(100f * sx, 1212f * sy)
            lineTo(100f * sx, 208f * sy)
            close()
        }

        // Soft tag shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D6CCC0")
        }
        canvas.save()
        canvas.translate(10f * sx, 14f * sy)
        canvas.drawPath(tagPath, shadowPaint)
        canvas.restore()

        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawPath(tagPath, tagPaint)

        // Inner dashed terracotta stitching
        val innerPath = Path().apply {
            moveTo(256f * sx, 94f * sy)
            lineTo(768f * sx, 94f * sy)
            lineTo(898f * sx, 224f * sy)
            lineTo(898f * sx, 1186f * sy)
            lineTo(126f * sx, 1186f * sy)
            lineTo(126f * sx, 224f * sy)
            close()
        }
        val stitchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.5f * sx
            color = Color.parseColor("#C2410C")
            pathEffect = DashPathEffect(floatArrayOf(14f * sx, 10f * sx), 0f)
        }
        canvas.drawPath(innerPath, stitchPaint)

        // Eyelet hole + brass grommet
        val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F1EAE0")
        }
        canvas.drawCircle(512f * sx, 134f * sy, 24f * sx, holePaint)
        val grommetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 7f * sx
            color = Color.parseColor("#B45309")
        }
        canvas.drawCircle(512f * sx, 134f * sy, 26f * sx, grommetPaint)
    }

    private fun drawBistroChalkboard(canvas: Canvas, sx: Float, sy: Float, w: Int, h: Int) {
        val bgPaint = Paint().apply {
            shader = RadialGradient(
                w * 0.5f, h * 0.45f, h * 0.7f,
                Color.parseColor("#26332D"),
                Color.parseColor("#141B18"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Outer chalk border
        val chalkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#E7E2D8")
            alpha = 200
            strokeWidth = 3.5f * sx
        }
        canvas.drawRoundRect(
            RectF(44f * sx, 44f * sy, 980f * sx, 1236f * sy),
            24f * sx, 24f * sy, chalkPaint
        )

        // Inner dashed chalk border
        chalkPaint.strokeWidth = 2f * sx
        chalkPaint.alpha = 130
        chalkPaint.pathEffect = DashPathEffect(floatArrayOf(18f * sx, 12f * sx), 0f)
        canvas.drawRoundRect(
            RectF(62f * sx, 62f * sy, 962f * sx, 1218f * sy),
            16f * sx, 16f * sy, chalkPaint
        )

        // Ornamental diamond divider above QR
        val ornamentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D97706")
            strokeWidth = 2.5f * sx
        }
        canvas.drawLine(230f * sx, 286f * sy, 472f * sx, 286f * sy, ornamentPaint)
        canvas.drawLine(552f * sx, 286f * sy, 794f * sx, 286f * sy, ornamentPaint)
        drawDiamond(canvas, 512f * sx, 286f * sy, 14f * sx, ornamentPaint)

        // Warm cream QR frame
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FAF8F5")
        }
        canvas.drawRoundRect(
            RectF(162f * sx, 325f * sy, 862f * sx, 1025f * sy),
            32f * sx, 32f * sy, cardPaint
        )
    }

    private fun drawBotanicalWifi(canvas: Canvas, sx: Float, sy: Float, w: Int, h: Int) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                Color.parseColor("#ECFDF5"),
                Color.parseColor("#D1FAE5"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Concentric translucent Wi-Fi / signal arcs at top center
        val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#059669")
            alpha = 34
            strokeWidth = 14f * sx
            strokeCap = Paint.Cap.ROUND
        }
        for (radius in listOf(120f, 200f, 280f)) {
            val r = radius * sx
            canvas.drawArc(
                RectF(512f * sx - r, 110f * sy - r, 512f * sx + r, 110f * sy + r),
                205f, 130f, false, arcPaint
            )
        }

        // Soft organic circles in corners
        val blobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#10B981")
            alpha = 26
        }
        canvas.drawCircle(90f * sx, 1190f * sy, 150f * sx, blobPaint)
        canvas.drawCircle(934f * sx, 1060f * sy, 110f * sx, blobPaint)

        // White QR container with forest green border
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(
            RectF(152f * sx, 320f * sy, 872f * sx, 1040f * sy),
            44f * sx, 44f * sy, whitePaint
        )
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f * sx
            color = Color.parseColor("#065F46")
        }
        canvas.drawRoundRect(
            RectF(152f * sx, 320f * sy, 872f * sx, 1040f * sy),
            44f * sx, 44f * sy, borderPaint
        )
    }

    private fun drawVipTicket(canvas: Canvas, sx: Float, sy: Float, w: Int, h: Int) {
        val backdropColor = Color.parseColor("#120924")
        canvas.drawColor(backdropColor)

        // Main ticket body
        val ticketRect = RectF(84f * sx, 60f * sy, 940f * sx, 1220f * sy)
        val ticketPath = Path().apply {
            addRoundRect(ticketRect, 44f * sx, 44f * sy, Path.Direction.CW)
        }
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawPath(ticketPath, whitePaint)

        // Gradient top header clipped inside ticket
        canvas.save()
        canvas.clipPath(ticketPath)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                84f * sx, 60f * sy, 940f * sx, 285f * sy,
                Color.parseColor("#6D28D9"),
                Color.parseColor("#DB2777"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(84f * sx, 60f * sy, 940f * sx, 285f * sy, headerPaint)
        canvas.restore()

        // Semicircular punch-out notches on both sides at y = 1005
        val notchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = backdropColor }
        canvas.drawCircle(84f * sx, 1005f * sy, 36f * sx, notchPaint)
        canvas.drawCircle(940f * sx, 1005f * sy, 36f * sx, notchPaint)

        // Dashed tear perforation line
        val tearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f * sx
            color = Color.parseColor("#CBD5E1")
            pathEffect = DashPathEffect(floatArrayOf(18f * sx, 14f * sx), 0f)
        }
        canvas.drawLine(136f * sx, 1005f * sy, 888f * sx, 1005f * sy, tearPaint)
    }

    private fun drawGalaArch(canvas: Canvas, sx: Float, sy: Float, w: Int, h: Int) {
        canvas.drawColor(Color.parseColor("#FDFBF7"))

        // Soft champagne radial wash at top
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                512f * sx, 260f * sy, 480f * sx,
                Color.parseColor("#FCE7F3"),
                Color.parseColor("#FDFBF7"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), glowPaint)

        // Outer architectural arch frame
        val archPath = Path().apply {
            moveTo(76f * sx, 1210f * sy)
            lineTo(76f * sx, 500f * sy)
            arcTo(RectF(76f * sx, 64f * sy, 948f * sx, 936f * sy), 180f, 180f, false)
            lineTo(948f * sx, 1210f * sy)
            close()
        }
        val burgundyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4.5f * sx
            color = Color.parseColor("#881337")
        }
        canvas.drawPath(archPath, burgundyPaint)

        // Inner thin gold arch
        val innerArchPath = Path().apply {
            moveTo(96f * sx, 1190f * sy)
            lineTo(96f * sx, 500f * sy)
            arcTo(RectF(96f * sx, 84f * sy, 928f * sx, 916f * sy), 180f, 180f, false)
            lineTo(928f * sx, 1190f * sy)
            close()
        }
        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f * sx
            color = Color.parseColor("#D97706")
        }
        canvas.drawPath(innerArchPath, goldPaint)

        // Gold diamond star at apex
        val goldFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#D97706") }
        drawDiamond(canvas, 512f * sx, 74f * sy, 16f * sx, goldFill)

        // White QR medallion
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(
            RectF(176f * sx, 345f * sy, 848f * sx, 1017f * sy),
            36f * sx, 36f * sy, whitePaint
        )
        goldPaint.alpha = 140
        canvas.drawRoundRect(
            RectF(176f * sx, 345f * sy, 848f * sx, 1017f * sy),
            36f * sx, 36f * sy, goldPaint
        )
    }

    private fun drawNeonCyber(canvas: Canvas, sx: Float, sy: Float, w: Int, h: Int) {
        canvas.drawColor(Color.parseColor("#070A12"))

        // Cyber tech grid
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#06B6D4")
            alpha = 26
            strokeWidth = 1.5f * sx
        }
        var x = 64f * sx
        while (x < w) {
            canvas.drawLine(x, 0f, x, h.toFloat(), gridPaint)
            x += 80f * sx
        }
        var y = 64f * sy
        while (y < h) {
            canvas.drawLine(0f, y, w.toFloat(), y, gridPaint)
            y += 80f * sy
        }

        // Glow halo behind QR pad
        val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                512f * sx, 680f * sy, 460f * sx,
                Color.parseColor("#44A855F7"),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(512f * sx, 680f * sy, 460f * sx, haloPaint)

        // Crisp bright white QR pad
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(
            RectF(162f * sx, 330f * sy, 862f * sx, 1030f * sy),
            24f * sx, 24f * sy, whitePaint
        )

        // Cyan and Magenta HUD corner brackets
        val cyanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 8f * sx
            strokeCap = Paint.Cap.SQUARE
            color = Color.parseColor("#06B6D4")
        }
        drawCornerBrackets(canvas, 136f * sx, 304f * sy, 888f * sx, 1056f * sy, 68f * sx, cyanPaint)

        val magentaLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D946EF")
            strokeWidth = 4f * sx
        }
        canvas.drawLine(240f * sx, 266f * sy, 784f * sx, 266f * sy, magentaLine)
    }

    private fun drawCreatorPop(canvas: Canvas, sx: Float, sy: Float, w: Int, h: Int) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(
                    Color.parseColor("#FF512F"),
                    Color.parseColor("#DD2476"),
                    Color.parseColor("#6D28D9")
                ),
                floatArrayOf(0f, 0.52f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Translucent frosted spheres
        val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 32
        }
        canvas.drawCircle(140f * sx, 180f * sy, 210f * sx, glassPaint)
        canvas.drawCircle(900f * sx, 1120f * sy, 240f * sx, glassPaint)

        // Drop shadow + crisp white QR card
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            alpha = 45
        }
        canvas.drawRoundRect(
            RectF(152f * sx, 336f * sy, 872f * sx, 1056f * sy),
            48f * sx, 48f * sy, shadowPaint
        )
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(
            RectF(152f * sx, 320f * sy, 872f * sx, 1040f * sy),
            48f * sx, 48f * sy, whitePaint
        )
    }

    private fun drawPolaroidSnap(canvas: Canvas, sx: Float, sy: Float, w: Int, h: Int) {
        canvas.drawColor(Color.parseColor("#E6E1D8"))

        // Soft drop shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C7C0B5")
        }
        canvas.drawRoundRect(
            RectF(100f * sx, 90f * sy, 948f * sx, 1218f * sy),
            16f * sx, 16f * sy, shadowPaint
        )

        // Crisp white Polaroid paper frame
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(
            RectF(88f * sx, 76f * sy, 936f * sx, 1204f * sy),
            16f * sx, 16f * sy, framePaint
        )

        // Subtle recessed photo well border
        val wellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f * sx
            color = Color.parseColor("#E2E8F0")
        }
        canvas.drawRect(156f * sx, 140f * sy, 868f * sx, 852f * sy, wellPaint)

        // Decorative sky-blue masking tape strip at top center
        val tapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
            alpha = 215
        }
        canvas.drawRect(402f * sx, 42f * sy, 622f * sx, 102f * sy, tapePaint)
    }

    private fun drawSwissPoster(canvas: Canvas, sx: Float, sy: Float, w: Int, h: Int) {
        canvas.drawColor(Color.parseColor("#F4EFE6"))

        // Bold vermilion Bauhaus circle top-right
        val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DC2626")
        }
        canvas.drawCircle(846f * sx, 144f * sy, 96f * sx, redPaint)

        // Sharp black architectural rules
        val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111827")
            strokeWidth = 6f * sx
        }
        canvas.drawLine(72f * sx, 286f * sy, 952f * sx, 286f * sy, rulePaint)
        canvas.drawLine(72f * sx, 1068f * sy, 952f * sx, 1068f * sy, rulePaint)

        // Clean white QR square with bold black border
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRect(162f * sx, 326f * sy, 862f * sx, 1026f * sy, whitePaint)
        val boxBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f * sx
            color = Color.parseColor("#111827")
        }
        canvas.drawRect(162f * sx, 326f * sy, 862f * sx, 1026f * sy, boxBorder)
    }

    private fun drawCornerBrackets(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        len: Float,
        paint: Paint
    ) {
        // Top-left
        canvas.drawLine(left, top + len, left, top, paint)
        canvas.drawLine(left, top, left + len, top, paint)
        // Top-right
        canvas.drawLine(right - len, top, right, top, paint)
        canvas.drawLine(right, top, right, top + len, paint)
        // Bottom-left
        canvas.drawLine(left, bottom - len, left, bottom, paint)
        canvas.drawLine(left, bottom, left + len, bottom, paint)
        // Bottom-right
        canvas.drawLine(right - len, bottom, right, bottom, paint)
        canvas.drawLine(right, bottom, right, bottom - len, paint)
    }

    private fun drawDiamond(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val path = Path().apply {
            moveTo(cx, cy - radius)
            lineTo(cx + radius, cy)
            lineTo(cx, cy + radius)
            lineTo(cx - radius, cy)
            close()
        }
        canvas.drawPath(path, paint)
    }
}
