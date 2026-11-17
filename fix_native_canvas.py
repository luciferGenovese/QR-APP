import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

text = text.replace("drawContext.canvas.nativeCanvas.drawText", "drawContext.canvas.nativeCanvas.drawText")
if "import androidx.compose.ui.graphics.nativeCanvas" not in text:
    text = text.replace("import androidx.compose.ui.graphics.Color", "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.nativeCanvas")

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

