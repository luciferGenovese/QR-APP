import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

old_block = """                                    FilledTonalButton(
                                        onClick = { showFullColorPickerDialog = true },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Filled.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Rueda & HEX", style = MaterialTheme.typography.labelSmall)
                                    }"""

new_block = """                                    FilledTonalButton(
                                        onClick = { 
                                            if (isPro) {
                                                showFullColorPickerDialog = true 
                                            } else {
                                                showPaywallDialog = true
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Filled.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Rueda & HEX", style = MaterialTheme.typography.labelSmall)
                                        if (!isPro) {
                                            Spacer(Modifier.width(4.dp))
                                            Icon(Icons.Filled.Lock, contentDescription = "PRO", modifier = Modifier.size(14.dp))
                                        }
                                    }"""

text = text.replace(old_block, new_block)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

