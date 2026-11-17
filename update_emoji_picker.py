import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

old_block = """                                if (selectedBodyShape == com.example.utils.BodyShape.EMOJI) {
                                    androidx.compose.material3.OutlinedTextField(
                                        value = customEmojiText,
                                        onValueChange = { if (it.length <= 2) customEmojiText = it },
                                        label = { Text("Escribe tu Emoji") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                    )
                                }"""

new_block = """                                if (selectedBodyShape == com.example.utils.BodyShape.EMOJI) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = customEmojiText,
                                            onValueChange = { if (it.length <= 2) customEmojiText = it },
                                            label = { Text("Escribe tu Emoji o Símbolo") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Recomendados (Sólidos / Alta legibilidad):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        androidx.compose.foundation.lazy.LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val recommendedSymbols = listOf(
                                                "❤", "❣", "💕", "💖", "★", "✪", "◆", "■", "●", "▲", "▼", "⬢", "✚", "✖", "✿", "❁", "♣", "♠", "♦", "☁", "☂", "☘", "⚡"
                                            )
                                            items(recommendedSymbols.size) { index ->
                                                val sym = recommendedSymbols[index]
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    modifier = Modifier.clickable { customEmojiText = sym }
                                                ) {
                                                    Text(
                                                        text = sym,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }"""

text = text.replace(old_block, new_block)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

