file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("parseandroidx.compose.ui.graphics.Color", "parseColor")
content = content.replace("parseandroidx", "parseColor")
content = content.replace("HSVToandroidx.compose.ui.graphics.Color", "HSVToColor")
content = content.replace("drawandroidx.compose.ui.graphics.Color", "drawColor")
content = content.replace("androidx.compose.ui.graphics.Color.parseColor", "android.graphics.Color.parseColor")
content = content.replace("android.graphics.androidx.compose.ui.graphics.Color", "android.graphics.Color")
content = content.replace("android.graphics.Color.White", "androidx.compose.ui.graphics.Color.White")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done fixing compile errors 2")
