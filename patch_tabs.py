import re

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "r") as f:
    content = f.read()

target = """                        val tabs = listOf(
                            Triple(0, "Plantilla", Icons.Default.Star),
                            Triple(1, "LOGO", Icons.Default.Person),
                            Triple(2, "Color", Icons.Default.Star),
                            Triple(3, "Texto", Icons.Default.Info),
                            Triple(4, "Estilo", Icons.Default.Star)
                        )"""

replacement = """                        val tabs = listOf(
                            Triple(0, "Plantilla", Icons.Default.GridView),
                            Triple(1, "LOGO", Icons.Default.Image),
                            Triple(2, "Color", Icons.Default.Palette),
                            Triple(3, "Texto", Icons.Default.TextFields),
                            Triple(4, "Estilo", Icons.Default.Brush)
                        )"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "w") as f:
    f.write(content)
