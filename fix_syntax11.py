import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# Let's see if SmartQrFeatures is even imported.
if "import com.example.utils.SmartQrFeatures" not in text:
    text = text.replace("import com.example.R\n", "import com.example.R\nimport com.example.utils.SmartQrFeatures\n")

# Replace fully qualified names just in case
text = text.replace("com.example.utils.SmartQrFeatures.generateCustomQrCode", "SmartQrFeatures.generateCustomQrCode")

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

