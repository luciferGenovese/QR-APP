with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

depth = 0
for i, l in enumerate(lines):
    stripped = l.lstrip()
    if not stripped: continue
    
    # expected indent is depth * 4
    # but we just want to see where depth goes wrong
    
    # adjust for closing braces on this line
    closing = l.count('}')
    opening = l.count('{')
    
    # if line starts with }, depth decreases before checking
    if stripped.startswith('}'):
        expected_depth = depth - 1
    else:
        expected_depth = depth
        
    actual_depth = (len(l) - len(stripped)) // 4
    
    if abs(expected_depth - actual_depth) >= 2 and i > 1850 and i < 3000:
        print(f"Mismatch at {i+1}: expected {expected_depth}, actual {actual_depth}. Line: {stripped[:50]}")
    
    depth += opening - closing

