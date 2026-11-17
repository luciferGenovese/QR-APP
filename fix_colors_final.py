import re
file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove the alias and put standard Color back
content = content.replace("import android.graphics.Color\nimport androidx.compose.ui.graphics.Color as ComposeColor", "import androidx.compose.ui.graphics.Color")
content = content.replace("ComposeColor", "Color")
content = content.replace("android.graphics.Color.parseColor", "android.graphics.Color.parseColor")
# Wait, let's just make sure android.graphics.Color is explicitly used where it was Color.parseColor or HSVToColor
content = content.replace("Color.parseColor", "android.graphics.Color.parseColor")
content = content.replace("android.graphics.android.graphics.Color.parseColor", "android.graphics.Color.parseColor")
content = content.replace("HSVToColor", "android.graphics.Color.HSVToColor")
content = content.replace("android.graphics.Color.android.graphics.Color", "android.graphics.Color")
content = content.replace("drawColor", "drawColor") # drawColor is on Canvas, might need Int or Compose Color?
# Canvas drawColor in Compose takes compose color! So it's fine.

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done fixing colors final")
