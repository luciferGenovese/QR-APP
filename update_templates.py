import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

new_templates = """        TemplateItemDef(
            id = "vip_gold",
            name = "VIP Gold",
            category = "Elegante",
            vectorResId = R.drawable.template_frame_gold,
            primaryColor = Color.parseColor("#F59E0B"),
            cardColor = Color.parseColor("#18181B"),
            defaultText = "VIP ACCESS",
            defaultSubtext = "Escanea para beneficios exclusivos",
            defaultTextColor = Color.parseColor("#FBBF24"),
            isDarkTheme = true
        ),
        TemplateItemDef(
            id = "cafe_menu",
            name = "Café & Menú",
            category = "Gastronomía",
            vectorResId = R.drawable.template_frame_cafe,
            primaryColor = Color.parseColor("#4A3B32"),
            cardColor = Color.parseColor("#D4C3A3"),
            defaultText = "NUESTRO MENÚ",
            defaultSubtext = "Escanea para ordenar",
            defaultTextColor = Color.parseColor("#4A3B32"),
            isDarkTheme = false
        ),
        TemplateItemDef(
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
        ),
        TemplateItemDef(
            id = "eco_nature",
            name = "Eco Nature",
            category = "Natural",
            vectorResId = R.drawable.template_frame_nature,
            primaryColor = Color.parseColor("#10B981"),
            cardColor = Color.parseColor("#D1FAE5"),
            defaultText = "100% NATURAL",
            defaultSubtext = "Conoce nuestro origen",
            defaultTextColor = Color.parseColor("#14532D"),
            isDarkTheme = false
        ),
        TemplateItemDef(
            id = "romantic_love",
            name = "Romantic Love",
            category = "Especial",
            vectorResId = R.drawable.template_frame_love,
            primaryColor = Color.parseColor("#F43F5E"),
            cardColor = Color.parseColor("#FDF2F8"),
            defaultText = "SAVE THE DATE",
            defaultSubtext = "Confirma tu asistencia",
            defaultTextColor = Color.parseColor("#BE185D"),
            isDarkTheme = false
        ),
        TemplateItemDef(
            id = "party_event",
            name = "Party Event",
            category = "Eventos",
            vectorResId = R.drawable.template_frame_party,
            primaryColor = Color.parseColor("#9333EA"),
            cardColor = Color.parseColor("#F3E8FF"),
            defaultText = "VIP TICKETS",
            defaultSubtext = "Escanea tu entrada",
            defaultTextColor = Color.parseColor("#581C87"),
            isDarkTheme = false
        )"""

text = text.replace("""        TemplateItemDef(
            id = "vip_gold",
            name = "VIP Gold",
            category = "Elegante",
            vectorResId = R.drawable.template_frame_gold,
            primaryColor = Color.parseColor("#F59E0B"),
            cardColor = Color.parseColor("#18181B"),
            defaultText = "VIP ACCESS",
            defaultSubtext = "Escanea para beneficios exclusivos",
            defaultTextColor = Color.parseColor("#FBBF24"),
            isDarkTheme = true
        )""", new_templates)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

