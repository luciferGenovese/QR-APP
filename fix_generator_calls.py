import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

# Update generateBitmap call at line 536
old_call1 = """                val rawBitmap = CodeGenerator.generateBitmap(
                    content = textContent,
                    format = BarcodeFormat.QR_CODE,
                    foregroundColor = selectedForegroundColor,
                    backgroundColor = selectedBackgroundColor,
                    bodyShape = selectedShape,
                    logoBitmap = activeLogo,
                    errorCorrectionLevel = selectedEccLevel
                )"""
new_call1 = """                val rawBitmap = CodeGenerator.generateBitmap(
                    content = textContent,
                    format = BarcodeFormat.QR_CODE,
                    foregroundColor = selectedForegroundColor,
                    backgroundColor = selectedBackgroundColor,
                    bodyShape = selectedShape,
                    logoBitmap = activeLogo,
                    errorCorrectionLevel = selectedEccLevel,
                    customEmoji = customEmojiText
                )"""
text = text.replace(old_call1, new_call1)

# Update generateBitmapOrThrow call at line 773
old_call2 = """                val rawBitmap = CodeGenerator.generateBitmapOrThrow(
                    content = textContent,
                    format = BarcodeFormat.QR_CODE,
                    width = 2048,
                    height = 2048,
                    foregroundColor = selectedForegroundColor,
                    backgroundColor = selectedBackgroundColor,
                    bodyShape = selectedShape,
                    logoBitmap = activeLogo,
                    errorCorrectionLevel = selectedEccLevel
                )"""
new_call2 = """                val rawBitmap = CodeGenerator.generateBitmapOrThrow(
                    content = textContent,
                    format = BarcodeFormat.QR_CODE,
                    width = 2048,
                    height = 2048,
                    foregroundColor = selectedForegroundColor,
                    backgroundColor = selectedBackgroundColor,
                    bodyShape = selectedShape,
                    logoBitmap = activeLogo,
                    errorCorrectionLevel = selectedEccLevel,
                    customEmoji = customEmojiText
                )"""
text = text.replace(old_call2, new_call2)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

