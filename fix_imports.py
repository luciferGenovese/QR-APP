import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("rememberSaveable", "remember")
content = content.replace("Icons.Default.IosShare", "Icons.Default.Share")

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(content)

