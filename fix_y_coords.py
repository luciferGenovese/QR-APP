import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# Fix baseTitleY
old_base_title_y = """        val baseTitleY = when (custom.templateDef.id) {
            "escanea_aqui" -> targetHeight * 0.178f
            "navy_geo" -> if (hasHeaderAccent) targetHeight * 0.190f else targetHeight * 0.165f
            "pay_minimal" -> if (hasHeaderAccent) targetHeight * 0.195f else targetHeight * 0.175f
            "modern_blue" -> if (hasHeaderAccent) targetHeight * 0.170f else targetHeight * 0.145f
            "scan_me_neon" -> if (hasHeaderAccent) targetHeight * 0.175f else targetHeight * 0.150f
            "minimal_clean" -> if (hasHeaderAccent) targetHeight * 0.175f else targetHeight * 0.150f
            "vip_gold" -> if (hasHeaderAccent) targetHeight * 0.175f else targetHeight * 0.150f
            else -> targetHeight * 0.150f
        }"""
new_base_title_y = """        val baseTitleY = when (custom.templateDef.id) {
            "escanea_aqui" -> targetHeight * 0.178f
            "navy_geo" -> if (hasHeaderAccent) targetHeight * 0.190f else targetHeight * 0.165f
            "pay_minimal" -> if (hasHeaderAccent) targetHeight * 0.195f else targetHeight * 0.175f
            "modern_blue" -> if (hasHeaderAccent) targetHeight * 0.170f else targetHeight * 0.145f
            "scan_me_neon" -> if (hasHeaderAccent) targetHeight * 0.175f else targetHeight * 0.150f
            "minimal_clean" -> if (hasHeaderAccent) targetHeight * 0.175f else targetHeight * 0.150f
            "vip_gold" -> if (hasHeaderAccent) targetHeight * 0.175f else targetHeight * 0.150f
            "cafe_menu" -> targetHeight * 0.170f
            "tech_blue" -> targetHeight * 0.155f
            "eco_nature" -> targetHeight * 0.200f
            "romantic_love" -> targetHeight * 0.205f
            "party_event" -> targetHeight * 0.200f
            else -> targetHeight * 0.150f
        }"""
text = text.replace(old_base_title_y, new_base_title_y)

# Fix baseSubY
old_base_sub_y = """            val baseSubY = when (custom.templateDef.id) {
                "pay_minimal" -> if (hasFooterAccent) targetHeight * 0.835f else targetHeight * 0.850f
                "navy_geo" -> if (hasFooterAccent) targetHeight * 0.845f else targetHeight * 0.860f
                "modern_blue" -> if (hasFooterAccent) targetHeight * 0.868f else targetHeight * 0.883f
                "scan_me_neon" -> if (hasFooterAccent) targetHeight * 0.905f else targetHeight * 0.920f
                "minimal_clean" -> if (hasFooterAccent) targetHeight * 0.845f else targetHeight * 0.860f
                "vip_gold" -> if (hasFooterAccent) targetHeight * 0.875f else targetHeight * 0.892f
                else -> targetHeight * 0.860f
            }"""
new_base_sub_y = """            val baseSubY = when (custom.templateDef.id) {
                "pay_minimal" -> if (hasFooterAccent) targetHeight * 0.835f else targetHeight * 0.850f
                "navy_geo" -> if (hasFooterAccent) targetHeight * 0.845f else targetHeight * 0.860f
                "modern_blue" -> if (hasFooterAccent) targetHeight * 0.868f else targetHeight * 0.883f
                "scan_me_neon" -> if (hasFooterAccent) targetHeight * 0.905f else targetHeight * 0.920f
                "minimal_clean" -> if (hasFooterAccent) targetHeight * 0.845f else targetHeight * 0.860f
                "vip_gold" -> if (hasFooterAccent) targetHeight * 0.875f else targetHeight * 0.892f
                "cafe_menu" -> targetHeight * 0.905f
                "tech_blue" -> targetHeight * 0.925f
                "eco_nature" -> targetHeight * 0.935f
                "romantic_love" -> targetHeight * 0.895f
                "party_event" -> targetHeight * 0.915f
                else -> targetHeight * 0.860f
            }"""
text = text.replace(old_base_sub_y, new_base_sub_y)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)
