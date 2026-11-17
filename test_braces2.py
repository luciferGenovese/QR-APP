with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

def check(start, end, name):
    b = 0
    for i in range(start-1, end):
        b += lines[i].count('{') - lines[i].count('}')
    print(f"{name}: {b}")

# Before my replace, where did Logo start and end?
# I put it at 2030 to 2518 (approx) in the current file.
check(2032, 2517, "Current Logo")
