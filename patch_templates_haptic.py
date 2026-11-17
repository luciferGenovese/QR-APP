import re

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('exportTemplatePdf(context, currentBmp, templateDef.name, titleText, contentText)', 
                          'com.example.utils.HapticUtil.playSuccessHaptic(context); exportTemplatePdf(context, currentBmp, templateDef.name, titleText, contentText)')

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("Patched TemplatesScreen for Haptics")
