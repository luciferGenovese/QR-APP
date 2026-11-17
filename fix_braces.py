with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

new_lines = []
current_depth = 0

for i, l in enumerate(lines):
    # Check if this line is a TAB marker
    if l.strip().startswith('// TAB '):
        # We need the depth to be exactly 6 right now!
        # If it's > 6, we need to insert closing braces.
        # If it's < 6, we need to remove previously added closing braces (harder, but usually it's > 6).
        while current_depth > 6:
            new_lines.append(' ' * (current_depth * 4 - 4) + '}')
            current_depth -= 1
        
        while current_depth < 6:
            # This means we closed too many braces!
            # Let's remove the last closing brace from new_lines
            for j in range(len(new_lines)-1, -1, -1):
                if new_lines[j].strip() == '}':
                    del new_lines[j]
                    current_depth += 1
                    break
                if current_depth == 6: break
                
    # Now process the line itself
    new_lines.append(l)
    current_depth += l.count('{') - l.count('}')

# At the end of the file, check if we need to close remaining braces
# Wait, the end of the file is outside the Customization Card.
# Let's just do it for the whole file. But we only care about TAB markers.
# Let's print final depth:
print(f"Final depth after parsing: {current_depth}")

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write('\n'.join(new_lines) + '\n')
