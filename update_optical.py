import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

old_optical = """                        Button(
                            onClick = { onNavigateToUltraTransfer?.invoke() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black)
                        ) {
                            Text("Probar", fontWeight = FontWeight.Bold)
                        }"""

new_optical = """                        Button(
                            onClick = { 
                                if (isPro) {
                                    onNavigateToUltraTransfer?.invoke()
                                } else {
                                    showPaywallDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black)
                        ) {
                            Icon(if (isPro) Icons.Filled.Check else Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isPro) "Abrir" else "PRO", fontWeight = FontWeight.Bold)
                        }"""

text = text.replace(old_optical, new_optical)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

