import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# Completely remove import com.example.utils.SmartQrFeatures
text = text.replace("import com.example.utils.SmartQrFeatures\n", "")

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r') as f:
    text = f.read()

# Fix the duplicate parameters in TextStudioDialog call
# The error says: Argument already passed for this parameter (lines 1088, 1089)
# Let's find out what they are exactly.
