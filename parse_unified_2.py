with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

unified_start = 2078
unified_end = 2874

# We can just use the markers
markers = {
    'Colores': (2081, 2243),
    'Estilo': (2247, 2381),
    'Logo': (2385, 2871)
}

# Now we will reconstruct the file:
# 1. up to header_start (1866)
# 2. Skip header (1866 to 1916)
# 3. from 1917 up to 2077
# 4. Instead of unified_start to unified_end, we write:

new_blocks = []

new_blocks.append('                    // TAB 1: LOGO')
new_blocks.append('                    if (isQrCode) ExpandableSection(title = "Logotipo y Marca", icon = Icons.Default.Image) {')
new_blocks.append('                        Column(modifier = Modifier.fillMaxWidth()) {')
new_blocks.extend(lines[2386:2872])
new_blocks.append('                        }')
new_blocks.append('                    }')

new_blocks.append('                    // TAB 2: COLORES (Código y Fondo)')
new_blocks.append('                    ExpandableSection(title = "Diseño y Colores", icon = Icons.Default.Palette, initiallyExpanded = !isQrCode) {')
new_blocks.append('                        Column(modifier = Modifier.fillMaxWidth()) {')
new_blocks.extend(lines[2082:2244])
new_blocks.append('                        }')
new_blocks.append('                    }')

new_blocks.append('                    // TAB 4: ESTILO (Forma de los módulos QR)')
new_blocks.append('                    if (isQrCode) ExpandableSection(title = "Estilo de Módulos y ECC", icon = Icons.Default.Brush) {')
new_blocks.append('                        Column(modifier = Modifier.fillMaxWidth()) {')
new_blocks.extend(lines[2248:2382])
new_blocks.append('                        }')
new_blocks.append('                    }')

final_lines = lines[:1866] + lines[1917:2078] + new_blocks + lines[2875:]

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write('\n'.join(final_lines) + '\n')
