import re
file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix sp import
if "import androidx.compose.ui.unit.sp" not in content:
    content = content.replace("import androidx.compose.ui.unit.dp", "import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp")

# Remove all androidx.compose.ui.graphics.Color in favor of Color or android.graphics.Color
content = content.replace("androidx.compose.ui.graphics.Color", "Color")

# Fix Color.Transparent etc to standard Color if compose color was meant
# Or better, just import compose Color as CColor?
# Let's fix the icons first:
content = content.replace("Icons.Default.Person", "Icons.Default.AccountCircle")
content = content.replace("Icons.Default.Phone", "Icons.Default.Call")
content = content.replace("Icons.Default.Email", "Icons.Default.Email") # Email usually exists, or Mail
content = content.replace("Icons.Default.List", "Icons.Default.List")
content = content.replace("Icons.Default.Place", "Icons.Default.Place")
content = content.replace("Icons.Default.Face", "Icons.Default.Face")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done fixing compile errors 3")
