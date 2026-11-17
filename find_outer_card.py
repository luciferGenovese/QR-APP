with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

start_idx = -1
for i, l in enumerate(lines):
    if '// Main Customization Studio Card' in l:
        start_idx = i
        break

if start_idx != -1:
    depth = 0
    # find where the inner Column starts
    col_start = -1
    for i in range(start_idx, len(lines)):
        depth += lines[i].count('{') - lines[i].count('}')
        if 'Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {' in lines[i]:
            col_start = i
            break
            
    print(f"Start: {start_idx}, Col Start: {col_start}")
