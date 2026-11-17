import re
file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("android.graphics.android.graphics.Color", "android.graphics.Color")
content = content.replace("android.graphics.android.graphics.Color", "android.graphics.Color")

# Check for BLACK/WHITE
content = content.replace("android.graphics.BLACK", "android.graphics.Color.BLACK")
content = content.replace("android.graphics.WHITE", "android.graphics.Color.WHITE")
content = content.replace("android.graphics.DKGRAY", "android.graphics.Color.DKGRAY")
content = content.replace("android.graphics.LTGRAY", "android.graphics.Color.LTGRAY")
content = content.replace("android.graphics.GRAY", "android.graphics.Color.GRAY")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done fixing dupes")
