import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

# 1. Find the start of the Customization Studio Header
header_start = -1
for i, l in enumerate(lines):
    if 'text = stringResource(R.string.title_personalize)' in l:
        # Go up to the Row
        for j in range(i, -1, -1):
            if 'Row(' in lines[j] and 'modifier = Modifier.fillMaxWidth()' in lines[j]:
                header_start = j
                break
        break

header_end = -1
if header_start != -1:
    open_braces = 0
    for i in range(header_start, len(lines)):
        open_braces += lines[i].count("{") - lines[i].count("}")
        if open_braces == 0:
            header_end = i
            break

print(f"Header: {header_start} to {header_end}")

# 2. Find the Unified Section
unified_start = -1
for i, l in enumerate(lines):
    if 'ExpandableSection(title = "Diseño, Forma y Logotipo"' in l:
        unified_start = i
        break

unified_end = -1
if unified_start != -1:
    open_braces = 0
    for i in range(unified_start, len(lines)):
        open_braces += lines[i].count("{") - lines[i].count("}")
        if open_braces == 0:
            unified_end = i
            break

print(f"Unified: {unified_start} to {unified_end}")
