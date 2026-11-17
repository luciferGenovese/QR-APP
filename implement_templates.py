import re

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

# 1. Imports
if "import com.example.utils.TemplateRenderer" not in content:
    content = content.replace("import com.example.utils.CodeGenerator", "import com.example.utils.CodeGenerator\nimport com.example.utils.TemplateRenderer\nimport com.example.utils.TemplateCustomization\nimport com.example.utils.TemplateItemDef")

# 2. Add state variables
state_vars = """
    var selectedTemplateDef by remember { mutableStateOf<TemplateItemDef?>(null) }
    var customTitleText by remember { mutableStateOf("") }
    var customSubText by remember { mutableStateOf("") }
"""
if "var selectedTemplateDef" not in content:
    content = content.replace("var cachedTemplateBitmap by remember(templateResId) { mutableStateOf<Bitmap?>(null) }", "var cachedTemplateBitmap by remember(templateResId) { mutableStateOf<Bitmap?>(null) }\n" + state_vars)

# 3. Modify "Plantilla" tab (activeCustomTab == 0)
old_plantilla_tab = """                    if (isQrCode && activeCustomTab == 0) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Sub-tabs (Popular, Wifi, Negocio, Evento)
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                val tabs = listOf("Popular", "Wifi", "Negocio", "Evento")
                                items(tabs) { tab ->
                                    val isSelected = tab == plantillaSubTab
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { plantillaSubTab = tab }) {
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
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(template.foreground), modifier = Modifier.size(48.dp))
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(text = template.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }"""

new_plantilla_tab = """                    if (isQrCode && activeCustomTab == 0) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val categories = listOf("Todos") + TemplateRenderer.AVAILABLE_TEMPLATES.map { it.category }.distinct()
                            if (plantillaSubTab !in categories) {
                                plantillaSubTab = "Todos"
                            }
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(categories) { tab ->
                                    val isSelected = tab == plantillaSubTab
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { plantillaSubTab = tab }) {
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
                            
                            val filteredTemplates = if (plantillaSubTab == "Todos") TemplateRenderer.AVAILABLE_TEMPLATES else TemplateRenderer.AVAILABLE_TEMPLATES.filter { it.category == plantillaSubTab }
                            val chunkedTemplates = filteredTemplates.chunked(2)
                            chunkedTemplates.forEach { rowTemplates ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowTemplates.forEach { templateDef ->
                                        val isSelected = selectedTemplateDef?.id == templateDef.id
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(150.dp)
                                                .clickable {
                                                    if (isSelected) {
                                                        selectedTemplateDef = null
                                                    } else {
                                                        selectedTemplateDef = templateDef
                                                        selectedForegroundColor = templateDef.primaryColor
                                                        selectedBackgroundColor = templateDef.cardColor
                                                    }
                                                },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF5F5F5)),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                            )
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.QrCode, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Color(templateDef.primaryColor), modifier = Modifier.size(64.dp))
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Text(text = templateDef.name, style = MaterialTheme.typography.bodyMedium, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                    }
                                    if (rowTemplates.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }"""

if old_plantilla_tab in content:
    content = content.replace(old_plantilla_tab, new_plantilla_tab)
else:
    print("Could not find old plantilla tab")

# 4. Modify "Texto" tab (activeCustomTab == 3)
old_texto_tab = """                    // TAB 3: TEXTO (Label over QR)
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
                    }"""

new_texto_tab = """                    // TAB 3: TEXTO (Label over QR)
                    if (isQrCode && activeCustomTab == 3) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (selectedTemplateDef == null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                        Spacer(Modifier.width(12.dp))
                                        Text("Selecciona una Plantilla en la pestaña 'Plantilla' para añadir texto decorativo.", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        
                            Text(
                                text = "Título Superior",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            androidx.compose.material3.OutlinedTextField(
                                value = customTitleText,
                                onValueChange = { customTitleText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(selectedTemplateDef?.defaultText ?: "Ej. ESCANÉAME") },
                                singleLine = true,
                                enabled = selectedTemplateDef != null
                            )
                            Spacer(Modifier.height(16.dp))
                            
                            Text(
                                text = "Subtítulo Inferior",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            androidx.compose.material3.OutlinedTextField(
                                value = customSubText,
                                onValueChange = { customSubText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(selectedTemplateDef?.defaultSubtext ?: "Ej. Escanea con tu cámara") },
                                singleLine = true,
                                enabled = selectedTemplateDef != null
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }"""

if old_texto_tab in content:
    content = content.replace(old_texto_tab, new_texto_tab)
else:
    print("Could not find old texto tab")


# 5. Modify livePreviewBitmap generation logic
# Find where livePreviewBitmap is updated inside LaunchedEffect
preview_gen_target = """                val rawBitmap = CodeGenerator.generateBitmap(
                    content = currentContent,
                    format = format,
                    width = bw,
                    height = bh,
                    foregroundColor = selectedForegroundColor,
                    backgroundColor = selectedBackgroundColor,
                    bodyShape = selectedBodyShape,
                    logoBitmap = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type))
                )
                livePreviewBitmap = if (cachedTemplateBitmap != null && preview != null) {
                    compositeWithCachedTemplate(cachedTemplateBitmap!!, preview)
                } else {
                    preview
                }"""

new_preview_gen = """                val rawBitmap = CodeGenerator.generateBitmap(
                    content = currentContent,
                    format = format,
                    width = bw,
                    height = bh,
                    foregroundColor = selectedForegroundColor,
                    backgroundColor = selectedBackgroundColor,
                    bodyShape = selectedBodyShape,
                    logoBitmap = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type))
                )
                val preview = if (selectedTemplateDef != null) {
                    try {
                        val custom = TemplateCustomization(
                            templateDef = selectedTemplateDef!!,
                            qrContent = currentContent,
                            titleText = customTitleText.ifBlank { selectedTemplateDef!!.defaultText },
                            subtext = customSubText.ifBlank { selectedTemplateDef!!.defaultSubtext },
                            qrForegroundColor = selectedForegroundColor,
                            qrBackgroundColor = selectedBackgroundColor,
                            qrBodyShape = selectedBodyShape,
                            qrLogoBitmap = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type))
                        )
                        TemplateRenderer.renderTemplateBitmap(context, custom, 1024, 1280)
                    } catch (e: Exception) {
                        rawBitmap
                    }
                } else {
                    rawBitmap
                }
                
                livePreviewBitmap = if (cachedTemplateBitmap != null && preview != null) {
                    compositeWithCachedTemplate(cachedTemplateBitmap!!, preview)
                } else {
                    preview
                }"""

# Wait, `val preview = rawBitmap` is already there before `livePreviewBitmap =`. Let's just match the whole thing.
preview_gen_target_full = """                val rawBitmap = CodeGenerator.generateBitmap(
                    content = currentContent,
                    format = format,
                    width = bw,
                    height = bh,
                    foregroundColor = selectedForegroundColor,
                    backgroundColor = selectedBackgroundColor,
                    bodyShape = selectedBodyShape,
                    logoBitmap = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type))
                )
                val preview = rawBitmap
                livePreviewBitmap = if (cachedTemplateBitmap != null && preview != null) {
                    compositeWithCachedTemplate(cachedTemplateBitmap!!, preview)
                } else {
                    preview
                }"""

if preview_gen_target_full in content:
    content = content.replace(preview_gen_target_full, new_preview_gen)
else:
    print("Could not find preview gen target")
    # try flexible regex
    import re
    p = re.compile(r'val rawBitmap = CodeGenerator\.generateBitmap\([\s\S]*?val preview = rawBitmap\s+livePreviewBitmap = [^}]+\}\s+else\s+\{\s+preview\s+\}')
    match = p.search(content)
    if match:
        print("Found with regex!")
        content = content.replace(match.group(0), new_preview_gen)
    else:
        print("Could not find with regex either.")

# 6. Modify final generatedBitmap for saving
final_gen_target_full = """                val rawBitmap = CodeGenerator.generateBitmapOrThrow(
                    content = contentToEncode,
                    format = format,
                    width = bw,
                    height = bh,
                    foregroundColor = selectedForegroundColor,
                    backgroundColor = selectedBackgroundColor,
                    bodyShape = selectedBodyShape,
                    logoBitmap = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type))
                )
                generatedBitmap = if (cachedTemplateBitmap != null) {
                    compositeWithCachedTemplate(cachedTemplateBitmap!!, rawBitmap)
                } else {
                    rawBitmap
                }"""

new_final_gen = """                val rawBitmap = CodeGenerator.generateBitmapOrThrow(
                    content = contentToEncode,
                    format = format,
                    width = bw,
                    height = bh,
                    foregroundColor = selectedForegroundColor,
                    backgroundColor = selectedBackgroundColor,
                    bodyShape = selectedBodyShape,
                    logoBitmap = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type))
                )
                
                val finalRender = if (selectedTemplateDef != null) {
                    try {
                        val custom = TemplateCustomization(
                            templateDef = selectedTemplateDef!!,
                            qrContent = contentToEncode,
                            titleText = customTitleText.ifBlank { selectedTemplateDef!!.defaultText },
                            subtext = customSubText.ifBlank { selectedTemplateDef!!.defaultSubtext },
                            qrForegroundColor = selectedForegroundColor,
                            qrBackgroundColor = selectedBackgroundColor,
                            qrBodyShape = selectedBodyShape,
                            qrLogoBitmap = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type))
                        )
                        TemplateRenderer.renderTemplateBitmap(context, custom, 1024, 1280)
                    } catch (e: Exception) {
                        rawBitmap
                    }
                } else {
                    rawBitmap
                }
                
                generatedBitmap = if (cachedTemplateBitmap != null) {
                    compositeWithCachedTemplate(cachedTemplateBitmap!!, finalRender)
                } else {
                    finalRender
                }"""

if final_gen_target_full in content:
    content = content.replace(final_gen_target_full, new_final_gen)
else:
    print("Could not find final gen target")

# Also add the new state vars to the LaunchedEffect dependency list:
effect_target = "LaunchedEffect(content, secretMessage, secretPin, selectedForegroundColor, selectedBackgroundColor, selectedBodyShape, selectedLogoBitmap, type, removeLogo, cachedTemplateBitmap, currentChunkIndex, fileChunks) {"
new_effect = "LaunchedEffect(content, secretMessage, secretPin, selectedForegroundColor, selectedBackgroundColor, selectedBodyShape, selectedLogoBitmap, type, removeLogo, cachedTemplateBitmap, currentChunkIndex, fileChunks, selectedTemplateDef, customTitleText, customSubText) {"
content = content.replace(effect_target, new_effect)

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)

