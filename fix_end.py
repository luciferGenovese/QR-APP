with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

# We want to remove everything from line 4502 onwards, 
# wait, where does the new ExpandableSection start?
start = -1
for i, l in enumerate(lines):
    if l == '@Composable' and i+1 < len(lines) and lines[i+1].startswith('fun ExpandableSection('):
        start = i
        break

print(f"Start of ExpandableSection is {start}")

