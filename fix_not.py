with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("expanded = !expanded", "expanded = expanded == false")

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(content)

