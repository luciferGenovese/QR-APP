def extract_block(lines, start_marker):
    start_idx = -1
    for i, line in enumerate(lines):
        if start_marker in line:
            start_idx = i
            break
            
    if start_idx == -1: return -1, -1
        
    brace_start = -1
    for i in range(start_idx, len(lines)):
        if "if (activeBitmap != null) {" in lines[i]:
            brace_start = i
            break
            
    open_braces = 0
    for i in range(start_idx, brace_start + 1):
        open_braces += lines[i].count("{") - lines[i].count("}")
        
    for i in range(brace_start + 1, len(lines)):
        open_braces += lines[i].count("{") - lines[i].count("}")
        if open_braces == 0:
            return start_idx, i

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

l, r = extract_block(lines, "// Opciones de Descarga e Impresión:")
print(f"Export block: {l} to {r}")
