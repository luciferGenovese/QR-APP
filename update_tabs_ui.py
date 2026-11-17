import re

file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

def replace_block(content, start_marker, end_marker, new_content):
    start_idx = content.find(start_marker)
    if start_idx == -1: return content
    
    # Simple bracket counting to find the end of the block
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

# 1. Update Logo Tab
logo_tab_start = "if (isQrCode && activeCustomTab == 1) {"
new_logo_tab = """if (isQrCode && activeCustomTab == 1) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Sub-tabs (Imagen, Texto)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier.background(Color.Transparent),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("Imagen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF00C896))
                                    Text("Texto", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Biblioteca de logos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(12.dp))
                            
                            // Placeholder Grid for logos
                            val logos = listOf(
                                Icons.Default.CameraAlt, Icons.Default.ThumbUp, Icons.Default.PlayArrow, Icons.Default.Call,
                                Icons.Default.Payment, Icons.Default.TextFormat, Icons.Default.Link, Icons.Default.Wifi,
                                Icons.Default.Phone, Icons.Default.Email, Icons.Default.Person, Icons.Default.List,
                                Icons.Default.Place, Icons.Default.Info, Icons.Default.Face, Icons.Default.Star
                            )
                            
                            val chunkedLogos = logos.chunked(4)
                            chunkedLogos.forEach { rowLogos ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    rowLogos.forEach { icon ->
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
                                                .clickable { 
                                                    // selection logic
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(icon, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(32.dp))
                                        }
                                    }
                                }
                            }
                            
                            Spacer(Modifier.height(24.dp))
                            
                            // Bottom Shapes Selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick={}, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Gray)) { Text("Original") }
                                Button(onClick={}, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFF00C896))) { Text("Cuadrado") }
                                Button(onClick={}, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Gray)) { Text("Círculo") }
                            }
                        }
                    }"""

content = replace_block(content, logo_tab_start, "", new_logo_tab)

# 2. Update Plantilla Tab
plantilla_tab_start = "if (isQrCode && activeCustomTab == 0) {"
new_plantilla_tab = """if (isQrCode && activeCustomTab == 0) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Sub-tabs (Popular, Wifi, Negocio, Evento)
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                val tabs = listOf("Popular", "Wifi", "Negocio", "Evento")
                                items(tabs) { tab ->
                                    val isSelected = tab == "Popular"
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = tab,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF00C896) else Color.Gray,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            
                            // Grid of templates
                            val chunkedTemplates = templates.chunked(3)
                            chunkedTemplates.forEach { rowTemplates ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowTemplates.forEach { template ->
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(110.dp)
                                                .clickable {
                                                    selectedForegroundColor = template.foreground
                                                    selectedBackgroundColor = template.background
                                                    selectedBodyShape = template.shape
                                                },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(template.foreground), modifier = Modifier.size(64.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }"""

content = replace_block(content, plantilla_tab_start, "", new_plantilla_tab)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated logo and plantilla tabs")
