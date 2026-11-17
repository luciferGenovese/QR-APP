import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

# Find the Main Customization Studio Card
start_card = -1
for i, l in enumerate(lines):
    if '// Main Customization Studio Card' in l:
        start_card = i
        break

# Find the Header Row inside it
header_start = -1
for i in range(start_card, len(lines)):
    if '// Header with friendly subtitle' in lines[i]:
        header_start = i
        break

header_end = -1
if header_start != -1:
    # Find the next TAB comment which indicates the end of the header section
    for i in range(header_start, len(lines)):
        if '// TAB 0:' in lines[i]:
            header_end = i - 1
            break

# Now find the Unified Section
unified_start = -1
for i in range(start_card, len(lines)):
    if '// SECCIÓN UNIFICADA: DISEÑO, FORMA Y LOGOTIPO' in lines[i]:
        unified_start = i
        break

unified_end = -1
if unified_start != -1:
    for i in range(unified_start, len(lines)):
        if '// TAB 3: TEXTO' in lines[i]:
            unified_end = i - 1
            break

print(f"Header: {header_start} to {header_end}")
print(f"Unified: {unified_start} to {unified_end}")
