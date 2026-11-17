import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r', encoding='utf-8') as f:
    content = f.read()

sub_pattern = r'val baseSubY = when \(custom\.templateDef\.id\) \{.*?\}'
new_sub = '''val baseSubY = when (custom.templateDef.id) {
                "pay_minimal" -> if (hasFooterAccent) targetHeight * 0.835f else targetHeight * 0.850f
                "navy_geo" -> if (hasFooterAccent) targetHeight * 0.845f else targetHeight * 0.860f
                "modern_blue" -> if (hasFooterAccent) targetHeight * 0.845f else targetHeight * 0.860f
                "scan_me_neon" -> if (hasFooterAccent) targetHeight * 0.845f else targetHeight * 0.860f
                "minimal_clean" -> if (hasFooterAccent) targetHeight * 0.845f else targetHeight * 0.860f
                "vip_gold" -> if (hasFooterAccent) targetHeight * 0.845f else targetHeight * 0.860f
                else -> targetHeight * 0.850f
            }'''
content = re.sub(sub_pattern, new_sub, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated baseSubY.")
