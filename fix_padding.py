import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    content = f.read()

# Let's ensure the BottomSheet doesn't have overlapping padding or awkward spacing
content = content.replace("modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).padding(bottom = 32.dp)", 
                          "modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).padding(bottom = 56.dp)")

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(content)

