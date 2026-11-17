import re

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r') as f:
    text = f.read()

bad_1 = """            colorPalette = colorPalette,
            isBold = true,
            isUppercase = true,
            onResetDefaults = {"""
good_1 = """            colorPalette = colorPalette,
            onResetDefaults = {"""
text = text.replace(bad_1, good_1)

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'w') as f:
    f.write(text)
