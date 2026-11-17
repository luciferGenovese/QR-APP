import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the entire baseSubY block
sub_pattern = r'val baseSubY = when \(custom\.templateDef\.id\) \{.*?\}'
new_sub = '''val baseSubY = when (custom.templateDef.id) {
                "pay_minimal" -> if (hasFooterAccent) targetHeight * 0.860f else targetHeight * 0.880f
                "navy_geo" -> if (hasFooterAccent) targetHeight * 0.870f else targetHeight * 0.890f
                "modern_blue" -> if (hasFooterAccent) targetHeight * 0.870f else targetHeight * 0.890f
                "scan_me_neon" -> if (hasFooterAccent) targetHeight * 0.870f else targetHeight * 0.890f
                "minimal_clean" -> if (hasFooterAccent) targetHeight * 0.870f else targetHeight * 0.890f
                "vip_gold" -> if (hasFooterAccent) targetHeight * 0.870f else targetHeight * 0.890f
                else -> targetHeight * 0.880f
            }'''
content = re.sub(sub_pattern, new_sub, content, flags=re.DOTALL)

# Replace the entire baseFooterY block
footer_pattern = r'val baseFooterY = when \(custom\.templateDef\.id\) \{.*?\}'
new_footer = '''val baseFooterY = when (custom.templateDef.id) {
                "navy_geo" -> targetHeight * 0.930f
                "pay_minimal" -> targetHeight * 0.920f
                "modern_blue" -> targetHeight * 0.930f
                "scan_me_neon" -> targetHeight * 0.930f
                "minimal_clean" -> targetHeight * 0.930f
                "vip_gold" -> targetHeight * 0.930f
                else -> targetHeight * 0.930f
            }'''
content = re.sub(footer_pattern, new_footer, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated baseSubY and baseFooterY.")
