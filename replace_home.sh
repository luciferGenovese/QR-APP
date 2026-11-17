sed -i -e '/Spacer(modifier = Modifier.height(16.dp))/,/Spacer(modifier = Modifier.height(24.dp))/c\
            Spacer(modifier = Modifier.height(16.dp))\
            Row(\
                modifier = Modifier.fillMaxWidth().height(100.dp),\
                horizontalArrangement = Arrangement.spacedBy(16.dp)\
            ) {\
                // Escanear QR\
                Card(\
                    modifier = Modifier.weight(1f).fillMaxHeight()\
                        .clickable { onNavigateToScan() }\
                        .then(if(themeMode == "liquid_glass") Modifier.border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(16.dp)) else Modifier),\
                    shape = RoundedCornerShape(16.dp),\
                    elevation = CardDefaults.cardElevation(defaultElevation = if (themeMode == "liquid_glass") 0.dp else 2.dp),\
                    colors = CardDefaults.cardColors(\
                        containerColor = if (themeMode == "pink") Color(0xFFC8E6C9)\
                                          else if (themeMode == "liquid_glass") Color(0x33FFFFFF)\
                                          else Color(0xFF4CAF50)\
                    )\
                ) {\
                    Box(\
                        modifier = Modifier.fillMaxSize(),\
                        contentAlignment = Alignment.Center\
                    ) {\
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {\
                            val scanContentColor = if (themeMode == "pink") Color(0xFF4A4A4A) else Color.White\
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = scanContentColor, modifier = Modifier.size(32.dp))\
                            Spacer(modifier = Modifier.height(4.dp))\
                            Text(stringResource(R.string.home_scan_qr), color = scanContentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)\
                        }\
                    }\
                }\
\
                // Transferir Archivo\
                Card(\
                    modifier = Modifier.weight(1f).fillMaxHeight()\
                        .clickable { onNavigateToTransfer() }\
                        .then(if(themeMode == "liquid_glass") Modifier.border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(16.dp)) else Modifier),\
                    shape = RoundedCornerShape(16.dp),\
                    elevation = CardDefaults.cardElevation(defaultElevation = if (themeMode == "liquid_glass") 0.dp else 2.dp),\
                    colors = CardDefaults.cardColors(\
                        containerColor = if (themeMode == "pink") Color(0xFFFFCC80)\
                                          else if (themeMode == "liquid_glass") Color(0x33FFFFFF)\
                                          else Color(0xFFFF9800)\
                    )\
                ) {\
                    Box(\
                        modifier = Modifier.fillMaxSize(),\
                        contentAlignment = Alignment.Center\
                    ) {\
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {\
                            val transferContentColor = if (themeMode == "pink") Color(0xFF4A4A4A) else Color.White\
                            Icon(Icons.Default.Share, contentDescription = null, tint = transferContentColor, modifier = Modifier.size(32.dp))\
                            Spacer(modifier = Modifier.height(4.dp))\
                            Text(stringResource(R.string.home_transfer_file), color = transferContentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)\
                        }\
                    }\
                }\
            }\
            Spacer(modifier = Modifier.height(24.dp))
' app/src/main/java/com/example/ui/screens/HomeScreen.kt
