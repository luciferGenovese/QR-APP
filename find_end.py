with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

depth = 0
found_brace = False
for i in range(1851, len(lines)):
    depth += lines[i].count('{') - lines[i].count('}')
    if lines[i].count('{') > 0: found_brace = True
    if found_brace and depth == 0:
        print(f"Ends at {i+1}")
        break
