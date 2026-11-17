import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

billing_init = """    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val billingManager = remember(context) { com.example.billing.BillingManager(context) }
    androidx.compose.runtime.DisposableEffect(billingManager) {
        onDispose { billingManager.terminate() }
    }
    val isPremiumLogoUnlocked by billingManager.isPremiumLogoUnlocked.collectAsState()"""

text = text.replace("    val context = LocalContext.current", billing_init)


# Find the button for uploading logo.
old_btn = """                                            Button(
                                                onClick = {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                        logoPickerLauncher.launch("image/*")
                                                    } else {
                                                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Filled.FileUpload, contentDescription = null)
                                                Spacer(Modifier.width(8.dp))
                                                Text(if (selectedLogoBitmap != null && selectedLogoPresetId == "CUSTOM_UPLOAD") "Cambiar imagen seleccionada" else "Elegir archivo de imagen")
                                            }"""

new_btn = """                                            Button(
                                                onClick = {
                                                    if (isPremiumLogoUnlocked) {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                            logoPickerLauncher.launch("image/*")
                                                        } else {
                                                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                                        }
                                                    } else {
                                                        activity?.let { billingManager.launchBillingFlow(it) } ?: android.widget.Toast.makeText(context, "Error iniciando pagos", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(if (isPremiumLogoUnlocked) Icons.Filled.FileUpload else Icons.Filled.WorkspacePremium, contentDescription = null)
                                                Spacer(Modifier.width(8.dp))
                                                Text(if (!isPremiumLogoUnlocked) "Desbloquear Logo Premium ($1.00)" else if (selectedLogoBitmap != null && selectedLogoPresetId == "CUSTOM_UPLOAD") "Cambiar imagen seleccionada" else "Elegir archivo de imagen")
                                            }"""

text = text.replace(old_btn, new_btn)

# Find the clickable box logic right above the button
old_click = """                                            .clickable {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    logoPickerLauncher.launch("image/*")
                                                } else {
                                                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                                }
                                            }"""

new_click = """                                            .clickable {
                                                if (isPremiumLogoUnlocked) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                        logoPickerLauncher.launch("image/*")
                                                    } else {
                                                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                                    }
                                                } else {
                                                    activity?.let { billingManager.launchBillingFlow(it) } ?: android.widget.Toast.makeText(context, "Error iniciando pagos", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }"""

text = text.replace(old_click, new_click)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

