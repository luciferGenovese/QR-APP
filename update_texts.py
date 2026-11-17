import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# Update Escanea Aquí template text
text = text.replace('name = "Escanea Aquí",', 'name = "Escanea",')
text = text.replace('defaultText = "Escanea Aquí",', 'defaultText = "Escanea",')
text = text.replace('text = "Escanea Aquí", fontSize = 48f,', 'text = "Escanea", fontSize = 48f,')

# Update navy_geo subtext position
target_navy = """TemplateTextElement(id = "subtext", x = 512f, y = 1100f, width = 768f, height = 80f, text = "www.123.com", fontSize = 28f, align = Paint.Align.CENTER, color = "#4ADE80", isBold = true),
                TemplateTextElement(id = "footer", x = 512f, y = 1190f, width = 768f, height = 50f, text = "", fontSize = 22f, align = Paint.Align.CENTER, color = "#FFFFFF", isBold = false)"""

replacement_navy = """TemplateTextElement(id = "subtext", x = 512f, y = 1145f, width = 768f, height = 80f, text = "www.123.com", fontSize = 28f, align = Paint.Align.CENTER, color = "#4ADE80", isBold = true),
                TemplateTextElement(id = "footer", x = 512f, y = 1210f, width = 768f, height = 50f, text = "", fontSize = 22f, align = Paint.Align.CENTER, color = "#FFFFFF", isBold = false)"""

if target_navy in text:
    text = text.replace(target_navy, replacement_navy)
    print("Updated navy_geo")
else:
    print("Could not find navy_geo text block")

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

