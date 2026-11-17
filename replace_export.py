with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

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
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Opciones de Exportación", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                
                if (showExportSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showExportSheet = false },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).padding(bottom = 56.dp),
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
            }"""

final_lines = lines[:3025] + export_new.split('\n') + lines[3188:]

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write('\n'.join(final_lines) + '\n')

