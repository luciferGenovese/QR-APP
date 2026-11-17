import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

old_launched = "LaunchedEffect(content, secretMessage, secretPin, selectedForegroundColor, selectedBackgroundColor, selectedBodyShape, selectedLogoBitmap, type, removeLogo, cachedTemplateBitmap, currentChunkIndex, fileChunks, selectedTemplateDef, customTitleText, customSubText, selectedErrorCorrectionLevel, selectedLogoSizeRatio, selectedLogoBadgeShape) {"
new_launched = "LaunchedEffect(content, secretMessage, secretPin, selectedForegroundColor, selectedBackgroundColor, selectedBodyShape, selectedLogoBitmap, type, removeLogo, cachedTemplateBitmap, currentChunkIndex, fileChunks, selectedTemplateDef, customTitleText, customSubText, selectedErrorCorrectionLevel, selectedLogoSizeRatio, selectedLogoBadgeShape, customEmojiText) {"

text = text.replace(old_launched, new_launched)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

