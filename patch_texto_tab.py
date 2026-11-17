import re

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "r") as f:
    content = f.read()

target = """                    if (isQrCode && activeCustomTab == 4) {"""

replacement = """                    // TAB 3: TEXTO (Label over QR)
                    if (isQrCode && activeCustomTab == 3) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Añadir texto al código QR",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            androidx.compose.material3.OutlinedTextField(
                                value = "",
                                onValueChange = {},
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Ej. ESCANÉAME") },
                                singleLine = true
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    if (isQrCode && activeCustomTab == 4) {"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "w") as f:
    f.write(content)
