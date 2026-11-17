import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

text = text.replace('defaultSubtext = "www.reallygreatsite.com"', 'defaultSubtext = "www.123.com"')

tech_blue_old = """        TemplateItemDef(
            id = "tech_blue",
            name = "Tech Blueprint",
            category = "Tecnología",
            vectorResId = R.drawable.template_frame_tech,
            primaryColor = Color.parseColor("#38BDF8"),
            cardColor = Color.parseColor("#0F172A"),
            defaultText = "WIFI ACCESS",
            defaultSubtext = "Conéctate rápidamente",
            defaultTextColor = Color.parseColor("#38BDF8"),
            isDarkTheme = true
        )"""
tech_blue_new = """        TemplateItemDef(
            id = "tech_blue",
            name = "Tech Blueprint",
            category = "Tecnología",
            vectorResId = R.drawable.template_frame_tech,
            primaryColor = Color.parseColor("#38BDF8"),
            cardColor = Color.parseColor("#0F172A"),
            defaultText = "WIFI ACCESS",
            defaultSubtext = "Conéctate rápidamente",
            defaultTextColor = Color.WHITE,
            isDarkTheme = true
        )"""
text = text.replace(tech_blue_old, tech_blue_new)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)
