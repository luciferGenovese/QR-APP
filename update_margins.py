import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Update Header Accent Y
content = content.replace(
    '"navy_geo" -> (targetHeight * 0.085f)',
    '"navy_geo" -> (targetHeight * 0.115f)'
)
content = content.replace(
    '"pay_minimal" -> (targetHeight * 0.095f)',
    '"pay_minimal" -> (targetHeight * 0.125f)'
)
content = content.replace(
    'else -> (targetHeight * 0.070f)',
    'else -> (targetHeight * 0.100f)'
)

# Update Title Y
content = content.replace(
    '"navy_geo" -> if (hasHeaderAccent) targetHeight * 0.160f else targetHeight * 0.135f',
    '"navy_geo" -> if (hasHeaderAccent) targetHeight * 0.190f else targetHeight * 0.165f'
)
content = content.replace(
    '"pay_minimal" -> if (hasHeaderAccent) targetHeight * 0.165f else targetHeight * 0.145f',
    '"pay_minimal" -> if (hasHeaderAccent) targetHeight * 0.195f else targetHeight * 0.175f'
)
content = content.replace(
    '"modern_blue" -> if (hasHeaderAccent) targetHeight * 0.140f else targetHeight * 0.115f',
    '"modern_blue" -> if (hasHeaderAccent) targetHeight * 0.170f else targetHeight * 0.145f'
)
content = content.replace(
    '"scan_me_neon" -> if (hasHeaderAccent) targetHeight * 0.145f else targetHeight * 0.120f',
    '"scan_me_neon" -> if (hasHeaderAccent) targetHeight * 0.175f else targetHeight * 0.150f'
)
content = content.replace(
    '"minimal_clean" -> if (hasHeaderAccent) targetHeight * 0.145f else targetHeight * 0.120f',
    '"minimal_clean" -> if (hasHeaderAccent) targetHeight * 0.175f else targetHeight * 0.150f'
)
content = content.replace(
    '"vip_gold" -> if (hasHeaderAccent) targetHeight * 0.145f else targetHeight * 0.120f',
    '"vip_gold" -> if (hasHeaderAccent) targetHeight * 0.175f else targetHeight * 0.150f'
)
content = content.replace(
    'else -> targetHeight * 0.120f',
    'else -> targetHeight * 0.150f'
)

# Update Subtext Y
content = content.replace(
    '"pay_minimal" -> if (hasFooterAccent) targetHeight * 0.815f else targetHeight * 0.840f',
    '"pay_minimal" -> if (hasFooterAccent) targetHeight * 0.775f else targetHeight * 0.800f'
)
content = content.replace(
    '"navy_geo" -> if (hasFooterAccent) targetHeight * 0.835f else targetHeight * 0.850f',
    '"navy_geo" -> if (hasFooterAccent) targetHeight * 0.795f else targetHeight * 0.810f'
)
content = content.replace(
    '"modern_blue" -> if (hasFooterAccent) targetHeight * 0.850f else targetHeight * 0.865f',
    '"modern_blue" -> if (hasFooterAccent) targetHeight * 0.810f else targetHeight * 0.825f'
)
content = content.replace(
    '"scan_me_neon" -> if (hasFooterAccent) targetHeight * 0.860f else targetHeight * 0.875f',
    '"scan_me_neon" -> if (hasFooterAccent) targetHeight * 0.820f else targetHeight * 0.835f'
)
content = content.replace(
    '"minimal_clean" -> if (hasFooterAccent) targetHeight * 0.850f else targetHeight * 0.865f',
    '"minimal_clean" -> if (hasFooterAccent) targetHeight * 0.810f else targetHeight * 0.825f'
)
content = content.replace(
    '"vip_gold" -> if (hasFooterAccent) targetHeight * 0.850f else targetHeight * 0.865f',
    '"vip_gold" -> if (hasFooterAccent) targetHeight * 0.810f else targetHeight * 0.825f'
)
content = content.replace(
    'else -> targetHeight * 0.865f',
    'else -> targetHeight * 0.825f'
)

# Update Footer Y
content = content.replace(
    '"navy_geo" -> targetHeight * 0.895f',
    '"navy_geo" -> targetHeight * 0.855f'
)
content = content.replace(
    '"pay_minimal" -> targetHeight * 0.875f',
    '"pay_minimal" -> targetHeight * 0.835f'
)
content = content.replace(
    '"modern_blue" -> targetHeight * 0.910f',
    '"modern_blue" -> targetHeight * 0.870f'
)
content = content.replace(
    '"scan_me_neon" -> targetHeight * 0.920f',
    '"scan_me_neon" -> targetHeight * 0.880f'
)
content = content.replace(
    '"minimal_clean" -> targetHeight * 0.910f',
    '"minimal_clean" -> targetHeight * 0.870f'
)
content = content.replace(
    '"vip_gold" -> targetHeight * 0.910f',
    '"vip_gold" -> targetHeight * 0.870f'
)
content = content.replace(
    'else -> targetHeight * 0.910f',
    'else -> targetHeight * 0.870f'
)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("Updates applied.")
