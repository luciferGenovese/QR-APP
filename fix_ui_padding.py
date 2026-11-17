import re

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r') as f:
    text = f.read()

text = text.replace(
    """                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),""",
    """                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp, vertical = 8.dp),"""
)

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'w') as f:
    f.write(text)

