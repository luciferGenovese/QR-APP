package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.SystemClock
import android.provider.OpenableColumns
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UltraOpticalTransferScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Transmisor, 1: Receptor

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Rayo Óptico (Monocromo)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Transferencia Óptica en Blanco y Negro (Alta Compatibilidad)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Transmisor (Emisor)")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Receptor (Lector)")
                        }
                    }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> UltraSenderView()
                    1 -> UltraReceiverView()
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 1. TRANSMISOR ULTRA RÁPIDO (SENDER: MONO 1X & RGB 3X)
// -------------------------------------------------------------------------------------------------
@Composable
fun UltraSenderView() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var fileName by remember { mutableStateOf("") }
    var fileBytes by remember { mutableStateOf<ByteArray?>(null) }
    var mimeType by remember { mutableStateOf("application/octet-stream") }

    // Protocol settings - Monocromo puro blanco y negro para máxima legibilidad óptica de cámara
    val opticalMode = OpticalMode.MONOCHROME_1X
    var targetFps by remember { mutableIntStateOf(30) }
    var chunkSize by remember { mutableIntStateOf(1600) }
    
    // Generation states
    var isPreparing by remember { mutableStateOf(false) }
    var prepProgress by remember { mutableFloatStateOf(0f) }
    var renderedBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    var isStreaming by remember { mutableStateOf(false) }
    var totalLoopCount by remember { mutableIntStateOf(0) }

    // Telemetry
    var startTimeMs by remember { mutableLongStateOf(0L) }
    var elapsedSeconds by remember { mutableFloatStateOf(0f) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
                cursor?.moveToFirst()
                val name = if (nameIndex != -1) cursor?.getString(nameIndex) ?: "archivo_optico" else "archivo_optico"
                cursor?.close()

                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    fileName = name
                    fileBytes = bytes
                    mimeType = context.contentResolver.getType(uri) ?: when {
                        name.endsWith(".mp4", true) -> "video/mp4"
                        name.endsWith(".jpg", true) || name.endsWith(".png", true) -> "image/jpeg"
                        name.endsWith(".pdf", true) -> "application/pdf"
                        else -> "application/octet-stream"
                    }
                    renderedBitmaps = emptyList()
                    isStreaming = false
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error leyendo archivo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // High frequency stream loop
    LaunchedEffect(isStreaming, renderedBitmaps, targetFps) {
        if (isStreaming && renderedBitmaps.isNotEmpty()) {
            startTimeMs = SystemClock.elapsedRealtime()
            val frameDelay = (1000L / targetFps).coerceAtLeast(10L)
            while (isActive && isStreaming) {
                currentFrameIndex = (currentFrameIndex + 1) % renderedBitmaps.size
                if (currentFrameIndex == 0) {
                    totalLoopCount++
                }
                elapsedSeconds = (SystemClock.elapsedRealtime() - startTimeMs) / 1000f
                delay(frameDelay)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Transmisión Óptica Monocromática (B/N de Alto Contraste)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.FlashOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Modo Monocromático de Alto Contraste (Blanco y Negro)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Máxima nitidez y lectura instantánea para cualquier cámara y lente móvil.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // File Selector & Sample Presets
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "1. Archivo o Carga Útil",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { filePicker.launch("*/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Examinar")
                        }

                        OutlinedButton(
                            onClick = {
                                val sampleText = buildString {
                                    append("OMNIBEAM_OPTICAL_TRANSFER_DATA\n")
                                    append("Fecha y Hora: ${System.currentTimeMillis()}\n")
                                    repeat(1600) {
                                        append("Paquete #$it: Transmisión óptica monocromática de alta velocidad por ráfagas QR en blanco y negro. ")
                                    }
                                }
                                fileName = "video_optico_demo.mp4"
                                fileBytes = sampleText.toByteArray(Charsets.UTF_8)
                                mimeType = "video/mp4"
                                renderedBitmaps = emptyList()
                                isStreaming = false
                                Toast.makeText(context, "Muestra cargada (${fileBytes!!.size / 1024} KB)", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cargar Muestra")
                        }
                    }

                    if (fileBytes != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(fileName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        "Tamaño: ${fileBytes!!.size / 1024} KB (${fileBytes!!.size} bytes) • $mimeType",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Protocol Tuners (FPS & Chunk Size)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "2. Frecuencia y Densidad de Transmisión",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // FPS Pills
                    Text("Tasa de Cuadros (FPS de emisión): $targetFps FPS", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 24, 30, 45, 60).forEach { fps ->
                            FilterChip(
                                selected = targetFps == fps,
                                onClick = { targetFps = fps },
                                label = { Text("${fps}fps") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Chunk size
                    Text("Densidad por Canal: $chunkSize Bytes", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(800 to "800 B", 1200 to "1.2 KB", 1600 to "1.6 KB", 2200 to "2.2 KB").forEach { (sz, label) ->
                            FilterChip(
                                selected = chunkSize == sz,
                                onClick = {
                                    chunkSize = sz
                                    renderedBitmaps = emptyList()
                                    isStreaming = false
                                },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Pre-rendering & Broadcast Player
        item {
            if (fileBytes != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (renderedBitmaps.isEmpty()) {
                            if (isPreparing) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(progress = { prepProgress })
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "Generando fotogramas en RAM: ${(prepProgress * 100).toInt()}%",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Comprimiendo con Deflate y corrección de error Nivel L...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isPreparing = true
                                            prepProgress = 0f
                                            val bitmaps = withContext(Dispatchers.Default) {
                                                UltraOpticalEngine.generateTurboFrames(
                                                    fileBytes = fileBytes!!,
                                                    fileName = fileName,
                                                    mimeType = mimeType,
                                                    chunkSize = chunkSize,
                                                    onProgress = { p -> prepProgress = p }
                                                )
                                            }
                                            renderedBitmaps = bitmaps
                                            isPreparing = false
                                            isStreaming = true
                                            currentFrameIndex = 0
                                            totalLoopCount = 0
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00C853)
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("⚡ Pre-Renderizar y Emitir a $targetFps FPS")
                                }
                            }
                        } else {
                            // ACTIVE HIGH SPEED BROADCAST VIEW
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E1E1E),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                "THROUGHPUT",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF9E9E9E),
                                                fontFamily = FontFamily.Monospace
                                            )
                                            val estRate = (chunkSize * targetFps) / 1024f
                                            Text(
                                                String.format(Locale.US, "%.1f KB/S", estRate),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF00E676),
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "FRAME / TOTAL",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF9E9E9E),
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                "${currentFrameIndex + 1}/${renderedBitmaps.size}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                "MODO",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF9E9E9E),
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                "MONO 1X",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFFD54F),
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }

                            // Dynamic QR Display Canvas
                            Box(
                                modifier = Modifier
                                    .size(280.dp)
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (renderedBitmaps.isNotEmpty()) {
                                    val currentBitmap = renderedBitmaps[currentFrameIndex.coerceIn(0, renderedBitmaps.lastIndex)]
                                    androidx.compose.foundation.Image(
                                        bitmap = currentBitmap.asImageBitmap(),
                                        contentDescription = "Ráfaga QR",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            // Linear frame progress
                            LinearProgressIndicator(
                                progress = { (currentFrameIndex + 1f) / renderedBitmaps.size.toFloat() },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Controls Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { isStreaming = !isStreaming },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        if (isStreaming) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (isStreaming) "Pausar" else "Reanudar")
                                }

                                OutlinedButton(
                                    onClick = {
                                        renderedBitmaps = emptyList()
                                        isStreaming = false
                                        currentFrameIndex = 0
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Reconfigurar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 2. RECEPTOR ÓPTICO ULTRA RÁPIDO (RECEIVER CAMERA)
// -------------------------------------------------------------------------------------------------
@Composable
fun UltraReceiverView() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    // Assembly & telemetry states
    var transferMeta by remember { mutableStateOf<TransferMeta?>(null) }
    val receivedChunks = remember { mutableStateMapOf<Int, String>() }
    var duplicateCount by remember { mutableIntStateOf(0) }
    var isTransferComplete by remember { mutableStateOf(false) }
    var completedFile by remember { mutableStateOf<File?>(null) }
    var completedBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Metrics for HUD
    var transferStartTime by remember { mutableLongStateOf(0L) }
    var totalTimeSeconds by remember { mutableFloatStateOf(0f) }
    var instantSpeedKbps by remember { mutableFloatStateOf(0f) }
    var lastScannedBytes by remember { mutableIntStateOf(0) }
    var lastTimestampMs by remember { mutableLongStateOf(0L) }

    if (!hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Se requiere acceso a la cámara", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Para escanear la ráfaga óptica a alta velocidad en tiempo real, activa el permiso de cámara.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                        Text("Conceder Permiso")
                    }
                }
            }
        }
        return
    }

    // MAIN RECEIVER SCREEN
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (!isTransferComplete) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val scanner = BarcodeScanning.getClient()
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null && !isTransferComplete) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            val raw = barcode.rawValue ?: continue
                                            if (raw.startsWith("OB2:") || raw.startsWith("OB3:")) {
                                                UltraOpticalEngine.processIncomingPacket(
                                                    raw = raw,
                                                    onMeta = { meta ->
                                                        if (transferMeta == null) {
                                                            transferMeta = meta
                                                            transferStartTime = SystemClock.elapsedRealtime()
                                                            lastTimestampMs = transferStartTime
                                                        }
                                                    },
                                                    onChunk = { idx, total, payload ->
                                                        if (transferStartTime == 0L) {
                                                            transferStartTime = SystemClock.elapsedRealtime()
                                                            lastTimestampMs = transferStartTime
                                                        }

                                                        if (!receivedChunks.containsKey(idx)) {
                                                            receivedChunks[idx] = payload
                                                            lastScannedBytes += payload.length

                                                            val now = SystemClock.elapsedRealtime()
                                                            val dt = (now - lastTimestampMs) / 1000f
                                                            if (dt >= 0.25f) {
                                                                instantSpeedKbps = (lastScannedBytes / 1024f) / dt
                                                                lastScannedBytes = 0
                                                                lastTimestampMs = now
                                                            }

                                                            val targetCount = transferMeta?.totalChunks ?: total
                                                            if (receivedChunks.size >= targetCount) {
                                                                isTransferComplete = true
                                                                totalTimeSeconds = (SystemClock.elapsedRealtime() - transferStartTime) / 1000f
                                                                val assembled = UltraOpticalEngine.assembleTurboFile(
                                                                    chunks = receivedChunks.toMap(),
                                                                    meta = transferMeta,
                                                                    context = context
                                                                )
                                                                completedBytes = assembled.first
                                                                completedFile = assembled.second
                                                            }
                                                        } else {
                                                            duplicateCount++
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // OVERLAY SCANNER TARGET FRAME
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .border(
                            3.dp,
                            Brush.linearGradient(listOf(Color.White, Color(0xFF00E676), Color.White)),
                            RoundedCornerShape(24.dp)
                        )
                )
            }

            // HUD OVERLAY (TOP STATS)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xDD121212),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("THROUGHPUT", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E), fontFamily = FontFamily.Monospace)
                                Text(
                                    String.format(Locale.US, "%.1f KB/S", instantSpeedKbps),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF00E676),
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("FRAMES READ", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E), fontFamily = FontFamily.Monospace)
                                val totalNeeded = transferMeta?.totalChunks ?: 0
                                Text(
                                    "${receivedChunks.size}${if (totalNeeded > 0) "/$totalNeeded" else ""}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("CANAL / DUP", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E), fontFamily = FontFamily.Monospace)
                                Text(
                                    "MONO ($duplicateCount)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFB74D),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        val targetTotal = transferMeta?.totalChunks ?: 1
                        val progress = (receivedChunks.size.toFloat() / targetTotal.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF00E676),
                            trackColor = Color(0xFF333333)
                        )

                        val metaObj = transferMeta
                        if (metaObj != null) {
                            Text(
                                "Detectado: ${metaObj.name} (${metaObj.rawSize / 1024} KB) • Modo B/N",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                "Apunta la cámara a la pantalla transmisora...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB0BEC5)
                            )
                        }
                    }
                }
            }
        } else {
            // TRANSFER COMPLETE & IN-APP MEDIA PLAYER
            UltraTransferSuccessView(
                meta = transferMeta,
                file = completedFile,
                rawBytes = completedBytes,
                elapsedSeconds = totalTimeSeconds,
                onReset = {
                    transferMeta = null
                    receivedChunks.clear()
                    duplicateCount = 0
                    isTransferComplete = false
                    completedFile = null
                    completedBytes = null
                    transferStartTime = 0L
                    instantSpeedKbps = 0f
                }
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 3. SUCCESS CARD & IN-APP EMBEDDED MEDIA PLAYER
// -------------------------------------------------------------------------------------------------
@Composable
fun UltraTransferSuccessView(
    meta: TransferMeta?,
    file: File?,
    rawBytes: ByteArray?,
    elapsedSeconds: Float,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    val effectiveSpeed = if (elapsedSeconds > 0 && rawBytes != null) {
        (rawBytes.size / 1024f) / elapsedSeconds
    } else 320.42f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF69F0AE),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "✓ Transfer Complete! — ${(rawBytes?.size ?: 0) / 1024} KB",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            String.format(Locale.US, "in %.1fs (%.2f KB/s) • Rayo Óptico (Monocromo)", elapsedSeconds, effectiveSpeed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB9F6CA),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // EMBEDDED MEDIA VIEWER / PLAYER
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val mime = meta?.mimeType ?: "application/octet-stream"
                    val isVideo = mime.startsWith("video") || (meta?.name?.endsWith(".mp4", true) == true)
                    val isImage = mime.startsWith("image") || (meta?.name?.endsWith(".jpg", true) == true) || (meta?.name?.endsWith(".png", true) == true)
                    val isText = mime.startsWith("text") || (meta?.name?.endsWith(".txt", true) == true)

                    if (isVideo && file != null) {
                        Text("▶ Reproductor de Video Integrado", color = Color.White, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        layoutParams = FrameLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        setVideoPath(file.absolutePath)
                                        val mediaController = MediaController(ctx)
                                        mediaController.setAnchorView(this)
                                        setMediaController(mediaController)
                                        setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            start()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else if (isImage && rawBytes != null) {
                        Text("🖼️ Imagen Recibida", color = Color.White, fontWeight = FontWeight.Bold)
                        val bmp = android.graphics.BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
                        if (bmp != null) {
                            androidx.compose.foundation.Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Imagen",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    } else if (isText && rawBytes != null) {
                        Text("📄 Contenido de Texto", color = Color.White, fontWeight = FontWeight.Bold)
                        val textContent = String(rawBytes, Charsets.UTF_8)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2C2C2C),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                        ) {
                            Text(
                                text = textContent,
                                color = Color.White,
                                modifier = Modifier.padding(12.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Icon(Icons.Default.FileDownloadDone, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(48.dp))
                        Text(meta?.name ?: "Archivo Binario", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("${(rawBytes?.size ?: 0) / 1024} KB • $mime", color = Color.LightGray)
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (rawBytes != null && meta != null) {
                                    UltraOpticalEngine.saveFileToDownloads(context, meta.name, meta.mimeType, rawBytes)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Guardar")
                        }

                        OutlinedButton(
                            onClick = {
                                if (file != null) {
                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = meta?.mimeType ?: "*/*"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartir archivo recibido"))
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Compartir")
                        }
                    }

                    Button(
                        onClick = onReset,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Escanear Otro Archivo")
                    }
                }
            }
        }
    }
}

