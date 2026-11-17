import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

# Replace all isPremiumLogoUnlocked with isPro
text = text.replace("isPremiumLogoUnlocked", "isPro")

# The button logic for logo
old_btn_logic = """if (isPro) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                        logoPickerLauncher.launch("image/*")
                                                    } else {
                                                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                                    }
                                                } else {
                                                    activity?.let { billingManager.launchBillingFlow(it) } ?: android.widget.Toast.makeText(context, "Error iniciando pagos", android.widget.Toast.LENGTH_SHORT).show()
                                                }"""

new_btn_logic = """if (isPro) {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                            logoPickerLauncher.launch("image/*")
                                                        } else {
                                                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                                        }
                                                    } else {
                                                        showPaywallDialog = true
                                                    }"""

# Fix clickable
text = text.replace(old_btn_logic, new_btn_logic)

# Fix the button itself
old_btn_text = """Text(if (!isPro) "Desbloquear Logo Premium ($1.00)" else if (selectedLogoBitmap != null && selectedLogoPresetId == "CUSTOM_UPLOAD") "Cambiar imagen seleccionada" else "Elegir archivo de imagen")"""
new_btn_text = """Text(if (!isPro) "Usar Logo Propio (PRO)" else if (selectedLogoBitmap != null && selectedLogoPresetId == "CUSTOM_UPLOAD") "Cambiar imagen seleccionada" else "Elegir archivo de imagen")"""
text = text.replace(old_btn_text, new_btn_text)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

