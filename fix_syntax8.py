import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# Ah! In fix_final.py, I used text.replace("import com.example.utils.SmartQrFeatures.BodyShape\\n", "") which failed because of the \\n probably.
# Let's remove those lines explicitly.

text = text.replace("import com.example.utils.SmartQrFeatures.BodyShape\n", "")
text = text.replace("import com.example.utils.SmartQrFeatures.ErrorCorrectionLevel\n", "")
text = text.replace("import com.example.utils.SmartQrFeatures.LogoBadgeShape\n", "")
text = text.replace("com.example.utils.SmartQrFeatures.generateCustomQrCode", "SmartQrFeatures.generateCustomQrCode")

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

