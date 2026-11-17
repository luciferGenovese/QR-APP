with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

text = text.replace('.clickable(\n                    \n                    \n                    { expanded = !expanded }\n                )', '.clickable { expanded = !expanded }')

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)
