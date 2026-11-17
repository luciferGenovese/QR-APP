import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    content = f.read()

# 1. Insert ExpandableSection composable at the end of the file
expandable_section_code = """
@Composable
fun ExpandableSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            androidx.compose.animation.AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 0.dp)
                ) {
                    content()
                }
            }
        }
    }
}
"""
if "fun ExpandableSection" not in content:
    content += expandable_section_code

# 2. Add imports
imports_to_add = [
    "import androidx.compose.material.icons.filled.ExpandLess",
    "import androidx.compose.material.icons.filled.ExpandMore",
    "import androidx.compose.material.icons.filled.IosShare",
    "import androidx.compose.material3.ModalBottomSheet",
    "import androidx.compose.material3.rememberModalBottomSheetState",
    "import androidx.compose.material3.ExtendedFloatingActionButton",
    "import androidx.compose.runtime.saveable.rememberSaveable"
]
for imp in imports_to_add:
    if imp not in content:
        content = content.replace("import androidx.compose.runtime.remember", f"{imp}\nimport androidx.compose.runtime.remember", 1)


# 3. Add `showExportSheet` state variable inside GenerateScreen
if "var showExportSheet by rememberSaveable" not in content:
    content = content.replace(
        "var showSuccessDialog by remember { mutableStateOf(false) }",
        "var showSuccessDialog by remember { mutableStateOf(false) }\n    var showExportSheet by rememberSaveable { mutableStateOf(false) }"
    )


# 4. Remove SecondaryTabRow
tabs_regex = r"// Segmented Category Tabs\s+if \(isQrCode\) \{\s+val tabs = listOf\([\s\S]*?Spacer\(Modifier\.height\(16\.dp\)\)\s+\}"
content = re.sub(tabs_regex, "// Categorías desplegables integradas", content)


# 5. Replace `if (isQrCode && activeCustomTab == ...)` with ExpandableSection
content = content.replace(
    "// TAB 0: PLANTILLAS (Ready-to-use Themes)\n                    if (isQrCode && activeCustomTab == 0) {",
    "// TAB 0: PLANTILLAS (Ready-to-use Themes)\n                    if (isQrCode) {\n                        ExpandableSection(title = \"Plantillas Prediseñadas\", icon = Icons.Default.GridView, initiallyExpanded = true) {"
)

content = content.replace(
    "// TAB 1: LOGO (Insert Logo / App Icon)\n                    if (isQrCode && activeCustomTab == 1) {",
    "// TAB 1: LOGO (Insert Logo / App Icon)\n                    if (isQrCode) {\n                        ExpandableSection(title = \"Logotipo y Marca\", icon = Icons.Default.Image) {"
)

content = content.replace(
    "// TAB 2: COLORES (Código y Fondo)\n                    if (!isQrCode || activeCustomTab == 2) {",
    "// TAB 2: COLORES (Código y Fondo)\n                    ExpandableSection(title = \"Diseño y Colores\", icon = Icons.Default.Palette, initiallyExpanded = !isQrCode) {"
)

content = content.replace(
    "// TAB 3: TEXTO (Text in Templates)\n                    if (isQrCode && activeCustomTab == 3) {",
    "// TAB 3: TEXTO (Text in Templates)\n                    if (isQrCode) {\n                        ExpandableSection(title = \"Etiquetas de Texto\", icon = Icons.Default.TextFields) {"
)

content = content.replace(
    "// TAB 4: ESTILO (Forma de los módulos QR)\n                    if (isQrCode && activeCustomTab == 4) {",
    "// TAB 4: ESTILO (Forma de los módulos QR)\n                    if (isQrCode) {\n                        ExpandableSection(title = \"Estilo de Módulos y ECC\", icon = Icons.Default.Brush) {"
)

# 6. Replace Export options with BottomSheet toggle and add BottomSheet UI
export_old = """            // Opciones de Descarga e Impresión: Ubicadas abajo por debajo de generar y guardar código
            val activeBitmap = generatedBitmap ?: livePreviewBitmap
            if (activeBitmap != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {"""

export_new = """            // Opciones de Descarga e Impresión: Botón para abrir BottomSheet
            val activeBitmap = generatedBitmap ?: livePreviewBitmap
            if (activeBitmap != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showExportSheet = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.IosShare, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Opciones de Exportación", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                
                if (showExportSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showExportSheet = false },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).padding(bottom = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Exportar y Compartir",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Row 1: High Quality Images
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        showExportSheet = false
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || storagePermissionState.status.isGranted) {
                                            saveBitmapToGallery(context, activeBitmap, Bitmap.CompressFormat.PNG)
                                        } else {
                                            pendingBitmapFormat = Bitmap.CompressFormat.PNG
                                            storagePermissionState.launchPermissionRequest()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Guardar PNG")
                                }

                                Button(
                                    onClick = {
                                        showExportSheet = false
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || storagePermissionState.status.isGranted) {
                                            saveBitmapToGallery(context, activeBitmap, Bitmap.CompressFormat.JPEG)
                                        } else {
                                            pendingBitmapFormat = Bitmap.CompressFormat.JPEG
                                            storagePermissionState.launchPermissionRequest()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Guardar JPG")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Row 2: Print Poster & Stickers Sheet
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        showExportSheet = false
                                        val printTitle = customTitleText.ifBlank { typeTitle }
                                        val printSub = customSubText.ifBlank { "Escanea con la cámara de tu móvil" }
                                        PdfAndPrintHelper.printQrPoster(context, activeBitmap, printTitle, printSub)
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Póster A4", maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = {
                                        showExportSheet = false
                                        val printTitle = customTitleText.ifBlank { typeTitle }
                                        PdfAndPrintHelper.printStickerSheet(context, activeBitmap, printTitle)
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Icon(Icons.Default.GridView, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("12 Pegatinas", maxLines = 1)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Row 3: Vector SVG & Ultra HD Export
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        showExportSheet = false
                                        val contentToEncode = if (type == "QR_SECRET") {
                                            if (secretMessage.isNotBlank() && secretPin.isNotBlank()) SecretQrHelper.encrypt(secretMessage, secretPin) else content
                                        } else {
                                            content
                                        }
                                        exportSvg(
                                            context = context,
                                            content = contentToEncode,
                                            foregroundColor = selectedForegroundColor,
                                            backgroundColor = selectedBackgroundColor,
                                            bodyShape = selectedBodyShape,
                                            ecc = selectedErrorCorrectionLevel
                                        )
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Icon(Icons.Default.AspectRatio, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Vector SVG", maxLines = 1)
                                }

                                FilledTonalButton(
                                    onClick = { 
                                        showExportSheet = false
                                        showExportResolutionModal = true 
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Icon(Icons.Default.HighQuality, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ultra HD (4K)", maxLines = 1)
                                }
                            }
                        }
                    }
                }
            """

# We need to remove the remaining part of the old Export options card.
# The old block ends with:
#                             }
#                         }
#                     }
#                 }
#             }

import re

# Match the old Card completely. We know it starts exactly like `export_old`. 
# We'll replace it with a regex search to match from the old block to the closing braces.
card_end_regex = r"            // Opciones de Descarga e Impresión: Ubicadas abajo por debajo de generar y guardar código[\s\S]*?Ultra HD \(4K\)\", maxLines = 1\)\n                            \}\n                        \}\n                    \}\n                \}\n"

content = re.sub(card_end_regex, export_new, content)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(content)

