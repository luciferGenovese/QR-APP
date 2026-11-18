package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CodeEntity
import com.example.ui.MainViewModel

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
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
    val codes by viewModel.history.collectAsState()
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var selectedListType by remember { mutableStateOf("QR") }
    var selectedCodeForView by remember { mutableStateOf<CodeEntity?>(null) }
    val folders = codes.mapNotNull { it.folderName }.distinct()
    val filteredCodes = codes.filter { code ->
        val isQr = code.format == "QR_CODE" || code.format == "ANIM_QR"
        (if (selectedListType == "QR") isQr else !isQr) &&
            (selectedFolder == null || code.folderName == selectedFolder)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(stringResource(R.string.home_heading), style = MaterialTheme.typography.headlineLarge)
        }
        item {
            Card(
                onClick = onNavigateToScan,
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF173D2F), contentColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.home_scan_qr), style = MaterialTheme.typography.headlineSmall)
                        Text(
                            stringResource(R.string.home_scan_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCCE4D6),
                            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                        )
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFFA7F3CB))
                    }
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFB7F4D2)) {
                        Icon(
                            Icons.Default.QrCodeScanner, contentDescription = null,
                            tint = Color(0xFF173D2F), modifier = Modifier.padding(16.dp).size(40.dp)
                        )
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeActionCard(stringResource(R.string.home_create_qr), Icons.Default.QrCode, onNavigateToCreate, Modifier.weight(1f))
                    HomeActionCard(stringResource(R.string.home_create_barcode), Icons.Default.ViewColumn, onNavigateToBarcode, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeActionCard(stringResource(R.string.home_edit_qr), Icons.Default.AutoFixHigh, onNavigateToEdit, Modifier.weight(1f))
                    HomeActionCard(stringResource(R.string.home_edit_barcode), Icons.Default.Edit, onNavigateToEditBarcode, Modifier.weight(1f))
                }
            }
        }
        item {
            Card(
                onClick = onNavigateToTransfer,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, null, tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.home_transfer_file), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.home_saved), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text(filteredCodes.size.toString(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedListType == "QR", onClick = { selectedListType = "QR" },
                    label = { Text(stringResource(R.string.tab_qrs)) },
                    leadingIcon = { Icon(Icons.Default.QrCode, null, Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = selectedListType == "BARCODE", onClick = { selectedListType = "BARCODE" },
                    label = { Text(stringResource(R.string.tab_barcodes)) },
                    leadingIcon = { Icon(Icons.Default.ViewColumn, null, Modifier.size(16.dp)) }
                )
            }
        }
        if (folders.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FolderCard(stringResource(R.string.folder_all), selectedFolder == null, { selectedFolder = null })
                    }
                    items(folders) { folder ->
                        FolderCard(folder, selectedFolder == folder, { selectedFolder = folder })
                    }
                }
            }
        }
        if (filteredCodes.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.BookmarkBorder, null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            stringResource(R.string.home_no_codes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredCodes, key = { it.id }) { code ->
                Card(
                    onClick = { selectedCodeForView = code },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (selectedListType == "QR") Icons.Default.QrCode else Icons.Default.ViewColumn,
                            null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)
                        )
                        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                            Text(code.content, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                            Text(code.format.replace('_', ' '), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    selectedCodeForView?.let { code ->
        ViewQrDialog(code = code, onDismiss = { selectedCodeForView = null })
    }
}

@Composable
private fun HomeActionCard(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 132.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            Text(title.replace('\n', ' '), style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun FolderCard(name: String, isSelected: Boolean, onClick: () -> Unit) {
    val displayName = when (name) {
        "SCAN" -> stringResource(R.string.scan_type_scanned)
        "QR_ART" -> stringResource(R.string.create_opt_art)
        "QR_VCARD_PRO" -> stringResource(R.string.create_opt_vcard)
        "QR_WIFI_PRO" -> stringResource(R.string.create_opt_wifi)
        "QR_SECRET" -> stringResource(R.string.create_opt_secret)
        "QR_STICKERS" -> stringResource(R.string.create_opt_stickers)
        "QR_WATERMARK" -> stringResource(R.string.create_opt_watermark)
        "QR_ANIMATED" -> stringResource(R.string.create_opt_animated)
        "QR_TEXT" -> stringResource(R.string.option_text)
        "QR_CODE" -> stringResource(R.string.scan_type_created)
        else -> name
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(displayName, Modifier.padding(horizontal = 16.dp, vertical = 12.dp), style = MaterialTheme.typography.labelLarge)
    }
}
