import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('exportStickerSheetPdf(context, livePreviewBitmap!!, p1.ifEmpty { "QR Stickers" }, p2)', 
                          'com.example.utils.HapticUtil.playSuccessHaptic(context); exportStickerSheetPdf(context, livePreviewBitmap!!, p1.ifEmpty { "QR Stickers" }, p2)')

content = content.replace('exportPdf(context, livePreviewBitmap!!, p1.ifEmpty { "QR Code" }, p2)', 
                          'com.example.utils.HapticUtil.playSuccessHaptic(context); exportPdf(context, livePreviewBitmap!!, p1.ifEmpty { "QR Code" }, p2)')

content = content.replace('exportPng(context, livePreviewBitmap!!, p1.ifEmpty { "QR Code" })', 
                          'com.example.utils.HapticUtil.playSuccessHaptic(context); exportPng(context, livePreviewBitmap!!, p1.ifEmpty { "QR Code" })')

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("Patched GenerateScreen for Haptics")
