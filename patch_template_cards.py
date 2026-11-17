import re

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.QrCode, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Color(templateDef.primaryColor), modifier = Modifier.size(64.dp))
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Text(text = templateDef.name, style = MaterialTheme.typography.bodyMedium, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                                }
                                            }"""

new_target = """                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(6.dp)) {
                                                    androidx.compose.foundation.Image(
                                                        painter = androidx.compose.ui.res.painterResource(id = templateDef.vectorResId),
                                                        contentDescription = templateDef.name,
                                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = templateDef.name, 
                                                        style = MaterialTheme.typography.labelSmall, 
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray, 
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }"""

if target in content:
    content = content.replace(target, new_target)
else:
    print("Not found template cards!")

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)
