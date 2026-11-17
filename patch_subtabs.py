import re

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "r") as f:
    content = f.read()

target = "var activeCustomTab by remember { mutableIntStateOf(0) }"
replacement = """var activeCustomTab by remember { mutableIntStateOf(0) }
            var colorSubTab by remember { mutableIntStateOf(0) }
            var logoSubTab by remember { mutableIntStateOf(0) }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "w") as f:
    f.write(content)
