import re

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "r") as f:
    content = f.read()

target = """                            // Sub-tabs (Imagen, Texto)
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
                            }"""

replacement = """                            // Sub-tabs (Imagen, Texto)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier.background(Color.Transparent),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("Imagen", style = MaterialTheme.typography.titleMedium, fontWeight = if (logoSubTab == 0) FontWeight.Bold else FontWeight.Medium, color = if (logoSubTab == 0) Color(0xFF00C896) else Color.Gray, modifier = Modifier.clickable { logoSubTab = 0 }.padding(4.dp))
                                    Text("Texto", style = MaterialTheme.typography.titleMedium, fontWeight = if (logoSubTab == 1) FontWeight.Bold else FontWeight.Medium, color = if (logoSubTab == 1) Color(0xFF00C896) else Color.Gray, modifier = Modifier.clickable { logoSubTab = 1 }.padding(4.dp))
                                }
                            }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "w") as f:
    f.write(content)
