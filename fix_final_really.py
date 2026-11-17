file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix Icons
content = content.replace("Icons.Default.AccountCircle", "Icons.Default.Person")
content = content.replace("Icons.Default.Call", "Icons.Default.Phone")
content = content.replace("Icons.Default.List", "Icons.Default.Menu")
content = content.replace("Icons.Default.Place", "Icons.Default.LocationOn")

# Fix Android Color constants used with Paint/Pdf
content = content.replace("Color.BLACK", "android.graphics.Color.BLACK")
content = content.replace("Color.WHITE", "android.graphics.Color.WHITE")
content = content.replace("Color.DKGRAY", "android.graphics.Color.DKGRAY")
content = content.replace("Color.LTGRAY", "android.graphics.Color.LTGRAY")
content = content.replace("Color.GRAY", "android.graphics.Color.GRAY")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done fixing final really")
