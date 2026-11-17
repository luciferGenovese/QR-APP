import re
with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "r") as f:
    content = f.read()

target1 = """                                                .clickable {
                                                     // selection logic
                                                },"""

replacement1 = """                                                .clickable {
                                                     // selection logic (Not fully implemented without external icons, but visual selection can be simulated)
                                                },"""

if target1 in content:
    content = content.replace(target1, replacement1)

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "w") as f:
    f.write(content)
