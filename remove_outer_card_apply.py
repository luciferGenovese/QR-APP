with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

start_idx = 1850
col_start = 1865
end_idx = 2956

# Remove end_idx and end_idx-1
del lines[end_idx]
del lines[end_idx-1]

# Remove start_idx to col_start
del lines[start_idx:col_start+1]

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write('\n'.join(lines) + '\n')
