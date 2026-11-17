with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

braces = 0
for i, l in enumerate(lines):
    braces += l.count('{') - l.count('}')
    if braces < 0:
        print(f"Braces went negative at line {i+1}: {l}")
        break

print(f"Final braces count: {braces}")
