package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CodeEntity
import com.example.ui.MainViewModel

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToBarcode: () -> Unit,
    onNavigateToEditBarcode: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToTransfer: () -> Unit
) {
    val codes by viewModel.history.collectAsState(initial = emptyList())
    val themeMode by viewModel.themeState.collectAsState()
    
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var selectedListType by remember { mutableStateOf("QR") }
    var selectedCodeForView by remember { mutableStateOf<com.example.data.CodeEntity?>(null) }
    
    val folders = codes.mapNotNull { it.folderName }.distinct()
    val isGlass = themeMode == "liquid_glass" || themeMode == "liquid_glass_dark"
    val isDarkGlass = themeMode == "liquid_glass_dark"
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Row (Create)
                Row(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Crear QR
                    val createQrBrush = if (themeMode == "pink") {
                        SolidColor(Color(0xFFFFCDD2))
                    } else if (isGlass) {
                        Brush.linearGradient(listOf(Color(0x66FFFFFF), Color(0x11FFFFFF)))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFFF44336), Color(0xFFC62828)))
                    }
                    val createQrTextColor = if (themeMode == "pink") Color(0xFF4A4A4A) else Color.White

                    Card(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .clickable { onNavigateToCreate() }
                            .then(if (isGlass) Modifier.border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(20.dp)) else Modifier),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isGlass) 0.dp else 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(createQrBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = createQrTextColor, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(stringResource(R.string.home_create_qr), color = createQrTextColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                    
                    // Crear Código de barras
                    val createBarcodeBrush = if (themeMode == "pink") {
                        SolidColor(Color(0xFFFFCC80))
                    } else if (isGlass) {
                        Brush.linearGradient(listOf(Color(0x66FFFFFF), Color(0x11FFFFFF)))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFFFFEB3B), Color(0xFFFBC02D)))
                    }
                    val createBarcodeTextColor = if (themeMode == "pink") Color(0xFF4A4A4A) else if (isGlass) Color.White else Color(0xFF4A4A4A)

                    Card(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .clickable { onNavigateToBarcode() }
                            .then(if (isGlass) Modifier.border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(20.dp)) else Modifier),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isGlass) 0.dp else 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(createBarcodeBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.ViewColumn, contentDescription = null, tint = createBarcodeTextColor, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(stringResource(R.string.home_create_barcode), color = createBarcodeTextColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }
                
                // Bottom Row (Edit)
                Row(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Editar QR
                    val editQrBrush = if (themeMode == "pink") {
                        SolidColor(Color(0xFFB3E5FC))
                    } else if (isGlass) {
                        Brush.linearGradient(listOf(Color(0x66FFFFFF), Color(0x11FFFFFF)))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFF03A9F4), Color(0xFF01579B)))
                    }
                    val editQrTextColor = if (themeMode == "pink") Color(0xFF4A4A4A) else Color.White
                    
                    Card(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .clickable { onNavigateToEdit() }
                            .then(if (isGlass) Modifier.border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(16.dp)) else Modifier),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isGlass) 0.dp else 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(editQrBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = editQrTextColor, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(stringResource(R.string.home_edit_qr), color = editQrTextColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    // Editar Código de barras
                    val editBarcodeBrush = if (themeMode == "pink") {
                        SolidColor(Color(0xFFE1BEE7))
                    } else if (isGlass) {
                        Brush.linearGradient(listOf(Color(0x66FFFFFF), Color(0x11FFFFFF)))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFF9C27B0), Color(0xFF6A1B9A)))
                    }
                    val editBarcodeTextColor = if (themeMode == "pink") Color(0xFF4A4A4A) else Color.White

                    Card(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .clickable { onNavigateToEditBarcode() }
                            .then(if (isGlass) Modifier.border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(16.dp)) else Modifier),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isGlass) 0.dp else 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(editBarcodeBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = editBarcodeTextColor, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(stringResource(R.string.home_edit_barcode), color = editBarcodeTextColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Escanear QR
                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .clickable { onNavigateToScan() }
                        .then(if(isGlass) Modifier.border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(16.dp)) else Modifier),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isGlass) 0.dp else 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (themeMode == "pink") Color(0xFFC8E6C9)
                                          else if (isGlass) Color(0x33FFFFFF)
                                          else Color(0xFF4CAF50)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            val scanContentColor = if (themeMode == "pink") Color(0xFF4A4A4A) else Color.White
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = scanContentColor, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(stringResource(R.string.home_scan_qr), color = scanContentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }

                // Transferir Archivo
                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .clickable { onNavigateToTransfer() }
                        .then(if(isGlass) Modifier.border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(16.dp)) else Modifier),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isGlass) 0.dp else 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (themeMode == "pink") Color(0xFFFFCC80)
                                          else if (isGlass) Color(0x33FFFFFF)
                                          else Color(0xFFFF9800)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            val transferContentColor = if (themeMode == "pink") Color(0xFF4A4A4A) else Color.White
                            Icon(Icons.Default.Share, contentDescription = null, tint = transferContentColor, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(stringResource(R.string.home_transfer_file), color = transferContentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Guardados",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkGlass) Color.White else Color.Black
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            // Filters
            val homeFilterChipColors = if (isDarkGlass) {
                androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            } else {
                androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = Color.Black,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = Color.Black
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedListType == "QR",
                    onClick = { selectedListType = "QR" },
                    label = {
                        Text(
                            stringResource(R.string.tab_qrs),
                            fontWeight = if (selectedListType == "QR") FontWeight.Bold else FontWeight.Normal,
                            color = if (isDarkGlass) {
                                if (selectedListType == "QR") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            } else {
                                Color.Black
                            }
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = homeFilterChipColors
                )
                FilterChip(
                    selected = selectedListType == "BARCODE",
                    onClick = { selectedListType = "BARCODE" },
                    label = {
                        Text(
                            stringResource(R.string.tab_barcodes),
                            fontWeight = if (selectedListType == "BARCODE") FontWeight.Bold else FontWeight.Normal,
                            color = if (isDarkGlass) {
                                if (selectedListType == "BARCODE") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            } else {
                                Color.Black
                            }
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = homeFilterChipColors
                )
            }

        }
        
        item {
            if (folders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FolderCard(
                            name = stringResource(R.string.folder_all),
                            isSelected = selectedFolder == null,
                            onClick = { selectedFolder = null },
                            themeMode = themeMode
                        )
                    }
                    items(folders) { folder ->
                        FolderCard(
                            name = folder,
                            isSelected = selectedFolder == folder,
                            onClick = { selectedFolder = folder },
                            themeMode = themeMode
                        )
                    }
                }
            }
        }

        val filteredCodes = codes.filter { code ->
            val isTypeMatch = if (selectedListType == "QR") {
                code.format == "QR_CODE" || code.format == "ANIM_QR"
            } else {
                code.format != "QR_CODE" && code.format != "ANIM_QR"
            }
            val isFolderMatch = selectedFolder == null || code.folderName == selectedFolder
            isTypeMatch && isFolderMatch
        }

        if (filteredCodes.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.home_no_codes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDarkGlass) MaterialTheme.colorScheme.onSurfaceVariant else Color.Black
                    )
                }
            }
        } else {
            items(filteredCodes, key = { it.id }) { item ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedCodeForView = item },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            item.content,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkGlass) MaterialTheme.colorScheme.onSurface else Color.Black
                        )
                        Text(
                            item.format,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkGlass) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF374151)
                        )
                    }
                }
            }
        }
    }

    if (selectedCodeForView != null) {
        com.example.ui.screens.ViewQrDialog(
            code = selectedCodeForView!!,
            onDismiss = { selectedCodeForView = null }
        )
    }
}

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun FolderCard(name: String, isSelected: Boolean, onClick: () -> Unit, onLongClick: (() -> Unit)? = null, themeMode: String = "system") {
    val isDarkGlass = themeMode == "liquid_glass_dark"
    val displayName = when (name) {
        "SCAN" -> stringResource(R.string.scan_type_scanned)
        "QR_ART" -> stringResource(R.string.create_opt_art)
        "QR_VCARD_PRO" -> stringResource(R.string.create_opt_vcard)
        "QR_WIFI_PRO" -> stringResource(R.string.create_opt_wifi)
        "QR_SECRET" -> stringResource(R.string.create_opt_secret)
        "QR_STICKERS" -> stringResource(R.string.create_opt_stickers)
        "QR_WATERMARK" -> stringResource(R.string.create_opt_watermark)
        "QR_ANIMATED" -> stringResource(R.string.create_opt_animated)
        "QR_TEXT" -> "Texto"
        "QR_CODE" -> stringResource(R.string.scan_type_created)
        else -> name
    }
    
    Card(
        modifier = Modifier.combinedClickable(onClick = { onClick() }, onLongClick = { onLongClick?.invoke() }),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkGlass) {
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            } else {
                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Text(
            text = displayName,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isDarkGlass) {
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            } else {
                Color.Black
            }
        )
    }
}
