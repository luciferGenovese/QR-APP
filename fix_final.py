import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# Completely remove references to the enum types and let them be strings again like they were.
# Then change the generate function back to passing strings.

text = text.replace("import com.example.utils.SmartQrFeatures.BodyShape\\n", "")
text = text.replace("import com.example.utils.SmartQrFeatures.ErrorCorrectionLevel\\n", "")
text = text.replace("import com.example.utils.SmartQrFeatures.LogoBadgeShape\\n", "")

old_data_class = """    val qrBodyShape: BodyShape = BodyShape.SQUARE,
    val qrLogoBitmap: Bitmap? = null,
    val qrErrorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.Q,
    val qrLogoSizeRatio: Float = 0.2f,
    val qrLogoBadgeShape: LogoBadgeShape = LogoBadgeShape.SQUARE,"""

new_data_class = """    val qrBodyShape: String = "SQUARE",
    val qrLogoBitmap: Bitmap? = null,
    val qrErrorCorrectionLevel: String = "Q",
    val qrLogoSizeRatio: Float = 0.2f,
    val qrLogoBadgeShape: String = "SQUARE","""

text = text.replace(old_data_class, new_data_class)

old_qr_block = """        val qrBitmap = try {
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
            )"""

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
                logoSizeMultiplier = custom.qrLogoSizeRatio
            )"""

text = text.replace(old_qr_block, new_qr_block)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

