import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# Let's fix the TemplateCustomization data class properties to include missing properties that other files are relying on

new_data_class = """data class TemplateCustomization(
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
    val gradientAngle: Float = 0f,
    
    // Added missing properties back
    val qrBodyShape: String = "SQUARE",
    val qrLogoBitmap: Bitmap? = null,
    val qrErrorCorrectionLevel: String = "Q",
    val qrLogoSizeRatio: Float = 0.2f,
    val qrLogoBadgeShape: String = "SQUARE",
    val isBold: Boolean = true,
    val isUppercase: Boolean = true,
    val showSubtext: Boolean = true,
    val qrScale: Float = 1.0f,
    val qrOffsetY: Float = 0f
)"""

old_data_class = """data class TemplateCustomization(
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
)"""

text = text.replace(old_data_class, new_data_class)

# The SmartQrFeatures is unresolved because it's not imported or defined in TemplateRenderer.kt
# Looking at the original file (before my syntax replacement), SmartQrFeatures generation was commented out or different. Let's fix that.
# Let's replace the QR generation block with the simpler original block

new_qr_block = """        val qrBitmap = try {
            com.example.utils.SmartQrFeatures.generateCustomQrCode(
                content = custom.qrContent,
                width = qrSize,
                height = qrSize,
                foregroundColor = custom.qrForegroundColor,
                backgroundColor = custom.qrBackgroundColor,
                shape = custom.qrBodyShape,
                eyeShape = custom.qrEyeStyle,
                enableGradient = custom.enableGradient,
                gradientStartColor = custom.gradientStartColor,
                gradientEndColor = custom.gradientEndColor,
                gradientAngle = custom.gradientAngle,
                logoBitmap = custom.qrLogoBitmap,
                logoSizeMultiplier = custom.qrLogoSizeRatio,
                logoBadgeShape = custom.qrLogoBadgeShape,
                errorCorrectionLevel = custom.qrErrorCorrectionLevel
            )
        } catch (e: Exception) {
            null
        }"""

old_qr_block = """        val qrBitmap = try {
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
        }"""

text = text.replace(old_qr_block, new_qr_block)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

