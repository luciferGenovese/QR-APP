package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UltraOpticalTransferScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Transferir por QR") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Enviar") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Recibir") })
            }
            if (selectedTab == 0) MonoSenderView() else MonoReceiverView()
        }
    }
}

@Composable
private fun MonoSenderView() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sender by remember { mutableStateOf<MonoTransferSender?>(null) }
    var preparing by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var frame by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var frameNumber by remember { mutableIntStateOf(0) }
    var interval by remember { mutableIntStateOf(250) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { sender?.close() }
    }
    DisposableEffect(playing) {
        val window = (context as? android.app.Activity)?.window
        val flag = android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        val alreadyKeptOn = window?.attributes?.flags?.and(flag) != 0
        if (playing) window?.addFlags(flag)
        onDispose {
            if (playing && !alreadyKeptOn) window?.clearFlags(flag)
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && !preparing) {
            scope.launch {
                preparing = true
                error = null
                playing = false
                try {
                    val prepared = withContext(Dispatchers.IO) { MonoTransferSender.prepare(context, uri) }
                    sender?.close()
                    sender = prepared
                    frameNumber = 0
                    frame = null
                } catch (e: Exception) {
                    error = e.localizedMessage ?: "No se pudo preparar el archivo"
                } finally {
                    preparing = false
                }
            }
        }
    }

    LaunchedEffect(sender, playing, interval) {
        val active = sender ?: return@LaunchedEffect
        if (!playing) return@LaunchedEffect
        var index = 0
        var tick = 0
        try {
            while (isActive) {
                val isMetadata = tick % 8 == 0
                val packet = withContext(Dispatchers.IO) {
                    if (isMetadata) active.metadataPacket() else active.dataPacket(index)
                }
                frame = withContext(Dispatchers.Default) { renderMonoTransferQr(packet) }
                if (!isMetadata) {
                    frameNumber = index + 1
                    index = (index + 1) % active.info.totalChunks
                }
                tick++
                delay(interval.toLong())
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            playing = false
            error = e.localizedMessage ?: "No se pudo generar el código"
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!playing) {
            Text("Envía un archivo con códigos QR en blanco y negro.", style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = { picker.launch("*/*") }, enabled = !preparing, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.UploadFile, null)
                Spacer(Modifier.width(8.dp))
                Text("Seleccionar archivo")
            }
        }
        if (preparing) CircularProgressIndicator()
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        val active = sender
        if (active != null) {
            if (!playing) {
                Text(active.info.name, style = MaterialTheme.typography.titleMedium)
                Text("${active.info.size} bytes · ${active.info.totalChunks} fragmentos", style = MaterialTheme.typography.bodyMedium)
            }
            if (frame != null) {
                Image(
                    frame!!.asImageBitmap(), contentDescription = "Código de transferencia",
                    modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp).aspectRatio(1f).background(Color.White)
                )
                Text("Fragmento $frameNumber de ${active.info.totalChunks}", style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = { playing = !playing }, modifier = Modifier.fillMaxWidth()) {
                Text(if (playing) "Pausar" else if (frame == null) "Comenzar transmisión" else "Continuar")
            }
            Text("Velocidad", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(500 to "Lenta", 250 to "Normal", 125 to "Rápida").forEach { (ms, label) ->
                    FilterChip(selected = interval == ms, onClick = { interval = ms }, label = { Text(label) })
                }
            }
            Text(
                "En el otro teléfono, abre Recibir y apunta a esta pantalla. Mantén ambos dispositivos encendidos hasta completar la transferencia.",
                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MonoReceiverView() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val permission = rememberPermissionState(Manifest.permission.CAMERA)
    val scope = rememberCoroutineScope()
    var receiver by remember { mutableStateOf(MonoTransferReceiver(context)) }
    var info by remember { mutableStateOf<MonoTransferInfo?>(null) }
    var count by remember { mutableIntStateOf(0) }
    var completedFile by remember { mutableStateOf<File?>(null) }
    var verifying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(receiver) {
        onDispose { receiver.close() }
    }

    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri: Uri? ->
        val file = completedFile
        if (uri != null && file != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver.openOutputStream(uri) ?: error("No se pudo abrir el destino")
                        file.inputStream().buffered().use { input -> output.use { input.copyTo(it) } }
                    }
                    Toast.makeText(context, "Archivo guardado", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    error = e.localizedMessage ?: "No se pudo guardar el archivo"
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (completedFile != null) {
            Text("Transferencia completada", style = MaterialTheme.typography.headlineSmall)
            Text(info?.name.orEmpty(), style = MaterialTheme.typography.titleMedium)
            Text("Archivo verificado · ${info?.size ?: 0} bytes")
            Button(onClick = { save.launch(info?.name ?: "archivo") }, modifier = Modifier.fillMaxWidth()) { Text("Guardar archivo") }
            OutlinedButton(onClick = {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", completedFile!!)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = info?.mimeType ?: "application/octet-stream"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir archivo"))
            }, modifier = Modifier.fillMaxWidth()) { Text("Compartir") }
        } else if (!permission.status.isGranted) {
            Text("Permite el acceso a la cámara para recibir el archivo.")
            Button(onClick = { permission.launchPermissionRequest() }) { Text("Permitir cámara") }
        } else {
            Text("Apunta la cámara al QR del teléfono emisor.", style = MaterialTheme.typography.bodyLarge)
            TransferCamera(
                modifier = Modifier.fillMaxWidth().weight(1f),
                lifecycle = lifecycle,
                enabled = !verifying && error == null,
                onPacket = { packet ->
                    try {
                        if (receiver.accept(packet)) {
                            info = receiver.info
                            count = receiver.receivedCount
                            val current = receiver.info
                            if (current != null && count == current.totalChunks && !verifying) {
                                verifying = true
                                val activeReceiver = receiver
                                scope.launch {
                                    try {
                                        completedFile = withContext(Dispatchers.IO) { activeReceiver.finish() }
                                    } catch (e: Exception) {
                                        error = e.localizedMessage ?: "No se pudo verificar el archivo"
                                    } finally {
                                        verifying = false
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        error = e.localizedMessage ?: "No se pudo recibir el archivo"
                    }
                }
            )
            val total = info?.totalChunks ?: 0
            LinearProgressIndicator(
                progress = { if (total > 0) count.toFloat() / total else 0f },
                modifier = Modifier.fillMaxWidth()
            )
            Text(if (verifying) "Verificando archivo…" else if (total > 0) "$count de $total fragmentos · ${info?.name}" else "Esperando el archivo…")
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (info != null || error != null) {
            TextButton(enabled = !verifying, onClick = {
                receiver = MonoTransferReceiver(context)
                info = null
                count = 0
                completedFile = null
                error = null
                verifying = false
            }) { Text("Empezar de nuevo") }
        }
    }
}

@Composable
private fun TransferCamera(
    modifier: Modifier,
    lifecycle: androidx.lifecycle.LifecycleOwner,
    enabled: Boolean,
    onPacket: (String) -> Unit
) {
    val context = LocalContext.current
    val latestOnPacket by rememberUpdatedState(onPacket)
    val latestEnabled by rememberUpdatedState(enabled)
    val previewView = remember { PreviewView(context) }
    DisposableEffect(lifecycle, previewView) {
        var disposed = false
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
        var provider: ProcessCameraProvider? = null
        var preview: Preview? = null
        var analysis: ImageAnalysis? = null
        providerFuture.addListener({
            if (!disposed) {
                try {
                    provider = providerFuture.get()
                    preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { useCase ->
                        useCase.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage == null || !latestEnabled || disposed) {
                                imageProxy.close()
                            } else {
                                val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                scanner.process(input)
                                    .addOnSuccessListener { codes ->
                                        if (!disposed && latestEnabled) codes.firstOrNull()?.rawValue?.let(latestOnPacket)
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            }
                        }
                    }
                    provider?.bindToLifecycle(lifecycle, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                } catch (e: Exception) {
                    Toast.makeText(context, "No se pudo iniciar la cámara", Toast.LENGTH_SHORT).show()
                }
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            disposed = true
            analysis?.clearAnalyzer()
            if (provider != null && preview != null && analysis != null) provider?.unbind(preview, analysis)
            scanner.close()
        }
    }
    AndroidView(factory = { previewView }, modifier = modifier)
}
