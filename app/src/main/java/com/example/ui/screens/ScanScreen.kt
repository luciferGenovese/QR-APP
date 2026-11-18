package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.utils.BarcodeAnalyzer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    viewModel: com.example.ui.MainViewModel,
    onBack: () -> Unit,
    onResult: (String, String) -> Unit
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val scanBatchMode by viewModel.scanBatchMode.collectAsState()
    val scanKeepScreenOn by viewModel.scanKeepScreenOn.collectAsState()
    val scanSound by viewModel.scanSound.collectAsState()
    val scanVibration by viewModel.scanVibration.collectAsState()
    val autoCopyClipboard by viewModel.autoCopyClipboard.collectAsState()
    val autoOpenLinks by viewModel.autoOpenLinks.collectAsState()
    
    androidx.compose.runtime.DisposableEffect(scanKeepScreenOn) {
        val activity = context as? android.app.Activity
        if (scanKeepScreenOn) {
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    var scannedValue by remember { mutableStateOf<String?>(null) }
    var scannedFormat by remember { mutableStateOf<String?>(null) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var zoomLevel by remember { mutableFloatStateOf(0f) }

    // Secret QR PIN entry state
    var secretPinInput by remember { mutableStateOf("") }
    var isScanSecretPinVisible by remember { mutableStateOf(false) }
    var decryptedSecretText by remember { mutableStateOf<String?>(null) }
    var decryptError by remember { mutableStateOf<String?>(null) }

    // Batch Scanned items accumulator
    val batchScannedItems = remember { mutableStateListOf<Pair<String, String>>() }
    
    // Animated QR State
    var receivingAnimatedQr by remember { mutableStateOf(false) }
    val receivedChunks = remember { mutableStateMapOf<Int, String>() }
    var totalAnimatedChunks by remember { mutableStateOf(0) }
    var animatedFileName by remember { mutableStateOf("") }

    // Infinite laser vertical translation tracking
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserYOffset by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPosition"
    )

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            try {
                val inputImage = InputImage.fromFilePath(context, selectedUri)
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()
                val scanner = BarcodeScanning.getClient(options)
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            val barcode = barcodes.first()
                            
                            // Successful Decode Vibration Feedback
                            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                            try {
                                vibrator?.let {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        it.vibrate(android.os.VibrationEffect.createOneShot(80, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                    } else {
                                        @Suppress("DEPRECATION")
                                        it.vibrate(80)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            scannedValue = barcode.rawValue
                            scannedFormat = when(barcode.format) {
                                Barcode.FORMAT_QR_CODE -> "QR_CODE"
                                Barcode.FORMAT_CODE_128 -> "CODE_128"
                                Barcode.FORMAT_PDF417 -> "PDF_417"
                                Barcode.FORMAT_AZTEC -> "AZTEC"
                                else -> "CÓDIGO_BARRAS"
                            }
                            Toast.makeText(context, "¡Código leído de la galería!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No se detectó ningún código en la imagen", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al procesar imagen: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al cargar la imagen: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        
                        // Draw semi-transparent HUD overlay
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val cutoutSize = canvasWidth * 0.7f
                        val left = (canvasWidth - cutoutSize) / 2
                        val top = (canvasHeight - cutoutSize) / 3 // Slightly higher than dead center
                        
                        // Mask surroundings
                        drawRect(color = Color.Black.copy(alpha = 0.55f), size = Size(canvasWidth, top))
                        drawRect(
                            color = Color.Black.copy(alpha = 0.55f),
                            topLeft = Offset(0f, top + cutoutSize),
                            size = Size(canvasWidth, canvasHeight - (top + cutoutSize))
                        )
                        drawRect(
                            color = Color.Black.copy(alpha = 0.55f),
                            topLeft = Offset(0f, top),
                            size = Size(left, cutoutSize)
                        )
                        drawRect(
                            color = Color.Black.copy(alpha = 0.55f),
                            topLeft = Offset(left + cutoutSize, top),
                            size = Size(canvasWidth - (left + cutoutSize), cutoutSize)
                        )

                        // Outer glowing overlay corners
                        val cornerLength = 50f
                        val strokeWidth = 8f
                        val color = Color(0xFF86DEB8)
                        
                        // Top-Left corner tag
                        drawLine(color = color, start = Offset(left, top), end = Offset(left + cornerLength, top), strokeWidth = strokeWidth)
                        drawLine(color = color, start = Offset(left, top), end = Offset(left, top + cornerLength), strokeWidth = strokeWidth)
                        
                        // Top-Right corner tag
                        drawLine(color = color, start = Offset(left + cutoutSize, top), end = Offset(left + cutoutSize - cornerLength, top), strokeWidth = strokeWidth)
                        drawLine(color = color, start = Offset(left + cutoutSize, top), end = Offset(left + cutoutSize, top + cornerLength), strokeWidth = strokeWidth)
                        
                        // Bottom-Left corner tag
                        drawLine(color = color, start = Offset(left, top + cutoutSize), end = Offset(left + cornerLength, top + cutoutSize), strokeWidth = strokeWidth)
                        drawLine(color = color, start = Offset(left, top + cutoutSize), end = Offset(left, top + cutoutSize - cornerLength), strokeWidth = strokeWidth)
                        
                        // Bottom-Right corner tag
                        drawLine(color = color, start = Offset(left + cutoutSize, top + cutoutSize), end = Offset(left + cutoutSize - cornerLength, top + cutoutSize), strokeWidth = strokeWidth)
                        drawLine(color = color, start = Offset(left + cutoutSize, top + cutoutSize), end = Offset(left + cutoutSize, top + cutoutSize - cornerLength), strokeWidth = strokeWidth)

                        // Laser line rendering
                        if (scannedValue == null) {
                            val laserY = top + (cutoutSize * laserYOffset)
                            drawLine(
                                color = color.copy(alpha = 0.85f),
                                start = Offset(left + 20f, laserY),
                                end = Offset(left + cutoutSize - 20f, laserY),
                                strokeWidth = 5f
                            )
                        }
                    }
            ) {
                CameraPreview(
                    isPaused = if (scanBatchMode) false else scannedValue != null,
                    isTorchEnabled = isTorchEnabled,
                    isFrontCamera = isFrontCamera,
                    zoomLevel = zoomLevel,
                    onBarcodeScanned = { value, format ->
                        if (value.startsWith("ANIM_QR|")) {
                            // Format: ANIM_QR|totalChunks|chunkIndex|filename|base64_data
                            val parts = value.split("|", limit = 5)
                            if (parts.size == 5) {
                                val total = parts[1].toIntOrNull() ?: 0
                                val idx = parts[2].toIntOrNull() ?: 0
                                val fname = parts[3]
                                val data = parts[4]
                                
                                if (total > 0 && idx in 0 until total) {
                                    if (!receivingAnimatedQr || animatedFileName != fname || totalAnimatedChunks != total) {
                                        receivingAnimatedQr = true
                                        totalAnimatedChunks = total
                                        animatedFileName = fname
                                        receivedChunks.clear()
                                    }
                                    
                                    if (!receivedChunks.containsKey(idx)) {
                                        receivedChunks[idx] = data
                                        // Vibrate slightly for feedback on new chunk
                                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                        try {
                                            vibrator?.let {
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    it.vibrate(android.os.VibrationEffect.createOneShot(25, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    it.vibrate(25)
                                                }
                                            }
                                        } catch (e: Exception) {}
                                    }
                                    
                                    // Check if complete
                                    if (receivedChunks.size == totalAnimatedChunks) {
                                        receivingAnimatedQr = false
                                        val sortedData = receivedChunks.entries.sortedBy { it.key }.joinToString("") { it.value }
                                        scannedValue = "FILE_RECONSTRUCTED|$animatedFileName|$sortedData"
                                        scannedFormat = "ANIM_QR"
                                    }
                                }
                            }

                        } else {
                            // Apply Sound Setting
                            if (scanSound) {
                                try {
                                    val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
                                    toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 150)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            
                            // Apply Vibration Setting
                            if (scanVibration) {
                                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                try {
                                    vibrator?.let {
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            it.vibrate(android.os.VibrationEffect.createOneShot(80, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                        } else {
                                            @Suppress("DEPRECATION")
                                            it.vibrate(80)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            
                            // Apply Auto-copy Clipboard Setting
                            if (autoCopyClipboard) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Scanned Content", value)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            }
                            
                            // Apply Auto-open links Setting
                            if (autoOpenLinks) {
                                if (android.util.Patterns.WEB_URL.matcher(value).matches() || value.startsWith("http://") || value.startsWith("https://")) {
                                    try {
                                        var urlToOpen = value
                                        if (!urlToOpen.startsWith("http://") && !urlToOpen.startsWith("https://")) {
                                            urlToOpen = "http://$urlToOpen"
                                        }
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(urlToOpen))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                            }

                            val mappedFormat = when (format) {
                                "256" -> "QR_CODE"
                                "2" -> "CODE_128"
                                "1024" -> "PDF_417"
                                "64" -> "AZTEC"
                                else -> "CÓDIGO_BARRAS"
                            }

                            if (scanBatchMode) {
                                onResult(value, mappedFormat)
                            } else {
                                scannedValue = value
                                scannedFormat = mappedFormat
                            }
                        }

                    }
                )
                
                if (receivingAnimatedQr && totalAnimatedChunks > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Recibiendo archivo...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Fragmentos: ${receivedChunks.size} / $totalAnimatedChunks",
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            val progress = receivedChunks.size.toFloat() / totalAnimatedChunks.toFloat()
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth(0.6f),
                                color = Color(0xFF4CAF50),
                                trackColor = Color.DarkGray
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(
                        Icons.Default.QrCodeScanner, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(20.dp).size(36.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.scan_permission_title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(
                    stringResource(R.string.scan_permission_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text(stringResource(R.string.btn_grant_permission))
                }
                TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Text(stringResource(R.string.scan_gallery_desc))
                }
            }
        }

        FilledIconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back))
        }

        // Overlay Action Utility Column (Flash Control & Gallery Load)
        if (cameraPermissionState.status.isGranted) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Flashlight toggler
                FloatingActionButton(
                    onClick = { isTorchEnabled = !isTorchEnabled },
                    containerColor = if (isTorchEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    contentColor = if (isTorchEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Linterna"
                    )
                }

                // Gallery picker
                FloatingActionButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Leer de Galería"
                    )
                }

                // Switch Camera
                FloatingActionButton(
                    onClick = {
                        isFrontCamera = !isFrontCamera
                        // When switching camera, typically torch turns off automatically
                        isTorchEnabled = false 
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Cambiar Cámara"
                    )
                }
            }

            // Zoom Controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        if (zoomLevel < 1f) zoomLevel += 0.1f
                    },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Acercar")
                }
                
                FloatingActionButton(
                    onClick = {
                        if (zoomLevel > 0f) zoomLevel -= 0.1f
                    },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Alejar")
                }
            }
        }

        // Floating overlay confirmation panel
        scannedValue?.let { value ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.scan_detected_title, scannedFormat ?: stringResource(R.string.scan_unknown)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val isFile = value.startsWith("FILE_RECONSTRUCTED|")
                    val isSecret = com.example.utils.SecretQrHelper.isSecretQr(value)
                    val isProduct = com.example.utils.ProductLookupHelper.is1DProductBarcode(scannedFormat ?: "")
                    val isWifi = value.startsWith("WIFI:")

                    val displayValue = if (isSecret) {
                        "🔐 Mensaje Cifrado con Seguridad AES-256 (Se requiere PIN para desencriptar)"
                    } else if (isFile) {
                        val parts = value.split("|", limit = 3)
                        "Archivo recibido: ${if (parts.size >= 2) parts[1] else "Desconocido"}"
                    } else {
                        value
                    }

                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isSecret) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            border = null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "🔒 Código Secreto Encriptado (AES-256)",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Introduce el PIN o contraseña con la que fue creado para desbloquear el contenido.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = secretPinInput,
                                    onValueChange = { 
                                        secretPinInput = it
                                        decryptError = null 
                                    },
                                    label = { 
                                        Text(
                                            "PIN o Clave de Desencriptado", 
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ) 
                                    },
                                    placeholder = { 
                                        Text(
                                            "Introduce la clave...", 
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        ) 
                                    },
                                    singleLine = true,
                                    visualTransformation = if (isScanSecretPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isScanSecretPinVisible = !isScanSecretPinVisible }) {
                                            Icon(
                                                imageVector = if (isScanSecretPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (isScanSecretPinVisible) "Ocultar PIN" else "Mostrar PIN",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (decryptError != null) {
                                    Text(
                                        decryptError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val decrypted = com.example.utils.SecretQrHelper.decrypt(value, secretPinInput)
                                        if (decrypted != null && decrypted.isNotEmpty()) {
                                            decryptedSecretText = decrypted
                                            decryptError = null
                                            Toast.makeText(context, "🔓 ¡Mensaje desencriptado con éxito!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            decryptError = "❌ PIN o Contraseña incorrecta. No se pudo desencriptar."
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors()
                                ) {
                                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("🔓 Desencriptar Mensaje", fontWeight = FontWeight.Bold)
                                }

                                decryptedSecretText?.let { decrypted ->
                                    Spacer(Modifier.height(14.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    "Mensaje Desencriptado:",
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                decrypted,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Secreto", decrypted))
                                                        Toast.makeText(context, "📋 Copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.outlinedButtonColors(),
                                                    border = ButtonDefaults.outlinedButtonBorder
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(4.dp))
                                                    Text("Copiar")
                                                }
                                                val isDecryptedUrl = decrypted.startsWith("http://") || decrypted.startsWith("https://")
                                                if (isDecryptedUrl) {
                                                    Button(
                                                        onClick = {
                                                            try {
                                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(decrypted))
                                                                context.startActivity(intent)
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.buttonColors()
                                                    ) {
                                                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Abrir")
                                                    }
                                                } else {
                                                    Button(
                                                        onClick = {
                                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                                putExtra(Intent.EXTRA_TEXT, decrypted)
                                                                type = "text/plain"
                                                            }
                                                            context.startActivity(Intent.createChooser(shareIntent, "Compartir Secreto"))
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.buttonColors()
                                                    ) {
                                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Compartir")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isProduct) {
                        val searchEngine by viewModel.searchEngine.collectAsState()
                        val barcodeDigits = value.filter { it.isDigit() }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val url = com.example.utils.ProductLookupHelper.getSearchUrl(barcodeDigits, searchEngine)
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors()
                            ) {
                                Text("🔍 Buscar Producto")
                            }
                            OutlinedButton(
                                onClick = {
                                    val url = com.example.utils.ProductLookupHelper.getSearchUrl(barcodeDigits, "OpenFoodFacts")
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(),
                                border = ButtonDefaults.outlinedButtonBorder
                            ) {
                                Text("🥦 Nutrición")
                            }
                        }
                    }

                    if (isWifi) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val parts = value.removePrefix("WIFI:").split(";")
                        var ssid = ""
                        var password = ""
                        parts.forEach { part ->
                            if (part.startsWith("S:")) ssid = part.removePrefix("S:")
                            if (part.startsWith("P:")) password = part.removePrefix("P:")
                        }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            border = null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "📶 Red Wi-Fi: $ssid",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                if (password.isNotBlank()) {
                                    Text(
                                        "🔑 Contraseña: $password",
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("WiFi Password", password))
                                            android.widget.Toast.makeText(context, "Contraseña Wi-Fi copiada", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors()
                                    ) {
                                        Text("Copiar Contraseña Wi-Fi")
                                    }
                                }
                            }
                        }
                    }
                    
                    if (isFile) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                try {
                                    val parts = value.split("|", limit = 3)
                                    if (parts.size >= 3) {
                                        val fName = parts[1]
                                        val b64Data = parts[2]
                                        val bytes = android.util.Base64.decode(b64Data, android.util.Base64.NO_WRAP)
                                        
                                        // Save to Downloads
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                            val resolver = context.contentResolver
                                            val contentValues = android.content.ContentValues().apply {
                                                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fName)
                                                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                                            }
                                            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                                            uri?.let {
                                                resolver.openOutputStream(it)?.use { os ->
                                                    os.write(bytes)
                                                }
                                                android.widget.Toast.makeText(context, "Guardado en Descargas: $fName", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                                            val file = java.io.File(downloadsDir, fName)
                                            java.io.FileOutputStream(file).use { os ->
                                                os.write(bytes)
                                            }
                                            android.widget.Toast.makeText(context, "Guardado en Descargas: $fName", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    android.widget.Toast.makeText(context, "Error guardando archivo", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            Text("Guardar Archivo")
                        }
                    } else if (android.util.Patterns.WEB_URL.matcher(value).matches() || value.startsWith("http://") || value.startsWith("https://")) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                try {
                                    var urlToOpen = value
                                    if (!urlToOpen.startsWith("http://") && !urlToOpen.startsWith("https://")) {
                                        urlToOpen = "http://$urlToOpen"
                                    }
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(urlToOpen))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Ignore if unable to open
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text(stringResource(R.string.btn_open))
                        }
                    }

                    if (!isFile) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Scanned Content", value)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, context.getString(R.string.toast_copied), android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_copy))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                scannedValue = null
                                scannedFormat = null
                            },
                            colors = ButtonDefaults.textButtonColors()
                        ) {
                            Text(stringResource(R.string.btn_re_scan))
                        }
                        
                        if (!isFile) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    onResult(value, scannedFormat ?: context.getString(R.string.scan_unknown))
                                    scannedValue = null
                                    scannedFormat = null
                                },
                                colors = ButtonDefaults.buttonColors()
                            ) {
                                Text(stringResource(R.string.btn_confirm_save))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    isPaused: Boolean,
    isTorchEnabled: Boolean,
    isFrontCamera: Boolean,
    zoomLevel: Float,
    onBarcodeScanned: (String, String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var lastScannedValue by remember { mutableStateOf<String?>(null) }
    var cameraReference by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // Single thread executor designated for image analysis
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(isPaused) {
        if (!isPaused) {
            lastScannedValue = null
        }
    }

    LaunchedEffect(isTorchEnabled, cameraReference) {
        try {
            cameraReference?.cameraControl?.enableTorch(isTorchEnabled)
        } catch (e: Exception) {
            Log.e("CameraPreview", "Error toggling torch", e)
        }
    }

    LaunchedEffect(zoomLevel, cameraReference) {
        try {
            // zoomLevel is 0f to 1f
            cameraReference?.cameraControl?.setLinearZoom(zoomLevel.coerceIn(0f, 1f))
        } catch (e: Exception) {
            Log.e("CameraPreview", "Error setting zoom", e)
        }
    }

    // Rebind camera when camera switch changes
    LaunchedEffect(isFrontCamera, cameraProviderRef, previewViewRef) {
        val cameraProvider = cameraProviderRef ?: return@LaunchedEffect
        val previewView = previewViewRef ?: return@LaunchedEffect

        try {
            cameraProvider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        cameraExecutor,
                        BarcodeAnalyzer { barcode ->
                            if (!isPaused) {
                                barcode.rawValue?.let { value ->
                                    if (value.startsWith("ANIM_QR|")) {
                                        onBarcodeScanned(value, barcode.format.toString())
                                    } else if (value != lastScannedValue) {
                                        lastScannedValue = value
                                        onBarcodeScanned(value, barcode.format.toString())
                                    }
                                }
                            }
                        }
                    )
                }

            val cameraSelector = if (isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
            cameraReference = camera
            camera.cameraControl.enableTorch(isTorchEnabled)
            camera.cameraControl.setLinearZoom(zoomLevel)
        } catch (e: Exception) {
            Log.e("CameraPreview", "Use case binding failed on switch", e)
        }
    }


    // Explicitly unbind all use cases upon composable disposal
    DisposableEffect(cameraProviderRef) {
        onDispose {
            try {
                cameraProviderRef?.unbindAll()
            } catch (e: Exception) {
                Log.e("CameraPreview", "Error washing out camera on dispose", e)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            previewViewRef = previewView

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProviderRef = cameraProvider
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
