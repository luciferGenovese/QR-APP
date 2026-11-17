import sys
with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

# We need to find the "Diseño y Color" header section around line 1890
start_header = -1
for i, l in enumerate(lines):
    if 'text = "Diseño y Color"' in l:
        start_header = i
        break

# We need to go up to the Row containing it.
if start_header != -1:
    while "Row(" not in lines[start_header]:
        start_header -= 1
    
    end_header = start_header
    open_braces = 0
    for i in range(start_header, len(lines)):
        open_braces += lines[i].count("{") - lines[i].count("}")
        if open_braces == 0:
            end_header = i
            break
            
    print(f"Header block: {start_header} to {end_header}")
