import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

text = text.replace('"eco_nature" -> targetHeight * 0.935f', '"eco_nature" -> targetHeight * 0.945f')

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)
