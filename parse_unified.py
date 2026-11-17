with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

unified_start = 2078
unified_end = 2874

# Within this block we have:
# Text("🎨 COLORES Y FONDO")
# ... colors block ...
# Text("🔲 ESTILO DE MÓDULOS Y ECC")
# ... estilo block ...
# Text("🖼️ LOGOTIPO Y MARCA")
# ... logo block ...

# Let's find their indices
colores_start = -1
estilo_start = -1
logo_start = -1

for i in range(unified_start, unified_end + 1):
    if 'Text("🎨 COLORES Y FONDO"' in lines[i]: colores_start = i
    if 'Text("🔲 ESTILO DE MÓDULOS' in lines[i]: estilo_start = i
    if 'Text("🖼️ LOGOTIPO Y MARCA' in lines[i]: logo_start = i

print(f"Colores: {colores_start}")
print(f"Estilo: {estilo_start}")
print(f"Logo: {logo_start}")
