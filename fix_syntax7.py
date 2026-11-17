import re

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r') as f:
    text = f.read()

# Let's fix the missing parameters in TextStudioDialog call by adding them properly.
# The error says: No value passed for parameter 'showSubtext'

text = text.replace("isUppercase = true\n            },\n            onDismiss =", "isUppercase = true\n            },\n            showSubtext = true,\n            isBold = true,\n            isUppercase = true,\n            onDismiss =")

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'w') as f:
    f.write(text)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# Fix SmartQrFeatures import in TemplateRenderer
# My previous script `fix_final.py` might have left something behind.
# The error is: Unresolved reference 'SmartQrFeatures'. 
# That's because SmartQrFeatures is in `com.example.utils` but let's see. Wait, TemplateRenderer is also in `com.example.utils`. So why is it unresolved?
# Because `SmartQrFeatures` might be a file name, not an object. But `SmartQrFeatures` was working before.
# Let's check `com.example.utils.SmartQrFeatures`
