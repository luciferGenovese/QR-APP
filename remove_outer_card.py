with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

start_idx = 1850
col_start = 1865

depth = 0
found = False
end_idx = -1
# we want to find the closing brace of the Card which is at depth 0
# we'll start depth counting from col_start
for i in range(col_start, len(lines)):
    depth += lines[i].count('{') - lines[i].count('}')
    # the Card block ends when depth drops to -1 relative to col_start's initial '{'
    # actually, col_start line has 1 '{', so after processing it, depth is 1
    # when the Column closes, depth will become 0
    # when the Card closes, depth will become -1
    if depth == -1:
        end_idx = i
        break

print(f"End idx: {end_idx}")

if end_idx != -1:
    # the Column closes at some line before end_idx.
    # usually it's the line immediately before.
    print(f"Line at end_idx-1: {lines[end_idx-1]}")
    print(f"Line at end_idx: {lines[end_idx]}")
