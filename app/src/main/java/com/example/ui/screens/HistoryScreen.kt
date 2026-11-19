package com.example.ui.screens
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Download
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun HistoryScreen(
    viewModel: MainViewModel, 
    onNavigateToCreate: () -> Unit = {},
    onEditCode: (com.example.data.CodeEntity) -> Unit = {}
) {
    val history by viewModel.history.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, SCAN, CREATE, FAVORITE
    var showDialogWipeConfirm by remember { mutableStateOf(false) }
    var qrDialogContent by remember { mutableStateOf<com.example.data.CodeEntity?>(null) }

    val filteredHistory = remember(history, searchQuery, selectedFilter) {
        var result = history
        result = when (selectedFilter) {
            "SCAN" -> result.filter { it.type == "SCAN" }
            "CREATE" -> result.filter { it.type != "SCAN" }
            "FAVORITE" -> result.filter { it.isFavorite }
            else -> result // ALL
        }

        if (searchQuery.isNotBlank()) {
            result = result.filter {
                it.content.contains(searchQuery, ignoreCase = true) ||
                it.type.contains(searchQuery, ignoreCase = true) ||
                it.format.contains(searchQuery, ignoreCase = true)
            }
        }
        result
    }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.history_search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            var showExportMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showExportMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = showExportMenu,
                    onDismissRequest = { showExportMenu = false },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.98f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("📊 Exportar Historial a CSV", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium) },
                        onClick = {
                            showExportMenu = false
                            try {
                                val sb = StringBuilder()
                                sb.append("ID,Contenido,Formato,Tipo,Favorito,Fecha\n")
                                history.forEach { code ->
                                    val safeContent = code.content.replace("\"", "\"\"")
                                    sb.append("${code.id},\"$safeContent\",${code.format},${code.type},${code.isFavorite},${code.timestamp}\n")
                                }
                                val cachePath = java.io.File(context.cacheDir, "shared_images").apply { mkdirs() }
                                val csvFile = java.io.File(cachePath, "Historial_QR_Barcodes.csv")
                                csvFile.writeText(sb.toString(), Charsets.UTF_8)
                                val contentUri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    csvFile
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "Historial_QR_Barcodes.csv")
                                    putExtra(Intent.EXTRA_STREAM, contentUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Exportar Historial CSV"))
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Error exportando historial", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🗑️ Borrar Todo el Historial", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium) },
                        onClick = {
                            showExportMenu = false
                            showDialogWipeConfirm = true
                        }
                    )
                }
            }
        }

        // Filter Chips
        val chipColors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text(stringResource(R.string.history_filter_all), fontWeight = if (selectedFilter == "ALL") FontWeight.Bold else FontWeight.Normal) },
                shape = RoundedCornerShape(16.dp),
                colors = chipColors
            )
            FilterChip(
                selected = selectedFilter == "SCAN",
                onClick = { selectedFilter = "SCAN" },
                label = { Text(stringResource(R.string.history_filter_scanned), fontWeight = if (selectedFilter == "SCAN") FontWeight.Bold else FontWeight.Normal) },
                shape = RoundedCornerShape(16.dp),
                colors = chipColors
            )
            FilterChip(
                selected = selectedFilter == "CREATE",
                onClick = { selectedFilter = "CREATE" },
                label = { Text(stringResource(R.string.history_filter_generated), fontWeight = if (selectedFilter == "CREATE") FontWeight.Bold else FontWeight.Normal) },
                shape = RoundedCornerShape(16.dp),
                colors = chipColors
            )
            FilterChip(
                selected = selectedFilter == "FAVORITE",
                onClick = { selectedFilter = "FAVORITE" },
                label = { Text(stringResource(R.string.history_filter_favorite), fontWeight = if (selectedFilter == "FAVORITE") FontWeight.Bold else FontWeight.Normal) },
                shape = RoundedCornerShape(16.dp),
                colors = chipColors
            )

        }
        Spacer(modifier = Modifier.height(16.dp))

        if (filteredHistory.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).padding(bottom = 8.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.history_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.history_empty_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToCreate,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(stringResource(R.string.history_create_button))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredHistory, key = { it.id }) { item ->
                    HistoryItemRow(
                        item = item,
                        onDelete = { viewModel.deleteCode(item.id) },
                        onShare = {
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, item.content)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.btn_share)))
                            } catch (e: Exception) {
                                Toast.makeText(context, "No hay aplicación disponible para compartir", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onViewQr = { qrDialogContent = item },
                        onEdit = { onEditCode(item) },
                        onUpdate = { viewModel.updateCode(it) }
                    )
                }
            }
        }
    }

    qrDialogContent?.let { code ->
        ViewQrDialog(
            code = code,
            onDismiss = { qrDialogContent = null },
            onEdit = { onEditCode(code) }
        )
    }

    if (showDialogWipeConfirm) {
        AlertDialog(
            onDismissRequest = { showDialogWipeConfirm = false },
            title = { Text(stringResource(R.string.dialog_title_clear_history)) },
            text = { Text(stringResource(R.string.dialog_desc_clear_history)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllCodes()
                    showDialogWipeConfirm = false
                }) {
                    Text(stringResource(R.string.btn_confirm_wipe), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogWipeConfirm = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun HistoryItemRow(
    item: com.example.data.CodeEntity,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onViewQr: () -> Unit,
    onEdit: () -> Unit = {},
    onUpdate: (com.example.data.CodeEntity) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }

    if (showFolderDialog) {
        var folderName by remember { mutableStateOf(item.folderName ?: "") }
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = { Text(stringResource(R.string.dialog_assign_folder)) },
            text = {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text(stringResource(R.string.input_folder_name)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdate(item.copy(folderName = folderName.takeIf { it.isNotBlank() }))
                    showFolderDialog = false
                }) {
                    Text(stringResource(R.string.btn_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFolderDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
    val clipboardManager = LocalClipboardManager.current

    val isUrl = remember(item.content) {
        android.util.Patterns.WEB_URL.matcher(item.content).matches() ||
                item.content.startsWith("http://") ||
                item.content.startsWith("https://")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val icon = if (item.format == "QR_CODE") Icons.Default.QrCode else Icons.Default.Dataset
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${stringResource(if (item.type == "SCAN") R.string.scan_type_scanned else R.string.scan_type_created)} · ${item.format.replace('_', ' ')}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.content,
                        maxLines = if (expanded) 10 else 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = { onUpdate(item.copy(isFavorite = !item.isFavorite)) }) {
                    Icon(
                        imageVector = if (item.isFavorite) androidx.compose.material.icons.Icons.Filled.Star else androidx.compose.material.icons.Icons.Filled.StarBorder,
                        contentDescription = "Favorito",
                        tint = if (item.isFavorite) androidx.compose.ui.graphics.Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { showFolderDialog = true }) {
                            Icon(Icons.Default.CreateNewFolder, stringResource(R.string.dialog_assign_folder))
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, stringResource(R.string.btn_delete), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick copy key button
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(item.content))
                                Toast.makeText(context, context.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.btn_copy), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_copy), style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Full Editor action button
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Share key button
                        OutlinedButton(
                            onClick = onShare,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.btn_send), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_send), style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Web Browser action button
                        if (isUrl) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val url = if (item.content.startsWith("http")) item.content else "https://${item.content}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, context.getString(R.string.toast_cannot_open_link), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = stringResource(R.string.btn_open), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.btn_open), style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        // Display QR Dialog button
                        OutlinedButton(
                            onClick = onViewQr,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Visibility, contentDescription = stringResource(R.string.btn_view_qr), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_view_qr), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun ViewQrDialog(
    code: com.example.data.CodeEntity,
    onDismiss: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val barcodeFormat = try {
        com.google.zxing.BarcodeFormat.valueOf(code.format)
    } catch (e: Exception) {
        com.google.zxing.BarcodeFormat.QR_CODE
    }

    val bitmap = remember(code.content, barcodeFormat, code.id) {
        val savedFile = java.io.File(context.filesDir, "qr_${code.id}.png")
        if (savedFile.exists()) {
            val boundsOptions = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeFile(savedFile.absolutePath, boundsOptions)

            val reqSize = 512
            var inSampleSize = 1
            if (boundsOptions.outWidth > reqSize || boundsOptions.outHeight > reqSize) {
                val halfHeight = boundsOptions.outHeight / 2
                val halfWidth = boundsOptions.outWidth / 2
                while ((halfHeight / inSampleSize) >= reqSize && (halfWidth / inSampleSize) >= reqSize) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
            }
            android.graphics.BitmapFactory.decodeFile(savedFile.absolutePath, decodeOptions)
                ?: com.example.utils.CodeGenerator.generateBitmap(code.content, barcodeFormat)
        } else {
            com.example.utils.CodeGenerator.generateBitmap(code.content, barcodeFormat)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.title_code_saved),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                bitmap?.let { b ->
                    Image(
                        bitmap = b.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp)
                            .padding(4.dp)
                    )
                } ?: Text(stringResource(R.string.toast_cant_generate))

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = code.content,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            bitmap?.takeIf { !it.isRecycled }?.let { b ->
                                try {
                                    val fName = "QR_${System.currentTimeMillis()}.png"
                                    val bytes = java.io.ByteArrayOutputStream().use { baos ->
                                        b.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos)
                                        baos.toByteArray()
                                    }

                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                        val resolver = context.contentResolver
                                        val contentValues = android.content.ContentValues().apply {
                                            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fName)
                                            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
                                            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                                        }
                                        val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                                        val stream = uri?.let { resolver.openOutputStream(it) }
                                        if (stream != null) {
                                            stream.use { os -> os.write(bytes) }
                                            android.widget.Toast.makeText(context, context.getString(R.string.toast_saved, fName), android.widget.Toast.LENGTH_LONG).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Error al guardar la imagen", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                                        if (!downloadsDir.exists()) downloadsDir.mkdirs()
                                        val file = java.io.File(downloadsDir, fName)
                                        java.io.FileOutputStream(file).use { os ->
                                            os.write(bytes)
                                        }
                                        android.widget.Toast.makeText(context, context.getString(R.string.toast_saved, fName), android.widget.Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    android.widget.Toast.makeText(context, "Error al guardar: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_download), style = MaterialTheme.typography.labelLarge)
                    }

                    if (onEdit != null) {
                        FilledTonalButton(
                            onClick = {
                                onDismiss()
                                onEdit()
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.home_edit_qr), style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text(stringResource(R.string.btn_close), style = MaterialTheme.typography.labelLarge)
                    }
                }

            }
        }
    }
}
