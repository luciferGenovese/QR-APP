import re
file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

if "import androidx.compose.ui.graphics.Color as ComposeColor" not in content:
    content = content.replace("import androidx.compose.ui.graphics.Color\n", "")
    content = content.replace("import android.graphics.Color", "import android.graphics.Color\nimport androidx.compose.ui.graphics.Color as ComposeColor")

# Now change all the standard compose colors I added to ComposeColor
content = content.replace("Color.Transparent", "ComposeColor.Transparent")
content = content.replace("Color.Gray", "ComposeColor.Gray")
content = content.replace("Color.White", "ComposeColor.White")
content = content.replace("Color.DarkGray", "ComposeColor.DarkGray")
content = content.replace("Color.Black", "ComposeColor.Black")

# Also for Color(0xFF...) -> ComposeColor(0xFF...)
content = re.sub(r'Color\((0xFF[0-9A-Fa-f]{6})\)', r'ComposeColor(\1)', content)

# Remove the bad ComposeColor(0xFF...) if it somehow got wrapped
content = content.replace("android.graphics.ComposeColor", "android.graphics.Color")

# Some places I added `Color(template.foreground)` -> `ComposeColor(template.foreground)`
content = content.replace("Color(template.foreground)", "ComposeColor(template.foreground)")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done fixing compile errors 4")
