import re
file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("import android.graphics.Colorimport androidx.compose.ui.graphics.Color as ComposeColorimport android.graphics.Paint", 
                          "import android.graphics.Color\nimport androidx.compose.ui.graphics.Color as ComposeColor\nimport android.graphics.Paint")
content = content.replace("import androidx.compose.ui.unit.dpimport androidx.compose.ui.unit.sp",
                          "import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp")
content = content.replace("import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.spimport", "import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp\nimport")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done fixing newlines")
