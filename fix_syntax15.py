import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# Replace the QR generation call
old_qr_block = """        val qrBitmap = try {
            SmartQrFeatures.generateCustomQrCode(
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

new_qr_block = """        val qrBitmap = try {
            CodeGenerator.generateBitmap(
                content = custom.qrContent,
                format = BarcodeFormat.QR_CODE,
                width = qrSize,
                height = qrSize,
                foregroundColor = custom.qrForegroundColor,
                backgroundColor = custom.qrBackgroundColor,
                logoBitmap = custom.logoBitmap,
                logoSizeRatio = custom.logoSizeMultiplier
            )"""

text = text.replace(old_qr_block, new_qr_block)

# Clean up properties from TemplateCustomization that are no longer passed and I just hallucinated
# Actually, wait, it's safer to just let them be defaulted. But I should remove the duplicated parameters.
text = text.replace('val qrType: String = "SQUARE",', '')
text = text.replace('val qrEyeStyle: String = "SQUARE",', '')
text = text.replace('val enableGradient: Boolean = false,', '')
text = text.replace('val gradientStartColor: Int = Color.BLACK,', '')
text = text.replace('val gradientEndColor: Int = Color.BLACK,', '')
text = text.replace('val gradientAngle: Float = 0f,', '')
text = text.replace('val qrBodyShape: String = "SQUARE",', '')
text = text.replace('val qrLogoBitmap: Bitmap? = null,', '')
text = text.replace('val qrErrorCorrectionLevel: String = "Q",', '')
text = text.replace('val qrLogoSizeRatio: Float = 0.2f,', '')
text = text.replace('val qrLogoBadgeShape: String = "SQUARE",', '')
text = text.replace('val isBold: Boolean = true,', '')
text = text.replace('val isUppercase: Boolean = true,', '')
text = text.replace('val showSubtext: Boolean = true,', '')
text = text.replace('val qrScale: Float = 1.0f,', '')
text = text.replace('val qrOffsetY: Float = 0f', '')

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

