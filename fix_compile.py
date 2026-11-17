import re

file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix Color imports / usage
content = content.replace("Color(", "androidx.compose.ui.graphics.Color(")
content = content.replace("androidx.compose.ui.graphics.Color(0xFF", "androidx.compose.ui.graphics.Color(0xFF")
content = content.replace("Color.Transparent", "androidx.compose.ui.graphics.Color.Transparent")
content = content.replace("Color.Gray", "androidx.compose.ui.graphics.Color.Gray")
content = content.replace("Color.White", "androidx.compose.ui.graphics.Color.White")
content = content.replace("Color.DarkGray", "androidx.compose.ui.graphics.Color.DarkGray")
content = content.replace("android.graphics.androidx.compose.ui.graphics.Color", "android.graphics.Color")

# Fix Icons
content = content.replace("Icons.Filled.Style", "Icons.Default.Star")
content = content.replace("Icons.Outlined.AccountCircle", "Icons.Default.Person")
content = content.replace("Icons.Filled.Palette", "Icons.Default.Star")
content = content.replace("Icons.Outlined.Palette", "Icons.Default.Star")
content = content.replace("Icons.Outlined.FormatSize", "Icons.Default.Info")
content = content.replace("Icons.Filled.AutoAwesome", "Icons.Default.Star")
content = content.replace("Icons.Outlined.AutoAwesome", "Icons.Default.Star")

# Fix missing icons in list
old_icon_list = "Icons.Default.CameraAlt, Icons.Default.ThumbUp, Icons.Default.PlayArrow, Icons.Default.Call, \\n                                Icons.Default.Payment, Icons.Default.TextFormat, Icons.Default.Link, Icons.Default.Wifi, \\n                                Icons.Default.Phone, Icons.Default.Email, Icons.Default.Person, Icons.Default.List, \\n                                Icons.Default.Place, Icons.Default.Info, Icons.Default.Face, Icons.Default.Star"
new_icon_list = "Icons.Default.PlayArrow, Icons.Default.Phone, Icons.Default.Email, Icons.Default.Person, Icons.Default.List, Icons.Default.Place, Icons.Default.Info, Icons.Default.Face, Icons.Default.Star, Icons.Default.PlayArrow, Icons.Default.Phone, Icons.Default.Email, Icons.Default.Person, Icons.Default.List, Icons.Default.Place, Icons.Default.Star"
# Better replace using regex for the list of icons
content = re.sub(r'val logos = listOf\([\s\S]*?Icons\.Default\.Star\n[\s]*\)', f"val logos = listOf({new_icon_list})", content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done fixing compile errors")
