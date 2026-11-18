package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.MainViewModel
import com.example.utils.BodyShape
import com.example.utils.TemplateCustomization
import com.example.utils.TemplateItemDef
import com.example.utils.TemplateRenderer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.io.FileOutputStream

enum class TextTarget(val title: String, val subtitle: String) {
    HEADER_ACCENT("Texto Superior / Marca", "Marca, empresa o cabecera superior"),
    MAIN_TITLE("Título Principal", "Texto principal destacado (ej: ESCANÉAME, MENÚ, WIFI)"),
    SUBTEXT("Subtítulo / Instrucciones", "Texto explicativo o secundario"),
    FOOTER_ACCENT("Pie de Página / Logo", "Texto de pie, sitio web o marca inferior"),
    CREATE_NEW("Crear / Añadir Texto", "Crear o insertar un nuevo texto predefinido")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun TemplatesScreen(
    viewModel: MainViewModel? = null,
    onApplyTemplateDirect: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedTemplateDef by remember { mutableStateOf<TemplateItemDef?>(null) }

    // Editor state
    var qrContent by remember { mutableStateOf("https://google.com") }
    var titleText by remember { mutableStateOf("ESCANÉAME") }
    var headerAccentText by remember { mutableStateOf("") }
    var footerAccentText by remember { mutableStateOf("") }
    var textSizeSp by remember { mutableFloatStateOf(24f) }
    var textAlign by remember { mutableStateOf(Paint.Align.CENTER) }
    var textOffsetY by remember { mutableFloatStateOf(0f) }
    var textOffsetX by remember { mutableFloatStateOf(0f) }
    var selectedTextColor by remember { mutableIntStateOf(Color.BLACK) }
    var isBold by remember { mutableStateOf(true) }
    var isUppercase by remember { mutableStateOf(true) }
    
    var subtext by remember { mutableStateOf("Apunta con la cámara de tu móvil") }
    var subtextSizeSp by remember { mutableFloatStateOf(13f) }
    var selectedSubtextColor by remember { mutableIntStateOf(Color.DKGRAY) }
    var showSubtext by remember { mutableStateOf(true) }
    
    var qrScale by remember { mutableFloatStateOf(1.0f) }
    var qrOffsetY by remember { mutableFloatStateOf(0f) }
    var qrBodyShape by remember { mutableStateOf(BodyShape.SQUARE) }
    var qrForegroundColor by remember { mutableIntStateOf(Color.BLACK) }
    var qrBackgroundColor by remember { mutableIntStateOf(Color.WHITE) }
    var qrLogoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var removeLogo by remember { mutableStateOf(false) }

    // Dialog state for Double Click / Long Press / Button creation
    var showQuickEditDialog by remember { mutableStateOf(false) }
    var activeEditTarget by remember { mutableStateOf(TextTarget.MAIN_TITLE) }
    var showTextStudioDialog by remember { mutableStateOf(false) }

    // Storage permissions for saving JPG/PNG
    val storagePermissionState = rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    var pendingBitmapFormat by remember { mutableStateOf<Bitmap.CompressFormat?>(null) }

    // Live Rendered Bitmap
    var liveRenderedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Hybrid Custom Frame Upload (Premium Feature)
    val activity = context as? android.app.Activity
    val billingManager = remember(context) { com.example.billing.BillingManager(context) }
    androidx.compose.runtime.DisposableEffect(billingManager) {
        onDispose { billingManager.terminate() }
    }
    val isPro by billingManager.isPro.collectAsState()
    var showPaywallDialog by remember { mutableStateOf(false) }
    var customFrameBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var removeCustomFrame by remember { mutableStateOf(false) }

    val customFramePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            removeCustomFrame = false
            try {
                val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    android.graphics.BitmapFactory.decodeStream(it, null, bounds)
                }
                val maxDim = 1280
                var sampleSize = 1
                if (bounds.outWidth > maxDim || bounds.outHeight > maxDim) {
                    val halfH = bounds.outHeight / 2
                    val halfW = bounds.outWidth / 2
                    while ((halfH / sampleSize) >= maxDim && (halfW / sampleSize) >= maxDim) {
                        sampleSize *= 2
                    }
                }
                val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                customFrameBitmap = context.contentResolver.openInputStream(uri)?.use {
                    android.graphics.BitmapFactory.decodeStream(it, null, decodeOptions)
                }
                Toast.makeText(context, "¡Marco personalizado cargado exitosamente!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error cargando marco: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val logoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            removeLogo = false
            try {
                val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    android.graphics.BitmapFactory.decodeStream(it, null, bounds)
                }
                val maxDim = 256
                var sampleSize = 1
                if (bounds.outWidth > maxDim || bounds.outHeight > maxDim) {
                    val halfH = bounds.outHeight / 2
                    val halfW = bounds.outWidth / 2
                    while ((halfH / sampleSize) >= maxDim && (halfW / sampleSize) >= maxDim) {
                        sampleSize *= 2
                    }
                }
                val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                qrLogoBitmap = context.contentResolver.openInputStream(uri)?.use {
                    android.graphics.BitmapFactory.decodeStream(it, null, decodeOptions)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Effect to render live template whenever settings change
    LaunchedEffect(
        selectedTemplateDef, qrContent, titleText, headerAccentText, footerAccentText,
        textSizeSp, textAlign, textOffsetY, textOffsetX, selectedTextColor, isBold, isUppercase,
        subtext, subtextSizeSp, selectedSubtextColor, showSubtext,
        qrScale, qrOffsetY, qrBodyShape, qrForegroundColor, qrBackgroundColor,
        qrLogoBitmap, removeLogo, customFrameBitmap, removeCustomFrame
    ) {
        val currentDef = selectedTemplateDef ?: TemplateRenderer.AVAILABLE_TEMPLATES.first()
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val customization = TemplateCustomization(
                templateDef = currentDef,
                qrContent = qrContent,
                titleText = titleText,
                headerAccentText = headerAccentText.ifBlank { null },
                footerAccentText = footerAccentText.ifBlank { null },
                textSizeSp = textSizeSp,
                textAlign = textAlign,
                textOffsetY = textOffsetY,
                textOffsetX = textOffsetX,
                textColor = selectedTextColor,
                subtext = subtext,
                subtextSizeSp = subtextSizeSp,
                subtextColor = selectedSubtextColor,
                qrForegroundColor = qrForegroundColor,
                qrBackgroundColor = qrBackgroundColor,
                logoBitmap = if (removeLogo) null else qrLogoBitmap,
                customFrameBitmap = if (removeCustomFrame) null else customFrameBitmap
            )
            val rendered = TemplateRenderer.renderTemplateBitmap(context, customization, 768, 960)
            liveRenderedBitmap = rendered
        }
    }

    LaunchedEffect(storagePermissionState.status) {
        if (storagePermissionState.status.isGranted && pendingBitmapFormat != null) {
            val currentBmp = liveRenderedBitmap
            if (currentBmp != null) {
                saveTemplateBitmapToGallery(context, currentBmp, pendingBitmapFormat!!)
            }
            pendingBitmapFormat = null
        }
    }

    // Color Swatches
    val colorPalette = remember {
        listOf(
            "Negro" to Color.BLACK,
            "Blanco" to Color.WHITE,
            "Azul" to Color.parseColor("#2563EB"),
            "Dorado" to Color.parseColor("#F59E0B"),
            "Rojo" to Color.parseColor("#DC2626"),
            "Verde" to Color.parseColor("#16A34A"),
            "Neón" to Color.parseColor("#06B6D4"),
            "Púrpura" to Color.parseColor("#9333EA"),
            "Carbón" to Color.parseColor("#1E293B")
        )
    }

    val bodyShapes = remember {
        listOf(
            "Cuadrado" to BodyShape.SQUARE,
            "Redondeado" to BodyShape.ROUNDED,
            "Círculo" to BodyShape.CIRCLE,
            "Puntos" to BodyShape.DOT,
            "Diamante" to BodyShape.DIAMOND,
            "Corazón" to BodyShape.HEART,
            "Estrella" to BodyShape.STAR,
            "Hexágono" to BodyShape.HEXAGON
        )
    }

    if (showPaywallDialog) {
        PaywallDialog(
            onDismiss = { showPaywallDialog = false },
            onPurchaseLifetime = {
                showPaywallDialog = false
                activity?.let { billingManager.launchBillingFlow(it, com.example.billing.BillingManager.PRO_LIFETIME) }
            },
            onPurchaseMonthly = {
                showPaywallDialog = false
                activity?.let { billingManager.launchBillingFlow(it, com.example.billing.BillingManager.PRO_MONTHLY) }
            },
            onPurchaseYearly = {
                showPaywallDialog = false
                activity?.let { billingManager.launchBillingFlow(it, com.example.billing.BillingManager.PRO_YEARLY) }
            }
        )
    }

    if (selectedTemplateDef == null) {
        // --- 1. TEMPLATE GALLERY VIEW ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.template_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalButton(
                    onClick = {
                        if (isPro) customFramePickerLauncher.launch("image/*")
                        else showPaywallDialog = true
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.upload_frame) + " · Pro")
                }
            }
            Spacer(Modifier.height(20.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(144.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(TemplateRenderer.AVAILABLE_TEMPLATES) { template ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTemplateDef = template
                                titleText = template.defaultText
                                headerAccentText = template.defaultHeaderAccent ?: ""
                                footerAccentText = template.defaultFooterAccent ?: ""
                                subtext = template.defaultSubtext
                                selectedTextColor = template.defaultTextColor
                                selectedSubtextColor = if (template.isDarkTheme) Color.LTGRAY else Color.DKGRAY
                                qrForegroundColor = Color.BLACK
                                qrBackgroundColor = Color.WHITE
                                textSizeSp = if (template.id == "escanea_aqui") 22f else 24f
                                subtextSizeSp = 13f
                                textOffsetY = 0f
                                textOffsetX = 0f
                                qrScale = 1.0f
                                qrOffsetY = 0f
                                qrBodyShape = BodyShape.SQUARE
                                textAlign = if (template.id == "escanea_aqui") Paint.Align.LEFT else Paint.Align.CENTER
                                isBold = true
                                isUppercase = template.id != "escanea_aqui"
                                showSubtext = template.id != "escanea_aqui" && template.defaultSubtext.isNotBlank()
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TemplateThumbnail(
                                template = template,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.8f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = template.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 16.sp,
                                        lineHeight = 22.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = template.category,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- 2. TEMPLATE EDITOR & CUSTOMIZER VIEW ---
        val scrollState = rememberScrollState()
        val template = selectedTemplateDef!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            // Header with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedTemplateDef = null }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.btn_back)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Editor: ${template.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Fixed Sticky Live Preview Card (QR queda fijo sin moverse al deslizar las opciones)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        activeEditTarget = TextTarget.MAIN_TITLE
                                        showQuickEditDialog = true
                                    },
                                    onLongPress = {
                                        activeEditTarget = TextTarget.MAIN_TITLE
                                        showTextStudioDialog = true
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val currentBmp = liveRenderedBitmap
                        if (currentBmp != null) {
                            Image(
                                bitmap = currentBmp.asImageBitmap(),
                                contentDescription = "Vista Previa de Plantilla",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(androidx.compose.ui.graphics.Color.LightGray, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Gesture Hint & Quick Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡 Doble toque: editar | Mantener: ventana de texto",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(
                            onClick = { showTextStudioDialog = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Ventana de Texto", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Interactive quick badges for texts
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            AssistChip(
                                onClick = {
                                    activeEditTarget = TextTarget.MAIN_TITLE
                                    showQuickEditDialog = true
                                },
                                label = { Text("✨ Título: ${titleText.take(15)}") },
                                leadingIcon = {
                                    Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )
                        }
                        if (headerAccentText.isNotBlank()) {
                            item {
                                AssistChip(
                                    onClick = {
                                        activeEditTarget = TextTarget.HEADER_ACCENT
                                        showQuickEditDialog = true
                                    },
                                    label = { Text("📌 Cabecera: ${headerAccentText.take(12)}") }
                                )
                            }
                        }
                        if (showSubtext && subtext.isNotBlank()) {
                            item {
                                AssistChip(
                                    onClick = {
                                        activeEditTarget = TextTarget.SUBTEXT
                                        showQuickEditDialog = true
                                    },
                                    label = { Text("📝 Sub: ${subtext.take(12)}") }
                                )
                            }
                        }
                        if (footerAccentText.isNotBlank()) {
                            item {
                                AssistChip(
                                    onClick = {
                                        activeEditTarget = TextTarget.FOOTER_ACCENT
                                        showQuickEditDialog = true
                                    },
                                    label = { Text("🔻 Pie: ${footerAccentText.take(12)}") }
                                )
                            }
                        }
                        item {
                            FilledTonalButton(
                                onClick = {
                                    activeEditTarget = TextTarget.CREATE_NEW
                                    showTextStudioDialog = true
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("➕ Crear Texto", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // --- MAIN ACTION BUTTONS (Crear Texto, Ventana de Texto, Guardar, PDF, JPG, Share) ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dedicated Text Studio and Creation Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                activeEditTarget = TextTarget.MAIN_TITLE
                                showTextStudioDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Ventana de Texto")
                        }

                        Button(
                            onClick = {
                                activeEditTarget = TextTarget.CREATE_NEW
                                showTextStudioDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Crear / Añadir Texto")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val currentBmp = liveRenderedBitmap
                                if (currentBmp != null) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || storagePermissionState.status.isGranted) {
                                        saveTemplateBitmapToGallery(context, currentBmp, Bitmap.CompressFormat.PNG)
                                    } else {
                                        pendingBitmapFormat = Bitmap.CompressFormat.PNG
                                        storagePermissionState.launchPermissionRequest()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Descargar PNG")
                        }

                        Button(
                            onClick = {
                                val currentBmp = liveRenderedBitmap
                                if (currentBmp != null) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || storagePermissionState.status.isGranted) {
                                        saveTemplateBitmapToGallery(context, currentBmp, Bitmap.CompressFormat.JPEG)
                                    } else {
                                        pendingBitmapFormat = Bitmap.CompressFormat.JPEG
                                        storagePermissionState.launchPermissionRequest()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Descargar JPG")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val currentBmp = liveRenderedBitmap
                                if (currentBmp != null) {
                                    exportTemplatePdf(context, currentBmp, template.name, titleText, qrContent)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Exportar PDF (12 Stickers)")
                        }

                        OutlinedButton(
                            onClick = {
                                val currentBmp = liveRenderedBitmap
                                if (currentBmp != null) {
                                    shareTemplateBitmap(context, currentBmp)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Compartir")
                        }
                    }

                    if (viewModel != null) {
                        FilledTonalButton(
                            onClick = {
                                val currentBmp = liveRenderedBitmap
                                if (currentBmp != null) {
                                    viewModel.saveCode(
                                        content = qrContent,
                                        format = "QR_CODE",
                                        type = "TEMPLATE_${template.id.uppercase()}",
                                        bitmap = currentBmp
                                    )
                                    Toast.makeText(context, "Plantilla guardada en el historial", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Guardar en Historial")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // --- CONTROLS SECTION 1: QR CODE CONTENT & SHAPE ---
                Text(
                    text = "1. Contenido y Código QR (Centrado)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = qrContent,
                    onValueChange = { qrContent = it },
                    label = { Text("Contenido del QR (Enlace o Texto)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // QR Body Shape selector
                Text(
                    text = "Forma del cuerpo del QR:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bodyShapes) { (label, shape) ->
                        FilterChip(
                            selected = qrBodyShape == shape,
                            onClick = { qrBodyShape = shape },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // QR Scale slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Escala del QR dentro del marco:", style = MaterialTheme.typography.bodyMedium)
                    Text("${(qrScale * 100).toInt()}%", fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = qrScale,
                    onValueChange = { qrScale = it },
                    valueRange = 0.7f..1.15f
                )

                Spacer(modifier = Modifier.height(6.dp))

                // QR Vertical Offset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ajuste vertical del QR:", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${qrOffsetY.toInt()} px", fontWeight = FontWeight.Bold)
                        if (qrOffsetY != 0f) {
                            Spacer(Modifier.width(6.dp))
                            TextButton(onClick = { qrOffsetY = 0f }, contentPadding = PaddingValues(horizontal = 4.dp)) {
                                Text("Centrar", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Slider(
                    value = qrOffsetY,
                    onValueChange = { qrOffsetY = it },
                    valueRange = -40f..40f
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Logo in QR center
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { logoPickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Logo en centro")
                    }

                    if (qrLogoBitmap != null) {
                        OutlinedButton(
                            onClick = { removeLogo = !removeLogo },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (removeLogo) "Restaurar Logo" else "Quitar Logo")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Frame / Background (Premium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            if (isPro) {
                                customFramePickerLauncher.launch("image/*")
                            } else {
                                showPaywallDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Wallpaper, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (customFrameBitmap != null) "Cambiar Marco (PNG)" else "Marco Personalizado (PRO)")
                    }

                    if (customFrameBitmap != null) {
                        OutlinedButton(
                            onClick = { removeCustomFrame = !removeCustomFrame },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (removeCustomFrame) "Restaurar Marco" else "Quitar Marco PNG")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // --- CONTROLS SECTION 2: TEMPLATE TEXT CUSTOMIZATION ---
                Text(
                    text = "2. Textos y Títulos de la Plantilla",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Header Accent (Top brand/text)
                OutlinedTextField(
                    value = headerAccentText,
                    onValueChange = { headerAccentText = it },
                    label = { Text("Texto Superior / Marca (opcional)") },
                    placeholder = { Text("Ej: Fradel and Spies Co / PARA PAGAR") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Main Title
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Título Principal (ej: ESCANÉAME, SCAN HERE, MENÚ)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Text Styling (Bold, Uppercase)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isBold,
                        onClick = { isBold = !isBold },
                        label = { Text("Negrita") },
                        leadingIcon = if (isBold) {
                            { Icon(Icons.Default.FormatBold, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isUppercase,
                        onClick = { isUppercase = !isUppercase },
                        label = { Text("MAYÚSCULAS") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Text Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tamaño del título:", style = MaterialTheme.typography.bodyMedium)
                    Text("${textSizeSp.toInt()} sp", fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = textSizeSp,
                    onValueChange = { textSizeSp = it },
                    valueRange = 14f..42f
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Text Alignment Controls
                Text(
                    text = "Alineación del texto:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = textAlign == Paint.Align.LEFT,
                        onClick = { textAlign = Paint.Align.LEFT },
                        label = { Text("Izquierda") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = textAlign == Paint.Align.CENTER,
                        onClick = { textAlign = Paint.Align.CENTER },
                        label = { Text("Centrado") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = textAlign == Paint.Align.RIGHT,
                        onClick = { textAlign = Paint.Align.RIGHT },
                        label = { Text("Derecha") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Position offsets with Reset button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Posición del texto (X: ${textOffsetX.toInt()}px, Y: ${textOffsetY.toInt()}px):", style = MaterialTheme.typography.bodyMedium)
                    if (textOffsetX != 0f || textOffsetY != 0f) {
                        TextButton(
                            onClick = {
                                textOffsetX = 0f
                                textOffsetY = 0f
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("Restablecer a 0", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Move Text Vertically (Offset Y)
                Text("Desplazamiento Vertical (Y):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = textOffsetY,
                    onValueChange = { textOffsetY = it },
                    valueRange = -60f..60f
                )

                // Move Text Horizontally (Offset X)
                Text("Desplazamiento Horizontal (X):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = textOffsetX,
                    onValueChange = { textOffsetX = it },
                    valueRange = -80f..80f
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Text Color Palette
                Text(
                    text = "Color del texto principal:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colorPalette) { (name, colorVal) ->
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(androidx.compose.ui.graphics.Color(colorVal))
                                .border(
                                    width = if (selectedTextColor == colorVal) 3.dp else 1.dp,
                                    color = if (selectedTextColor == colorVal) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { selectedTextColor = colorVal }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // --- CONTROLS SECTION 3: SUBTITLE / FOOTER TEXT ---
                Text(
                    text = "3. Subtítulo y Pie de Página",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mostrar subtítulo", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = showSubtext, onCheckedChange = { showSubtext = it })
                }

                if (showSubtext) {
                    OutlinedTextField(
                        value = subtext,
                        onValueChange = { subtext = it },
                        label = { Text("Subtítulo (ej: ¡GRACIAS POR ELEGIRNOS! / Apunta con tu cámara)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tamaño del subtítulo:", style = MaterialTheme.typography.bodyMedium)
                        Text("${subtextSizeSp.toInt()} sp", fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = subtextSizeSp,
                        onValueChange = { subtextSizeSp = it },
                        valueRange = 10f..22f
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer Accent (Bottom logo/text)
                OutlinedTextField(
                    value = footerAccentText,
                    onValueChange = { footerAccentText = it },
                    label = { Text("Texto de Pie / Logo inferior (opcional)") },
                    placeholder = { Text("Ej: TU LOGO AQUÍ / www.misitio.com") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // --- QUICK EDIT DIALOG (Doble Toque / Selección Rápida) ---
    if (showQuickEditDialog) {
        QuickEditDialog(
            activeTarget = activeEditTarget,
            onTargetChange = { activeEditTarget = it },
            titleText = titleText,
            onTitleChange = { titleText = it },
            headerAccentText = headerAccentText,
            onHeaderAccentChange = { headerAccentText = it },
            subtext = subtext,
            onSubtextChange = { subtext = it },
            footerAccentText = footerAccentText,
            onFooterAccentChange = { footerAccentText = it },
            textSizeSp = textSizeSp,
            onTextSizeChange = { textSizeSp = it },
            onBoldChange = { isBold = it },
            onUppercaseChange = { isUppercase = it },
            selectedTextColor = selectedTextColor,
            onTextColorChange = { selectedTextColor = it },
            colorPalette = colorPalette,
            isBold = true,
            isUppercase = true,
            onOpenFullStudio = {
                showQuickEditDialog = false
                showTextStudioDialog = true
            },
            onDismiss = { showQuickEditDialog = false }
        )
    }

    // --- TEXT STUDIO DIALOG (Ventana de Texto Completa / Mantener Presionado / Botón Crear Texto) ---
    if (showTextStudioDialog) {
        TextStudioDialog(
            initialTarget = activeEditTarget,
            titleText = titleText,
            onTitleChange = { titleText = it },
            headerAccentText = headerAccentText,
            onHeaderAccentChange = { headerAccentText = it },
            subtext = subtext,
            onSubtextChange = { subtext = it },
            footerAccentText = footerAccentText,
            onFooterAccentChange = { footerAccentText = it },
            showSubtext = showSubtext,
            onShowSubtextChange = { showSubtext = it },
            textSizeSp = textSizeSp,
            onTextSizeChange = { textSizeSp = it },
            subtextSizeSp = subtextSizeSp,
            onSubtextSizeChange = { subtextSizeSp = it },
            textAlign = textAlign,
            onTextAlignChange = { textAlign = it },
            textOffsetY = textOffsetY,
            onTextOffsetYChange = { textOffsetY = it },
            textOffsetX = textOffsetX,
            onTextOffsetXChange = { textOffsetX = it },
            isBold = isBold,
            onBoldChange = { isBold = it },
            isUppercase = isUppercase,
            onUppercaseChange = { isUppercase = it },
            selectedTextColor = selectedTextColor,
            onTextColorChange = { selectedTextColor = it },
            colorPalette = colorPalette,
            onResetDefaults = {
                val def = selectedTemplateDef ?: TemplateRenderer.AVAILABLE_TEMPLATES.first()
                titleText = def.defaultText
                headerAccentText = def.defaultHeaderAccent ?: ""
                footerAccentText = def.defaultFooterAccent ?: ""
                subtext = def.defaultSubtext
                selectedTextColor = def.defaultTextColor
                textSizeSp = 24f
                subtextSizeSp = 13f
                textOffsetY = 0f
                textOffsetX = 0f
                textAlign = Paint.Align.CENTER
                isBold = true
                isUppercase = true
            },
            onDismiss = { showTextStudioDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEditDialog(
    activeTarget: TextTarget,
    onTargetChange: (TextTarget) -> Unit,
    titleText: String,
    onTitleChange: (String) -> Unit,
    headerAccentText: String,
    onHeaderAccentChange: (String) -> Unit,
    subtext: String,
    onSubtextChange: (String) -> Unit,
    footerAccentText: String,
    onFooterAccentChange: (String) -> Unit,
    textSizeSp: Float,
    onTextSizeChange: (Float) -> Unit,
    isBold: Boolean,
    onBoldChange: (Boolean) -> Unit,
    isUppercase: Boolean,
    onUppercaseChange: (Boolean) -> Unit,
    selectedTextColor: Int,
    onTextColorChange: (Int) -> Unit,
    colorPalette: List<Pair<String, Int>>,
    onOpenFullStudio: () -> Unit,
    onDismiss: () -> Unit
) {
    val quickPresets = listOf(
        "ESCANÉAME", "SCAN HERE", "PAGA AQUÍ", "WIFI GRATIS",
        "MENÚ DIGITAL", "SÍGUENOS", "VALORA AQUÍ", "RESERVA TU MESA",
        "DESCUENTO 10%", "TU LOGO AQUÍ", "CONTÁCTANOS", "INFO & HORARIOS"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.98f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "✏️ Edición Rápida de Texto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = activeTarget.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target Selector Chips
                val chipColors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = activeTarget == TextTarget.MAIN_TITLE,
                            onClick = { onTargetChange(TextTarget.MAIN_TITLE) },
                            label = { Text("✨ Título") },
                            colors = chipColors
                        )
                    }
                    item {
                        FilterChip(
                            selected = activeTarget == TextTarget.HEADER_ACCENT,
                            onClick = { onTargetChange(TextTarget.HEADER_ACCENT) },
                            label = { Text("📌 Cabecera") },
                            colors = chipColors
                        )
                    }
                    item {
                        FilterChip(
                            selected = activeTarget == TextTarget.SUBTEXT,
                            onClick = { onTargetChange(TextTarget.SUBTEXT) },
                            label = { Text("📝 Subtítulo") },
                            colors = chipColors
                        )
                    }
                    item {
                        FilterChip(
                            selected = activeTarget == TextTarget.FOOTER_ACCENT,
                            onClick = { onTargetChange(TextTarget.FOOTER_ACCENT) },
                            label = { Text("🔻 Pie") },
                            colors = chipColors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Editable Field based on target
                val currentText = when (activeTarget) {
                    TextTarget.MAIN_TITLE -> titleText
                    TextTarget.HEADER_ACCENT -> headerAccentText
                    TextTarget.SUBTEXT -> subtext
                    TextTarget.FOOTER_ACCENT -> footerAccentText
                    TextTarget.CREATE_NEW -> titleText
                }

                OutlinedTextField(
                    value = currentText,
                    onValueChange = { newText ->
                        when (activeTarget) {
                            TextTarget.MAIN_TITLE -> onTitleChange(newText)
                            TextTarget.HEADER_ACCENT -> onHeaderAccentChange(newText)
                            TextTarget.SUBTEXT -> onSubtextChange(newText)
                            TextTarget.FOOTER_ACCENT -> onFooterAccentChange(newText)
                            TextTarget.CREATE_NEW -> onTitleChange(newText)
                        }
                    },
                    label = { Text(activeTarget.title) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (currentText.isNotEmpty()) {
                            IconButton(onClick = {
                                when (activeTarget) {
                                    TextTarget.MAIN_TITLE -> onTitleChange("")
                                    TextTarget.HEADER_ACCENT -> onHeaderAccentChange("")
                                    TextTarget.SUBTEXT -> onSubtextChange("")
                                    TextTarget.FOOTER_ACCENT -> onFooterAccentChange("")
                                    TextTarget.CREATE_NEW -> onTitleChange("")
                                }
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Borrar texto")
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Popular Presets
                Text(
                    text = "Textos sugeridos con 1 toque:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(quickPresets) { preset ->
                        SuggestionChip(
                            onClick = {
                                when (activeTarget) {
                                    TextTarget.MAIN_TITLE -> onTitleChange(preset)
                                    TextTarget.HEADER_ACCENT -> onHeaderAccentChange(preset)
                                    TextTarget.SUBTEXT -> onSubtextChange(preset)
                                    TextTarget.FOOTER_ACCENT -> onFooterAccentChange(preset)
                                    TextTarget.CREATE_NEW -> onTitleChange(preset)
                                }
                            },
                            label = { Text(preset, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Format Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isBold,
                        onClick = { onBoldChange(!isBold) },
                        label = { Text("Negrita") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isUppercase,
                        onClick = { onUppercaseChange(!isUppercase) },
                        label = { Text("MAYÚSCULAS") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Text Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tamaño del texto:", style = MaterialTheme.typography.bodySmall)
                    Text("${textSizeSp.toInt()} sp", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
                Slider(
                    value = textSizeSp,
                    onValueChange = onTextSizeChange,
                    valueRange = 14f..36f
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Color row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(colorPalette.take(8)) { (_, colorVal) ->
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(androidx.compose.ui.graphics.Color(colorVal))
                                .border(
                                    width = if (selectedTextColor == colorVal) 2.5.dp else 1.dp,
                                    color = if (selectedTextColor == colorVal) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { onTextColorChange(colorVal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenFullStudio,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ventana Completa", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aplicar y Listo", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextStudioDialog(
    initialTarget: TextTarget,
    titleText: String,
    onTitleChange: (String) -> Unit,
    headerAccentText: String,
    onHeaderAccentChange: (String) -> Unit,
    subtext: String,
    onSubtextChange: (String) -> Unit,
    footerAccentText: String,
    onFooterAccentChange: (String) -> Unit,
    showSubtext: Boolean,
    onShowSubtextChange: (Boolean) -> Unit,
    textSizeSp: Float,
    onTextSizeChange: (Float) -> Unit,
    subtextSizeSp: Float,
    onSubtextSizeChange: (Float) -> Unit,
    textAlign: Paint.Align,
    onTextAlignChange: (Paint.Align) -> Unit,
    textOffsetY: Float,
    onTextOffsetYChange: (Float) -> Unit,
    textOffsetX: Float,
    onTextOffsetXChange: (Float) -> Unit,
    isBold: Boolean,
    onBoldChange: (Boolean) -> Unit,
    isUppercase: Boolean,
    onUppercaseChange: (Boolean) -> Unit,
    selectedTextColor: Int,
    onTextColorChange: (Int) -> Unit,
    colorPalette: List<Pair<String, Int>>,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTarget by remember { mutableStateOf(initialTarget) }

    val presetTemplates = listOf(
        Pair("🍽️ Menú & Restaurante", Pair("MENÚ DIGITAL", "Escanea para ver la carta y ordenar")),
        Pair("💳 Pagos & Transferencias", Pair("PAGA AQUÍ", "Aceptamos tarjetas y transferencias")),
        Pair("📶 Wi-Fi Clientes", Pair("WIFI GRATIS", "Apunta con la cámara para conectar")),
        Pair("📱 Redes Sociales", Pair("SÍGUENOS", "Encuéntranos en Instagram y TikTok")),
        Pair("⭐ Reseñas & Calificaciones", Pair("VALÓRANOS", "Déjanos tu opinión en Google")),
        Pair("🏷️ Descuentos & Ofertas", Pair("¡OFERTA ESPECIAL!", "10% de descuento en tu compra")),
        Pair("📅 Reservas & Citas", Pair("RESERVA TU MESA", "Reserva online sin esperas")),
        Pair("📍 Ubicación & Contacto", Pair("CONTÁCTANOS", "Horarios, mapa y atención"))
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.88f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.98f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TextFields,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Ventana de Edición de Textos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Personaliza o crea textos para tu plantilla",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Selection Tabs
                val studioChipColors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(TextTarget.values()) { target ->
                        FilterChip(
                            selected = selectedTarget == target,
                            onClick = { selectedTarget = target },
                            label = { Text(target.title.split("/").first().trim(), fontWeight = if (selectedTarget == target) FontWeight.Bold else FontWeight.Normal) },
                            colors = studioChipColors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTarget) {
                        TextTarget.CREATE_NEW -> {
                            Text(
                                text = "➕ Crear / Insertar Textos Predefinidos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Toca un pack de texto para aplicarlo inmediatamente a tu plantilla:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            presetTemplates.forEach { (category, pair) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            onTitleChange(pair.first)
                                            onSubtextChange(pair.second)
                                            onShowSubtextChange(true)
                                            selectedTarget = TextTarget.MAIN_TITLE
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = category,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Título: \"${pair.first}\"",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "Sub: \"${pair.second}\"",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Aplicar",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        TextTarget.MAIN_TITLE -> {
                            Text(
                                text = "✨ Título Principal",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = titleText,
                                onValueChange = onTitleChange,
                                label = { Text("Texto del Título") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Styling filters
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = isBold,
                                    onClick = { onBoldChange(!isBold) },
                                    label = { Text("Negrita") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = isUppercase,
                                    onClick = { onUppercaseChange(!isUppercase) },
                                    label = { Text("MAYÚSCULAS") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Alignment
                            Text("Alineación:", style = MaterialTheme.typography.bodySmall)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = textAlign == Paint.Align.LEFT,
                                    onClick = { onTextAlignChange(Paint.Align.LEFT) },
                                    label = { Text("Izquierda") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = textAlign == Paint.Align.CENTER,
                                    onClick = { onTextAlignChange(Paint.Align.CENTER) },
                                    label = { Text("Centro") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = textAlign == Paint.Align.RIGHT,
                                    onClick = { onTextAlignChange(Paint.Align.RIGHT) },
                                    label = { Text("Derecha") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Size Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tamaño de fuente:", style = MaterialTheme.typography.bodySmall)
                                Text("${textSizeSp.toInt()} sp", fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = textSizeSp,
                                onValueChange = onTextSizeChange,
                                valueRange = 14f..38f
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Offsets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Ajuste Vertical Y (${textOffsetY.toInt()}px):", style = MaterialTheme.typography.bodySmall)
                                if (textOffsetY != 0f) {
                                    TextButton(onClick = { onTextOffsetYChange(0f) }) {
                                        Text("Centrar", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            Slider(
                                value = textOffsetY,
                                onValueChange = onTextOffsetYChange,
                                valueRange = -60f..60f
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Ajuste Horizontal X (${textOffsetX.toInt()}px):", style = MaterialTheme.typography.bodySmall)
                                if (textOffsetX != 0f) {
                                    TextButton(onClick = { onTextOffsetXChange(0f) }) {
                                        Text("Centrar", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            Slider(
                                value = textOffsetX,
                                onValueChange = onTextOffsetXChange,
                                valueRange = -60f..60f
                            )
                        }

                        TextTarget.HEADER_ACCENT -> {
                            Text(
                                text = "📌 Texto Superior / Marca",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Aparece en la parte superior del marco de la plantilla.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = headerAccentText,
                                onValueChange = onHeaderAccentChange,
                                label = { Text("Texto Superior / Marca") },
                                placeholder = { Text("Ej: FRADEL AND SPIES CO / OFICIAL") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val headerPresets = listOf("MI EMPRESA", "LOCAL OFICIAL", "ZONA WIFI", "PAGO SEGURO", "MESA VIP", "PROMO")
                            Text("Sugerencias:", style = MaterialTheme.typography.bodySmall)
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(headerPresets) { p ->
                                    SuggestionChip(
                                        onClick = { onHeaderAccentChange(p) },
                                        label = { Text(p) }
                                    )
                                }
                            }
                        }

                        TextTarget.SUBTEXT -> {
                            Text(
                                text = "📝 Subtítulo / Instrucciones",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Activar subtítulo", style = MaterialTheme.typography.bodyMedium)
                                Switch(checked = showSubtext, onCheckedChange = onShowSubtextChange)
                            }

                            if (showSubtext) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = subtext,
                                    onValueChange = onSubtextChange,
                                    label = { Text("Texto del Subtítulo") },
                                    placeholder = { Text("Ej: Apunta con tu cámara para escanear") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Tamaño del subtítulo:", style = MaterialTheme.typography.bodySmall)
                                    Text("${subtextSizeSp.toInt()} sp", fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = subtextSizeSp,
                                    onValueChange = onSubtextSizeChange,
                                    valueRange = 10f..22f
                                )
                            }
                        }

                        TextTarget.FOOTER_ACCENT -> {
                            Text(
                                text = "🔻 Pie de Página / Logo Inferior",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Aparece en la base o borde inferior de la plantilla.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = footerAccentText,
                                onValueChange = onFooterAccentChange,
                                label = { Text("Texto de Pie de Página") },
                                placeholder = { Text("Ej: WWW.MINEGOCIO.COM / TU LOGO AQUÍ") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val footerPresets = listOf("WWW.MISITIO.COM", "TU LOGO AQUÍ", "@MINEGOCIO", "GRACIAS POR SU VISITA", "TEL: 555-0199")
                            Text("Sugerencias de pie:", style = MaterialTheme.typography.bodySmall)
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(footerPresets) { f ->
                                    SuggestionChip(
                                        onClick = { onFooterAccentChange(f) },
                                        label = { Text(f) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Color Selector
                    Text(
                        text = "Color del texto:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(colorPalette) { (_, colorVal) ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(androidx.compose.ui.graphics.Color(colorVal))
                                    .border(
                                        width = if (selectedTextColor == colorVal) 3.dp else 1.dp,
                                        color = if (selectedTextColor == colorVal) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable { onTextColorChange(colorVal) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetDefaults,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Restablecer", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Aplicar Cambios", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private fun saveTemplateBitmapToGallery(context: Context, bitmap: Bitmap, format: Bitmap.CompressFormat) {
    try {
        val extension = if (format == Bitmap.CompressFormat.JPEG) "jpg" else "png"
        val mimeType = if (format == Bitmap.CompressFormat.JPEG) "image/jpeg" else "image/png"
        val filename = "PLANTILLA_QR_${System.currentTimeMillis()}.$extension"

        var fos: java.io.OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                fos = resolver.openOutputStream(uri)
            }
        } else {
            val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS).toString()
            val fileDir = File(imagesDir)
            if (!fileDir.exists()) fileDir.mkdirs()
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            if (format == Bitmap.CompressFormat.JPEG) {
                val solidBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(solidBitmap)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                solidBitmap.compress(format, 100, it)
                solidBitmap.recycle()
            } else {
                bitmap.compress(format, 100, it)
            }
            Toast.makeText(context, "Plantilla guardada: $filename", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun shareTemplateBitmap(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "shared_templates")
        if (!cachePath.exists()) cachePath.mkdirs()
        val file = File(cachePath, "template_qr.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "com.aistudio.omnicode.zvyf.fileprovider",
            file
        )

        if (contentUri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/png"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir Plantilla QR"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al compartir: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun exportTemplatePdf(
    context: Context,
    bitmap: Bitmap,
    templateName: String,
    titleText: String,
    content: String
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header Title
        paint.color = Color.BLACK
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Hoja de Etiquetas Imprimibles (12 Stickers)", 40f, 40f, paint)

        // Metadata
        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("Estilo: $templateName | Texto: $titleText", 40f, 60f, paint)
        
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, 70f, 555f, 70f, paint)

        // Draw 3 columns x 4 rows = 12 stickers
        val cols = 3
        val rows = 4
        val stickerWidth = 144f
        val stickerHeight = 180f
        val gapX = 25f
        val gapY = 15f
        val marginX = (595f - (cols * stickerWidth + (cols - 1) * gapX)) / 2f
        val marginY = 85f

        val scaled = Bitmap.createScaledBitmap(bitmap, stickerWidth.toInt(), stickerHeight.toInt(), true)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = marginX + c * (stickerWidth + gapX)
                val top = marginY + r * (stickerHeight + gapY)
                
                // Draw shadow or border if desired, but template is usually dark/colored.
                canvas.drawBitmap(scaled, left, top, null)
            }
        }

        // Watermark / Footer
        paint.color = Color.GRAY
        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText("Generado por QR OFICIAL para Android - Formato A4", 40f, 820f, paint)

        pdfDocument.finishPage(page)

        val cachePath = File(context.cacheDir, "shared_templates")
        if (!cachePath.exists()) cachePath.mkdirs()
        val file = File(cachePath, "plantilla_qr.pdf")
        val stream = FileOutputStream(file)
        pdfDocument.writeTo(stream)
        stream.close()
        pdfDocument.close()

        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "com.aistudio.omnicode.zvyf.fileprovider",
            file
        )

        if (contentUri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, "application/pdf")
                putExtra(Intent.EXTRA_STREAM, contentUri)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Exportar PDF de Plantilla"))
            com.example.utils.ReviewManager.askForReview(context)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error en PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun TemplateThumbnail(
    template: TemplateItemDef,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var previewBitmap by remember(template) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(template) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val customization = TemplateCustomization(
                templateDef = template,
                qrContent = "https://google.com",
                titleText = template.defaultText,
                textSizeSp = if (template.id == "escanea_aqui") 22f else 24f,
                textAlign = if (template.id == "escanea_aqui") Paint.Align.LEFT else Paint.Align.CENTER,
                textColor = template.defaultTextColor,
                subtext = template.defaultSubtext,
                subtextSizeSp = 13f,
                subtextColor = if (template.isDarkTheme) Color.LTGRAY else Color.DKGRAY,
                qrForegroundColor = Color.BLACK,
                qrBackgroundColor = Color.WHITE
            )
            val bmp = TemplateRenderer.renderTemplateBitmap(context, customization, 512, 640)
            previewBitmap = bmp
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (template.isDarkTheme) androidx.compose.ui.graphics.Color(0xFF0F172A)
                else androidx.compose.ui.graphics.Color(0xFFF1F5F9)
            ),
        contentAlignment = Alignment.Center
    ) {
        val currentBmp = previewBitmap
        if (currentBmp != null) {
            Image(
                bitmap = currentBmp.asImageBitmap(),
                contentDescription = template.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
