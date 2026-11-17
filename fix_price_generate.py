import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

# Fix 19.99 price
text = text.replace('Text("$4.99", color = MaterialTheme.colorScheme.secondary)', 'Text("$19.99", color = MaterialTheme.colorScheme.secondary)')

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

