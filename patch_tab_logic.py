import re

file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace activeCustomTab conditions
# First, Colores: was 0, now 2
content = content.replace("if (!isQrCode || activeCustomTab == 0) {", "if (!isQrCode || activeCustomTab == 2) {")

# Second, Formas (Estilo): was 1, now 4
content = content.replace("if (isQrCode && activeCustomTab == 1) {", "if (isQrCode && activeCustomTab == 4) {")

# Third, Logo: was 2, now 1
content = content.replace("if (isQrCode && activeCustomTab == 2) {", "if (isQrCode && activeCustomTab == 1) {")

# Fourth, Plantillas: was 3, now 0
content = content.replace("if (isQrCode && activeCustomTab == 3) {", "if (isQrCode && activeCustomTab == 0) {")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done patching tab logic")
