import re

def replace_in_file(file_path, replacements):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    for old, new in replacements:
        if old in content:
            content = content.replace(old, new)
        else:
            print(f"WARNING: Could not find '{old[:30]}...' in {file_path}")
            
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

# 1. Update CreateScreen
create_screen_reps = [
    ('        CreateOption(stringResource(R.string.create_opt_art), Icons.Rounded.AutoAwesome, "QR_ART", primaryGroupColor, primaryIconColor),\n', '')
]
replace_in_file('app/src/main/java/com/example/ui/screens/CreateScreen.kt', create_screen_reps)

# 2. Update GenerateScreen
with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r', encoding='utf-8') as f:
    gen_content = f.read()

# Remove QR_ART from title mapping
gen_content = re.sub(r'\s*"QR_ART" -> context\.getString\(R\.string\.qr_type_art\)\n', '\n', gen_content)

# Remove QR_ART from validation
gen_content = re.sub(r'\s*"QR_ART" -> \{\s*if \(content\.isBlank\(\)\) \{\s*generalError = context\.getString\(R\.string\.err_art_empty\)\s*isValid = false\s*\}\s*\}\n', '\n', gen_content)

# Remove QR_ART from generation
# It's a bit larger, so I'll use regex to match from "QR_ART" -> { until "QR_STICKERS" -> {
art_block_pattern = r'(\s*)"QR_ART" -> \{.*?(\s*)"QR_STICKERS" -> \{'
gen_content = re.sub(art_block_pattern, r'\2"QR_STICKERS" -> {', gen_content, flags=re.DOTALL)

# Insert the explanation
explanation_code = """
                val explanation = when(type) {
                    "QR_SECRET" -> stringResource(R.string.exp_secret)
                    "QR_VCARD_PRO" -> stringResource(R.string.exp_vcard)
                    "QR_WIFI_PRO" -> stringResource(R.string.exp_wifi)
                    "QR_STICKERS" -> stringResource(R.string.exp_stickers)
                    "QR_WATERMARK" -> stringResource(R.string.exp_watermark)
                    "QR_ANIMATED" -> stringResource(R.string.exp_animated)
                    else -> null
                }
                if (explanation != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))"""

insert_target = """                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))"""

if insert_target in gen_content:
    gen_content = gen_content.replace(insert_target, "                    }\n                }\n" + explanation_code)
else:
    print("WARNING: Could not find insert_target in GenerateScreen.kt")

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w', encoding='utf-8') as f:
    f.write(gen_content)

print("Done updating app")
