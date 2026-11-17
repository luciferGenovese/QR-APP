with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

# We extracted:
# Logo content: 2081 to 2566
# Colors content: 2573 to 2734
# Styles content: 2871 to 3005
# Text block: 2738 to 2866

# But in python list slicing, lines[i:j+1] is needed for inclusive range.
# Logo content: lines[2081:2567]
# Colors content: lines[2573:2735]
# Styles content: lines[2871:3006]
# Text block: lines[2738:2867]

new_content = [
    '                    // SECCIÓN UNIFICADA: DISEÑO, FORMA Y LOGOTIPO',
    '                    ExpandableSection(title = "Diseño, Forma y Logotipo", icon = Icons.Default.Palette, initiallyExpanded = !isQrCode) {',
    '                        Column(modifier = Modifier.fillMaxWidth()) {',
    '                            Text("🎨 COLORES Y FONDO", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp))',
] + lines[2573:2735] + [
    '                            if (isQrCode) {',
    '                                Spacer(modifier = Modifier.height(8.dp))',
    '                                androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp))',
    '                                Text("🔲 ESTILO DE MÓDULOS Y ECC", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))',
] + lines[2871:3006] + [
    '                                Spacer(modifier = Modifier.height(8.dp))',
    '                                androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp))',
    '                                Text("🖼️ LOGOTIPO Y MARCA", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))',
] + lines[2081:2567] + [
    '                            }',
    '                        }',
    '                    }',
    '                    // TAB 3: TEXTO (Se mantiene independiente)'
] + lines[2738:2867]

# Replace lines 2078 to 3007 (inclusive)
# Which means lines[:2078] + new_content + lines[3008:]
final_lines = lines[:2078] + new_content + lines[3008:]

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write('\n'.join(final_lines) + '\n')

