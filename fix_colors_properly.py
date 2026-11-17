import re
file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Make sure we don't import android.graphics.Color
content = content.replace("import android.graphics.Color\n", "")

# Make sure we DO import androidx.compose.ui.graphics.Color
if "import androidx.compose.ui.graphics.Color\n" not in content:
    content = content.replace("package com.example.ui.screens\n", "package com.example.ui.screens\nimport androidx.compose.ui.graphics.Color\n")

# Replace any usages of android.graphics.Color back to fully qualified if they aren't already
# The ones that need it: parseColor, HSVToColor, TRANSPARENT, WHITE, BLACK (if used as Ints for the generator)
content = content.replace("android.graphics.Color.parseColor", "android.graphics.Color.parseColor")
content = content.replace("Color.parseColor", "android.graphics.Color.parseColor")
content = content.replace("Color.HSVToColor", "android.graphics.Color.HSVToColor")

# Wait, `Color` now resolves to Compose Color. So `Color.White` is fine for Compose.
# But for the generator `presetColors = listOf(Color.BLACK, Color.WHITE)` which need to be Ints.
content = content.replace("presetColors = remember {\n                listOf(\n                    Color.BLACK,\n                    Color.WHITE,",
                          "presetColors = remember {\n                listOf(\n                    android.graphics.Color.BLACK,\n                    android.graphics.Color.WHITE,")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done fixing colors properly")
