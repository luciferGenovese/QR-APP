file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

imports = """
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.ThumbUp
"""

content = content.replace("import androidx.compose.material.icons.filled.Star", "import androidx.compose.material.icons.filled.Star" + imports)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done fixing icons imports")
