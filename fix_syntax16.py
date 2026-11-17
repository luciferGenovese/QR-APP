import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# Fix the trailing comma in TemplateCustomization because I removed properties at the end.
text = re.sub(r'val qrBackgroundColor: Int = Color.WHITE,\s+', 'val qrBackgroundColor: Int = Color.WHITE\n', text)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

