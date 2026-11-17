import re

file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

def replace_block(content, start_marker, end_marker, new_content):
    start_idx = content.find(start_marker)
    if start_idx == -1: return content
    
    bracket_count = 0
    end_idx = -1
    for i in range(start_idx + len(start_marker), len(content)):
        if content[i] == '{':
            bracket_count += 1
        elif content[i] == '}':
            if bracket_count == 0:
                end_idx = i
                break
            bracket_count -= 1
            
    if end_idx != -1:
        return content[:start_idx] + new_content + content[end_idx+1:]
    return content

color_tab_start = "if (!isQrCode || activeCustomTab == 2) {"
new_color_tab = """if (!isQrCode || activeCustomTab == 2) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Sub-tabs (Primer plano, Fondo)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier.background(Color.Transparent),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("Primer plano", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF00C896))
                                    Text("Fondo", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            
                            // Color categories
                            val categories = listOf("Imagen", "Gradiente", "Color Sólido")
                            categories.forEach { category ->
                                Text(category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(12.dp))
                                
                                val colorsList = presetColors.take(5) // Just a placeholder for the colors
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (category == "Imagen") {
                                        Box(
                                            modifier = Modifier.size(50.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                                        }
                                    } else {
                                        colorsList.forEach { colorInt ->
                                            Box(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .background(Color(colorInt), RoundedCornerShape(8.dp))
                                                    .clickable { 
                                                        selectedForegroundColor = colorInt
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }"""

content = replace_block(content, color_tab_start, "", new_color_tab)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated color tab")
