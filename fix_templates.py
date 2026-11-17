import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# The python script I ran previously to remove duplicates messed up the file syntax completely by removing arguments from TemplateItemDef
# Let's fix the entire AVAILABLE_TEMPLATES block

new_block = """    val AVAILABLE_TEMPLATES = listOf(
        TemplateItemDef(
            id = "escanea_aqui",
            name = "Escanea Aquí",
            category = "Destacado",
            vectorResId = R.drawable.template_frame_escanea_aqui,
            primaryColor = Color.parseColor("#D4062A"),
            cardColor = Color.parseColor("#012322"),
            defaultText = "Escanea Aquí",
            defaultSubtext = "",
            defaultTextColor = Color.parseColor("#FDFEFF"),
            isDarkTheme = true,
            defaultHeaderAccent = null,
            defaultFooterAccent = null
        ),
        TemplateItemDef(
            id = "navy_geo",
            name = "Navy Geometric",
            category = "Corporativo",
            vectorResId = R.drawable.template_frame_navy_geo,
            primaryColor = Color.parseColor("#00A3FF"),
            cardColor = Color.parseColor("#093C68"),
            defaultText = "SCAN HERE",
            defaultSubtext = "www.123.com",
            defaultTextColor = Color.WHITE,
            isDarkTheme = true,
            defaultHeaderAccent = null,
            defaultFooterAccent = null
        ),
        TemplateItemDef(
            id = "pay_minimal",
            name = "Escanea Para Pagar",
            category = "Comercio",
            vectorResId = R.drawable.template_frame_pay_minimal,
            primaryColor = Color.BLACK,
            cardColor = Color.WHITE,
            defaultText = "ESCANEA PARA PAGAR",
            defaultSubtext = "¡GRACIAS POR ELEGIRNOS!",
            defaultTextColor = Color.BLACK,
            isDarkTheme = false,
            defaultHeaderAccent = null,
            defaultFooterAccent = null
        ),
        TemplateItemDef(
            id = "modern_blue",
            name = "Modern Blue",
            category = "Moderno",
            vectorResId = R.drawable.template_frame_modern,
            primaryColor = Color.parseColor("#3B82F6"),
            cardColor = Color.WHITE,
            defaultText = "ESCANÉAME",
            defaultSubtext = "Apunta con la cámara",
            defaultTextColor = Color.WHITE,
            isDarkTheme = false
        ),
        TemplateItemDef(
            id = "scan_me_neon",
            name = "Cyber Neon",
            category = "Neón",
            vectorResId = R.drawable.template_frame_scanme,
            primaryColor = Color.parseColor("#06B6D4"),
            cardColor = Color.parseColor("#0F172A"),
            defaultText = "SCAN ME",
            defaultSubtext = "Acceso instantáneo con tu móvil",
            defaultTextColor = Color.parseColor("#22D3EE"),
            isDarkTheme = true
        ),
        TemplateItemDef(
            id = "minimal_clean",
            name = "Minimal Clean",
            category = "Minimalista",
            vectorResId = R.drawable.template_frame_minimal,
            primaryColor = Color.parseColor("#475569"),
            cardColor = Color.parseColor("#F8FAFC"),
            defaultText = "MENÚ DIGITAL",
            defaultSubtext = "Escanea el código para ver",
            defaultTextColor = Color.parseColor("#1E293B"),
            isDarkTheme = false
        ),
        TemplateItemDef(
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
            defaultTextColor = Color.WHITE,
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
        )
    )

    fun renderTemplateBitmap("""

lines = text.split('\n')
start_idx = -1
for i, line in enumerate(lines):
    if 'val AVAILABLE_TEMPLATES = listOf(' in line:
        start_idx = i
        break

end_idx = -1
for i in range(start_idx, len(lines)):
    if 'fun renderTemplateBitmap(' in lines[i]:
        end_idx = i
        break

if start_idx != -1 and end_idx != -1:
    old_block = '\\n'.join(lines[start_idx:end_idx+1])
    text = text.replace(old_block, new_block.strip())

    # The previous script also corrupted the end of the file by cutting off fitTextSize and drawFormattedText
    # Let's fix that too. Wait, let's see how much it corrupted.

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

