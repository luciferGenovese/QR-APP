with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

def check(start, end, name):
    b = 0
    for i in range(start-1, end):
        b += lines[i].count('{') - lines[i].count('}')
    print(f"{name}: {b}")

check(1917, 2030, "Tab 0")
check(2518, 2686, "Tab 2 (Colores)")
check(2826, 2962, "Tab 4 (Estilo)")
