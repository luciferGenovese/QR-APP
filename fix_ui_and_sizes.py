import re

# 1. Fix TemplatesScreen.kt Card backgrounds
with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r') as f:
    ts_text = f.read()

target_card = """                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTemplateDef = template
                                titleText = template.defaultText
                                headerAccentText = template.defaultHeaderAccent ?: ""
                                footerAccentText = template.defaultFooterAccent ?: ""
                                subtext = template.defaultSubtext
                                selectedTextColor = template.defaultTextColor
                                selectedSubtextColor = if (template.isDarkTheme) Color.LTGRAY else Color.DKGRAY
                                qrForegroundColor = Color.BLACK
                                qrBackgroundColor = Color.WHITE
                                textSizeSp = if (template.id == "escanea_aqui") 22f else 24f
                                subtextSizeSp = 13f
                                textOffsetY = 0f
                                textOffsetX = 0f
                                qrScale = 1.0f
                                qrOffsetY = 0f
                                qrBodyShape = BodyShape.SQUARE
                                textAlign = if (template.id == "escanea_aqui") Paint.Align.LEFT else Paint.Align.CENTER
                                isBold = true
                                isUppercase = template.id != "escanea_aqui"
                                showSubtext = template.id != "escanea_aqui" && template.defaultSubtext.isNotBlank()
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )"""

replacement_card = """                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTemplateDef = template
                                titleText = template.defaultText
                                headerAccentText = template.defaultHeaderAccent ?: ""
                                footerAccentText = template.defaultFooterAccent ?: ""
                                subtext = template.defaultSubtext
                                selectedTextColor = template.defaultTextColor
                                selectedSubtextColor = if (template.isDarkTheme) Color.LTGRAY else Color.DKGRAY
                                qrForegroundColor = Color.BLACK
                                qrBackgroundColor = Color.WHITE
                                textSizeSp = if (template.id == "escanea_aqui") 22f else 24f
                                subtextSizeSp = 13f
                                textOffsetY = 0f
                                textOffsetX = 0f
                                qrScale = 1.0f
                                qrOffsetY = 0f
                                qrBodyShape = BodyShape.SQUARE
                                textAlign = if (template.id == "escanea_aqui") Paint.Align.LEFT else Paint.Align.CENTER
                                isBold = true
                                isUppercase = template.id != "escanea_aqui"
                                showSubtext = template.id != "escanea_aqui" && template.defaultSubtext.isNotBlank()
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    )"""

if target_card in ts_text:
    ts_text = ts_text.replace(target_card, replacement_card)
    with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'w') as f:
        f.write(ts_text)
    print("TemplatesScreen.kt Card updated")
else:
    print("TemplatesScreen.kt Card NOT FOUND")

# 2. Fix TemplateRenderer.kt density bug and sizing
with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    tr_text = f.read()

# We replace the density logic with a fixed multiplier.
# 1024f/512f = 2.0. If we assume density was 2.5 on the phone, the old multiplier was 2 * 2.5 = 5.0.
# Let's just use 4.5f as a solid multiplier to make text large but safe.
# Actually, let's use 5.0f for the title since it was very small according to the user.

tr_text = tr_text.replace(
    "newSize = custom.textSizeSp * 0.5f * density * (1024f / 512f)",
    "newSize = custom.textSizeSp * 2.5f"
)
tr_text = tr_text.replace(
    "newSize = custom.textSizeSp * density * (1024f / 512f)",
    "newSize = custom.textSizeSp * 5.0f"
)
tr_text = tr_text.replace(
    "newSize = (custom.subtextSizeSp * 1.1f) * density * (1024f / 512f)",
    "newSize = custom.subtextSizeSp * 5.5f"
)
tr_text = tr_text.replace(
    "newSize = (custom.subtextSizeSp * 1.2f) * density * (1024f / 512f)",
    "newSize = custom.subtextSizeSp * 6.0f"
)
tr_text = tr_text.replace(
    "newSize = custom.subtextSizeSp * density * (1024f / 512f)",
    "newSize = custom.subtextSizeSp * 5.0f"
)
tr_text = tr_text.replace(
    "newSize = custom.subtextSizeSp * 0.8f * density * (1024f / 512f)",
    "newSize = custom.subtextSizeSp * 4.0f"
)

# Also ensure we don't have remaining density references in textPaint.textSize if any
with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(tr_text)
print("TemplateRenderer.kt sizes updated")

