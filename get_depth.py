with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()
d = 0
for i, l in enumerate(lines):
    d += l.count('{') - l.count('}')
    if '// TAB 0:' in l:
        print(f"Depth at TAB 0: {d}")
        break
