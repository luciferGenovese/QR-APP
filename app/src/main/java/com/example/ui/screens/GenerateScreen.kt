package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Environment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.ThumbUp

import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.GridView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.core.content.ContextCompat
import android.graphics.Canvas
import android.graphics.RectF
import com.example.utils.CodeGenerator
import com.example.utils.TemplateRenderer
import com.example.utils.TemplateCustomization
import com.example.utils.TemplateItemDef
import com.example.utils.SecretQrHelper
import com.example.utils.BodyShape
import com.example.utils.LogoBadgeShape
import com.example.utils.ScanQualityLevel
import com.example.utils.ScanQualityResult
import com.example.utils.PdfAndPrintHelper
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Speed
import com.google.zxing.BarcodeFormat
import java.io.File
import java.io.FileOutputStream

data class QrTemplate(
    val name: String,
    val foreground: Int,
    val background: Int,
    val shape: com.example.utils.BodyShape
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class, ExperimentalLayoutApi::class)
@Composable
fun GenerateScreen(
    type: String,
    templateResId: Int? = null,
    editingCode: com.example.data.CodeEntity? = null,
    onNavigateToUltraTransfer: (() -> Unit)? = null,
    onSave: (String, String, android.graphics.Bitmap?) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val billingManager = remember(context) { com.example.billing.BillingManager(context) }
    androidx.compose.runtime.DisposableEffect(billingManager) {
        onDispose { billingManager.terminate() }
    }
    val isPro by billingManager.isPro.collectAsState()
    var showPaywallDialog by remember { mutableStateOf(false) }
    
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
    
    // Parse pre-filled values if editingCode is present
    val parsedInitialValues = remember(editingCode, type) {
        var initialContent = ""
        var initialP1 = ""
        var initialP2 = ""
        var initialP3 = ""
        var initialSecretMsg = ""
        var initialVcardOrg = ""
        var initialVcardUrl = ""

        if (editingCode != null) {
            val raw = editingCode.content
            when (type) {
                "QR_URL" -> initialP1 = raw
                "QR_PHONE" -> initialP1 = raw.removePrefix("tel:")
                "QR_WHATSAPP" -> initialP1 = raw.removePrefix("https://wa.me/")
                "QR_EMAIL" -> {
                    val s = raw.removePrefix("mailto:")
                    if (s.contains("?subject=")) {
                        val parts = s.split("?subject=", limit = 2)
                        initialP1 = parts[0]
                        initialP2 = parts.getOrNull(1) ?: ""
                    } else {
                        initialP1 = s
                    }
                }
                "QR_SMS" -> {
                    val s = raw.removePrefix("sms:")
                    if (s.contains("?body=")) {
                        val parts = s.split("?body=", limit = 2)
                        initialP1 = parts[0]
                        initialP2 = parts.getOrNull(1) ?: ""
                    } else {
                        initialP1 = s
                    }
                }
                "QR_WIFI", "QR_WIFI_PRO" -> {
                    val s = raw.removePrefix("WIFI:")
                    initialP1 = Regex("S:([^;]*)").find(s)?.groupValues?.getOrNull(1) ?: ""
                    initialP2 = Regex("P:([^;]*)").find(s)?.groupValues?.getOrNull(1) ?: ""
                    initialP3 = Regex("T:([^;]*)").find(s)?.groupValues?.getOrNull(1) ?: "WPA"
                }
                "QR_CONTACT" -> {
                    initialP1 = Regex("N:([^\n]*)").find(raw)?.groupValues?.getOrNull(1) ?: ""
                    initialP2 = Regex("TEL:([^\n]*)").find(raw)?.groupValues?.getOrNull(1) ?: ""
                    initialP3 = Regex("EMAIL:([^\n]*)").find(raw)?.groupValues?.getOrNull(1) ?: ""
                }
                "QR_VCARD_PRO" -> {
                    initialP1 = Regex("FN:([^\n]*)").find(raw)?.groupValues?.getOrNull(1)
                        ?: Regex("N:([^\n]*)").find(raw)?.groupValues?.getOrNull(1) ?: ""
                    initialP2 = Regex("TEL[^:]*:([^\n]*)").find(raw)?.groupValues?.getOrNull(1) ?: ""
                    initialP3 = Regex("EMAIL[^:]*:([^\n]*)").find(raw)?.groupValues?.getOrNull(1) ?: ""
                    initialVcardOrg = Regex("ORG[^:]*:([^\n]*)").find(raw)?.groupValues?.getOrNull(1) ?: ""
                    initialVcardUrl = Regex("URL[^:]*:([^\n]*)").find(raw)?.groupValues?.getOrNull(1) ?: ""
                }
                "QR_STICKERS" -> {
                    initialP1 = "QR Stickers"
                    initialP2 = raw
                    initialContent = raw
                }
                "QR_WATERMARK" -> {
                    initialContent = raw
                    initialP1 = "OFICIAL"
                }
                "QR_SECRET" -> {
                    initialSecretMsg = raw
                    initialContent = raw
                }
                else -> {
                    initialContent = raw
                }
            }
        }
        arrayOf(initialContent, initialP1, initialP2, initialP3, initialSecretMsg, initialVcardOrg, initialVcardUrl)
    }

    var content by remember(parsedInitialValues) { mutableStateOf(parsedInitialValues[0]) }
    
    val initialBitmap = remember(editingCode) {
        if (editingCode != null) {
            val file = java.io.File(context.filesDir, "qr_${editingCode.id}.png")
            if (file.exists()) {
                android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } else null
    }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(initialBitmap) }
    
    val storagePermissionState = rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    var pendingBitmapFormat by remember { mutableStateOf<Bitmap.CompressFormat?>(null) }
    
    LaunchedEffect(storagePermissionState.status) {
        if (storagePermissionState.status.isGranted && pendingBitmapFormat != null && generatedBitmap != null) {
            saveBitmapToGallery(context, generatedBitmap!!, pendingBitmapFormat!!)
            pendingBitmapFormat = null
        }
    }

    var selectedForegroundColor by remember { mutableIntStateOf(android.graphics.Color.BLACK) }
    var selectedBackgroundColor by remember { mutableIntStateOf(if (templateResId != null) android.graphics.Color.TRANSPARENT else android.graphics.Color.WHITE) }
    var showFullColorPickerDialog by remember { mutableStateOf(false) }
    var isPickingForeground by remember { mutableStateOf(true) }
    
    // Form fields based on selected type
    var p1 by remember(parsedInitialValues) { mutableStateOf(parsedInitialValues[1]) }
    var p2 by remember(parsedInitialValues) { mutableStateOf(parsedInitialValues[2]) }
    var p3 by remember(parsedInitialValues) { mutableStateOf(parsedInitialValues[3]) }

    // Secret QR State
    var secretMessage by remember(parsedInitialValues) { mutableStateOf(parsedInitialValues[4]) }
    var secretPin by remember { mutableStateOf("") }
    var isSecretPinVisible by remember { mutableStateOf(false) }

    // vCard Pro State
    var vcardOrg by remember(parsedInitialValues) { mutableStateOf(parsedInitialValues[5]) }
    var vcardUrl by remember(parsedInitialValues) { mutableStateOf(parsedInitialValues[6]) }

    // Post-generation Success Modal Dialog State
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var savedBitmapForDialog by remember { mutableStateOf<Bitmap?>(null) }

    // Validation errors
    var p1Error by remember { mutableStateOf<String?>(null) }
    var p2Error by remember { mutableStateOf<String?>(null) }
    var p3Error by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    var selectedBodyShape by remember { mutableStateOf(com.example.utils.BodyShape.SQUARE) }
    
    val bodyShapes = remember(context) {
        listOf(
            Pair(context.getString(R.string.shape_square), com.example.utils.BodyShape.SQUARE),
            Pair(context.getString(R.string.shape_circle), com.example.utils.BodyShape.CIRCLE),
            Pair(context.getString(R.string.shape_rounded), com.example.utils.BodyShape.ROUNDED),
            Pair(context.getString(R.string.shape_diamond), com.example.utils.BodyShape.DIAMOND),
            Pair(context.getString(R.string.shape_cross), com.example.utils.BodyShape.CROSS),
            Pair(context.getString(R.string.shape_star), com.example.utils.BodyShape.STAR),
            Pair(context.getString(R.string.shape_dot), com.example.utils.BodyShape.DOT),
            Pair(context.getString(R.string.shape_hexagon), com.example.utils.BodyShape.HEXAGON),
            Pair(context.getString(R.string.shape_heart), com.example.utils.BodyShape.HEART),
            Pair(context.getString(R.string.shape_triangle), com.example.utils.BodyShape.TRIANGLE),
            Pair(context.getString(R.string.shape_pentagon), com.example.utils.BodyShape.PENTAGON),
            Pair(context.getString(R.string.shape_octagon), com.example.utils.BodyShape.OCTAGON),
            Pair("Emoji (PRO)", com.example.utils.BodyShape.EMOJI)
        )
    }


    var selectedLogoUri by remember { mutableStateOf<Uri?>(null) }
    var customEmojiText by remember { mutableStateOf("😃") }
    var selectedLogoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedLogoPresetId by remember { mutableStateOf<String?>(null) }
    var removeLogo by remember { mutableStateOf(false) }
    var selectedLogoSizeRatio by remember { mutableFloatStateOf(0.22f) }
    var selectedLogoBadgeShape by remember { mutableStateOf(LogoBadgeShape.ROUNDED_SQUARE) }
    var selectedErrorCorrectionLevel by remember { mutableStateOf(ErrorCorrectionLevel.H) }
    var showExportResolutionModal by remember { mutableStateOf(false) }
    var showQualityInfoDialog by remember { mutableStateOf(false) }
    var logoTextBadgeInput by remember { mutableStateOf("") }
    var logoTextBadgeColor by remember { mutableIntStateOf(android.graphics.Color.parseColor("#0D47A1")) }

    // Animated QR Data
    var transferMode by remember { mutableStateOf("SEND") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var fileChunks by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentChunkIndex by remember { mutableStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }
    var animDelayMs by remember { mutableLongStateOf(280L) }

    // Receiving Animated QR Data
    val cameraPermissionState = com.google.accompanist.permissions.rememberPermissionState(android.Manifest.permission.CAMERA)
    var receivingAnimatedQr by remember { mutableStateOf(false) }
    val receivedChunks = remember { mutableStateMapOf<Int, String>() }
    var totalAnimatedChunks by remember { mutableStateOf(0) }
    var animatedFileName by remember { mutableStateOf("") }

    // Coroutine for animation
    LaunchedEffect(isAnimating, fileChunks, animDelayMs) {
        if (isAnimating && fileChunks.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(animDelayMs)
                currentChunkIndex = (currentChunkIndex + 1) % fileChunks.size
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedFileUri = uri
        if (uri != null) {
            try {
                // Get filename
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                var fName = "file"
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            fName = it.getString(nameIndex)
                        }
                    }
                }
                val safeFileName = fName.replace("|", "_")
                selectedFileName = safeFileName
                
                // Read chunks
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val bytes = inputStream.readBytes()
                    inputStream.close()
                    
                    val encoded = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    // Split into readable chunks of 220 chars for optimal optical scanning
                    val chunkSize = 220
                    val chunks = encoded.chunked(chunkSize)
                    
                    val formattedChunks = chunks.mapIndexed { index, chunk ->
                        "ANIM_QR|${chunks.size}|$index|$safeFileName|$chunk"
                    }
                    fileChunks = formattedChunks
                    currentChunkIndex = 0
                    isAnimating = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val logoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedLogoUri = uri
        if (uri != null) {
            removeLogo = false
            try {
                val boundsOptions = android.graphics.BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream, null, boundsOptions)
                }

                val maxDim = 512
                var inSampleSize = 1
                if (boundsOptions.outWidth > maxDim || boundsOptions.outHeight > maxDim) {
                    val halfHeight = boundsOptions.outHeight / 2
                    val halfWidth = boundsOptions.outWidth / 2
                    while ((halfHeight / inSampleSize) >= maxDim && (halfWidth / inSampleSize) >= maxDim) {
                        inSampleSize *= 2
                    }
                }

                val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                    inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                }
                val loadedBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream, null, decodeOptions)
                }

                if (loadedBitmap != null) {
                    selectedLogoBitmap = loadedBitmap
                    selectedLogoPresetId = "CUSTOM_UPLOAD"
                    removeLogo = false
                    Toast.makeText(context, "¡Logotipo cargado con éxito!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al cargar la imagen seleccionada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            logoPickerLauncher.launch("image/*")
        } else {
            Toast.makeText(context, context.getString(R.string.toast_gallery_denied), Toast.LENGTH_SHORT).show()
        }
    }

    var livePreviewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cachedTemplateBitmap by remember(templateResId) { mutableStateOf<Bitmap?>(null) }

    var selectedTemplateDef by remember { mutableStateOf<TemplateItemDef?>(null) }
    var customTitleText by remember { mutableStateOf("") }
    var customSubText by remember { mutableStateOf("") }

    
    LaunchedEffect(templateResId) {
        if (templateResId != null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val options = android.graphics.BitmapFactory.Options()
                options.inJustDecodeBounds = true
                android.graphics.BitmapFactory.decodeResource(context.resources, templateResId, options)
                var scale = 1
                val maxDim = 1080
                if (options.outWidth > maxDim || options.outHeight > maxDim) {
                    scale = Math.max(options.outWidth / maxDim, options.outHeight / maxDim)
                }
                options.inJustDecodeBounds = false
                options.inSampleSize = scale
                val templateBitmap = android.graphics.BitmapFactory.decodeResource(context.resources, templateResId, options)
                if (templateBitmap != null) {
                    val maxDimF = 1080f
                    val scaleF = if (templateBitmap.width > maxDimF) maxDimF / templateBitmap.width else 1f
                    val finalTemplate = if (scaleF < 1f) {
                        android.graphics.Bitmap.createScaledBitmap(templateBitmap, (templateBitmap.width * scaleF).toInt(), (templateBitmap.height * scaleF).toInt(), true)
                    } else {
                        templateBitmap
                    }
                    cachedTemplateBitmap = finalTemplate
                } else {
                    cachedTemplateBitmap = null
                }
            }
        } else {
            cachedTemplateBitmap = null
        }
    }

    LaunchedEffect(content, secretMessage, secretPin, selectedForegroundColor, selectedBackgroundColor, selectedBodyShape, selectedLogoBitmap, type, removeLogo, cachedTemplateBitmap, currentChunkIndex, fileChunks, selectedTemplateDef, customTitleText, customSubText, selectedErrorCorrectionLevel, selectedLogoSizeRatio, selectedLogoBadgeShape, customEmojiText) {
        val currentContent = if (type == "QR_SECRET") {
            if (secretMessage.isNotBlank() && secretPin.isNotBlank()) {
                SecretQrHelper.encrypt(secretMessage, secretPin)
            } else if (secretMessage.isNotBlank()) {
                SecretQrHelper.encrypt(secretMessage, "preview_key")
            } else {
                ""
            }
        } else if (type == "QR_ANIMATED" && fileChunks.isNotEmpty()) {
            fileChunks[currentChunkIndex]
        } else {
            content
        }
        
        if (currentContent.isNotBlank()) {
            val format = when (type) {
                "EAN_8" -> BarcodeFormat.EAN_8
                "EAN_13" -> BarcodeFormat.EAN_13
                "UPC_E" -> BarcodeFormat.UPC_E
                "UPC_A" -> BarcodeFormat.UPC_A
                "CODE_39" -> BarcodeFormat.CODE_39
                "CODE_93" -> BarcodeFormat.CODE_93
                "CODE_128", "BARCODE_128" -> BarcodeFormat.CODE_128
                "ITF" -> BarcodeFormat.ITF
                "PDF_417" -> BarcodeFormat.PDF_417
                "CODABAR" -> BarcodeFormat.CODABAR
                "DATA_MATRIX" -> BarcodeFormat.DATA_MATRIX
                "AZTEC" -> BarcodeFormat.AZTEC
                else -> BarcodeFormat.QR_CODE
            }
            
            val is1DBarcode = type in listOf("EAN_8", "EAN_13", "UPC_E", "UPC_A", "CODE_39", "CODE_93", "CODE_128", "BARCODE_128", "ITF", "CODABAR", "PDF_417")
            val bw = if (is1DBarcode) 600 else 512
            val bh = if (is1DBarcode) 300 else 512
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                val rawBitmap = CodeGenerator.generateBitmap(
                    content = currentContent,
                    format = format,
                    width = bw,
                    height = bh,
                    foregroundColor = selectedForegroundColor,
                    backgroundColor = selectedBackgroundColor,
                    bodyShape = selectedBodyShape,
                    logoBitmap = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type)),
                    errorCorrectionLevel = selectedErrorCorrectionLevel,
                    logoSizeRatio = selectedLogoSizeRatio,
                    logoBadgeShape = selectedLogoBadgeShape,
                    customEmoji = customEmojiText
                )
                val preview = if (selectedTemplateDef != null && rawBitmap != null) {
                    try {
                        val custom = TemplateCustomization(
                            templateDef = selectedTemplateDef!!,
                            qrContent = currentContent,
                            titleText = customTitleText.ifBlank { selectedTemplateDef!!.defaultText },
                            subtext = customSubText.ifBlank { selectedTemplateDef!!.defaultSubtext },
                            qrForegroundColor = selectedForegroundColor,
                            qrBackgroundColor = selectedBackgroundColor,
                            logoBitmap = if (type == "Wifi" || type == "Email" || type == "SMS" || type == "VCard" || type == "Text" || type == "Event") null else (selectedLogoBitmap ?: getSocialLogo(context, type))
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
                }
            }
        } else {
            livePreviewBitmap = null
        }
    }

    val scrollState = rememberScrollState()

    // Title mapping
    val typeTitle = remember(type, context) {
        when (type) {
            "QR_SECRET" -> context.getString(R.string.type_secret)
            "QR_VCARD_PRO" -> context.getString(R.string.type_vcard)
            "QR_WIFI_PRO" -> context.getString(R.string.qr_type_wifi_pro)
            "QR_STICKERS" -> context.getString(R.string.type_stickers)
            "QR_WATERMARK" -> context.getString(R.string.type_watermark)
            "QR_WIFI" -> context.getString(R.string.title_qr_wifi)
            "QR_PHONE" -> context.getString(R.string.title_qr_phone)
            "QR_URL" -> context.getString(R.string.title_qr_url)
            "QR_EMAIL" -> context.getString(R.string.title_qr_email)
            "QR_SMS" -> context.getString(R.string.title_qr_sms)
            "QR_CONTACT" -> context.getString(R.string.title_qr_contact)
            "QR_TEXT" -> context.getString(R.string.title_qr_text)
            "QR_APP" -> context.getString(R.string.title_qr_app)
            "QR_ANIMATED" -> "Transferir Archivo"
            "QR_BARCODE" -> context.getString(R.string.title_qr_barcode)
            "QR_RESERVATION" -> context.getString(R.string.title_qr_reservation)
            "QR_TWITTER" -> context.getString(R.string.title_qr_twitter)
            "QR_FACEBOOK" -> context.getString(R.string.title_qr_facebook)
            "QR_MESSENGER" -> context.getString(R.string.title_qr_messenger)
            "QR_YOUTUBE" -> context.getString(R.string.title_qr_youtube)
            "QR_INSTAGRAM" -> context.getString(R.string.title_qr_instagram)
            "QR_LINKEDIN" -> context.getString(R.string.title_qr_linkedin)
            "QR_TIKTOK" -> context.getString(R.string.title_qr_tiktok)
            "QR_TELEGRAM" -> context.getString(R.string.title_qr_telegram)
            "QR_WHATSAPP" -> context.getString(R.string.title_qr_whatsapp)
            "QR_GEO" -> context.getString(R.string.title_qr_location)
            "QR_CALENDAR" -> context.getString(R.string.option_calendar)
            "BARCODE_128", "CODE_128" -> context.getString(R.string.title_barcode_128)
            "PDF_417" -> context.getString(R.string.title_pdf_417)
            "AZTEC" -> context.getString(R.string.title_aztec)
            "EAN_8" -> "EAN-8"
            "EAN_13" -> "EAN-13"
            "UPC_E" -> "UPC-E"
            "UPC_A" -> "UPC-A"
            "CODE_39" -> "CODE 39"
            "CODE_93" -> "CODE 93"
            "ITF" -> "ITF"
            "CODABAR" -> "Codabar"
            "DATA_MATRIX" -> "Data Matrix"
            else -> context.getString(R.string.title_custom_code)
        }
    }

    val performSave: () -> Unit = {
        var isValid = true
        p1Error = null
        p2Error = null
        p3Error = null
        generalError = null

        when (type) {
            "QR_SECRET" -> {
                if (secretMessage.isBlank()) {
                    p1Error = "Ingresa el mensaje confidencial o secreto"
                    isValid = false
                }
                if (secretPin.isBlank()) {
                    p2Error = context.getString(R.string.err_pin_empty)
                    isValid = false
                }
            }
            "QR_VCARD_PRO" -> {
                if (p1.isBlank()) {
                    p1Error = "Ingresa el nombre completo"
                    isValid = false
                }
                if (p2.isBlank()) {
                    p2Error = context.getString(R.string.err_phone_empty)
                    isValid = false
                }
            }
            "QR_STICKERS" -> {
                if (p2.isBlank()) {
                    p2Error = "Ingresa el contenido o enlace de las etiquetas"
                    isValid = false
                }
            }
            "QR_WATERMARK" -> {
                if (content.isBlank()) {
                    generalError = context.getString(R.string.err_content_empty)
                    isValid = false
                }
            }
            "QR_WIFI", "QR_WIFI_PRO" -> {
                if (p1.isBlank()) {
                    p1Error = context.getString(R.string.err_ssid_empty)
                    isValid = false
                }
                if (p3.lowercase() != "nopass" && p2.length < 8) {
                    p2Error = context.getString(R.string.err_paspward_length)
                    isValid = false
                }
            }
            "QR_PHONE", "QR_WHATSAPP" -> {
                if (p1.isBlank()) {
                    p1Error = context.getString(R.string.err_invalid_phone)
                    isValid = false
                } else if (!p1.matches("^[+]?[0-9\\s]{5,15}$".toRegex())) {
                    p1Error = context.getString(R.string.err_invalid_phone)
                    isValid = false
                }
            }
            "QR_URL" -> {
                if (p1.isBlank()) {
                    p1Error = context.getString(R.string.err_invalid_url)
                    isValid = false
                } else {
                    val targetUrl = if (p1.startsWith("http")) p1 else "https://$p1"
                    if (!android.util.Patterns.WEB_URL.matcher(targetUrl).matches()) {
                        p1Error = context.getString(R.string.err_invalid_url)
                        isValid = false
                    }
                }
            }
            "QR_EMAIL" -> {
                if (p1.isBlank()) {
                    p1Error = context.getString(R.string.err_invalid_email)
                    isValid = false
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(p1).matches()) {
                    p1Error = context.getString(R.string.err_invalid_email)
                    isValid = false
                }
            }
            "QR_SMS" -> {
                if (p1.isBlank()) {
                    p1Error = context.getString(R.string.err_invalid_phone)
                    isValid = false
                }
            }
            "QR_CONTACT" -> {
                if (p1.isBlank()) {
                    p1Error = context.getString(R.string.err_contact_empty)
                    isValid = false
                }
                if (p3.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(p3).matches()) {
                    p3Error = context.getString(R.string.err_invalid_email)
                    isValid = false
                }
            }
            else -> {
                val reqLength = when(type) {
                    "EAN_8", "UPC_E" -> 8
                    "UPC_A" -> 12
                    "EAN_13" -> 13
                    else -> -1
                }
                if (content.isBlank()) {
                    generalError = context.getString(R.string.err_text_empty)
                    isValid = false
                } else if (reqLength != -1 && content.length != reqLength) {
                    generalError = context.getString(R.string.err_exact_length, reqLength)
                    isValid = false
                } else if (type == "ITF" && content.length % 2 != 0) {
                    generalError = context.getString(R.string.err_even_length)
                    isValid = false
                }
            }
        }

        if (isValid) {
            val format = when (type) {
                "EAN_8" -> BarcodeFormat.EAN_8
                "EAN_13" -> BarcodeFormat.EAN_13
                "UPC_E" -> BarcodeFormat.UPC_E
                "UPC_A" -> BarcodeFormat.UPC_A
                "CODE_39" -> BarcodeFormat.CODE_39
                "CODE_93" -> BarcodeFormat.CODE_93
                "CODE_128", "BARCODE_128" -> BarcodeFormat.CODE_128
                "ITF" -> BarcodeFormat.ITF
                "PDF_417" -> BarcodeFormat.PDF_417
                "CODABAR" -> BarcodeFormat.CODABAR
                "DATA_MATRIX" -> BarcodeFormat.DATA_MATRIX
                "AZTEC" -> BarcodeFormat.AZTEC
                else -> BarcodeFormat.QR_CODE
            }
            val is1DBarcode = type in listOf("EAN_8", "EAN_13", "UPC_E", "UPC_A", "CODE_39", "CODE_93", "CODE_128", "BARCODE_128", "ITF", "CODABAR", "PDF_417")
            val bw = if (is1DBarcode) 800 else 512
            val bh = if (is1DBarcode) 400 else 512
            
            try {
                val contentToEncode = when (type) {
                    "QR_SECRET" -> if (secretMessage.isNotBlank() && secretPin.isNotBlank()) SecretQrHelper.encrypt(secretMessage, secretPin) else content
                    else -> content
                }
                
                val rawBitmap = CodeGenerator.generateBitmapOrThrow(
                    content = contentToEncode,
                    format = format,
                    width = bw,
                    height = bh,
                    foregroundColor = selectedForegroundColor,
                    backgroundColor = selectedBackgroundColor,
                    bodyShape = selectedBodyShape,
                    logoBitmap = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type)),
                    errorCorrectionLevel = selectedErrorCorrectionLevel,
                    logoSizeRatio = selectedLogoSizeRatio,
                    logoBadgeShape = selectedLogoBadgeShape,
                    customEmoji = customEmojiText
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
                            logoBitmap = if (type == "Wifi" || type == "Email" || type == "SMS" || type == "VCard" || type == "Text" || type == "Event") null else (selectedLogoBitmap ?: getSocialLogo(context, type))
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
                }
                
                val finalBitmapToSave = generatedBitmap!!
                onSave(contentToEncode, format.name, finalBitmapToSave)
                savedBitmapForDialog = finalBitmapToSave
                showSuccessDialog = true
                val successMsg = if (editingCode != null) "Código actualizado con éxito" else context.getString(R.string.toast_generated_success)
                Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: context.getString(R.string.toast_cant_generate)
                Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, context.getString(R.string.toast_cant_generate), Toast.LENGTH_SHORT).show()
        }
    }


    Column(
        modifier = Modifier.fillMaxSize().imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // FIXED TOP SECTION: Top Bar & QR Code Live Preview Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
                    }
                    Text(
                        text = stringResource(R.string.editor_title),
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    androidx.compose.material3.Button(
                        onClick = performSave,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(stringResource(R.string.btn_save), style = MaterialTheme.typography.labelLarge)
                    }
                }

                // QR Preview Card (Fixed at the top)
                if (type == "QR_ANIMATED" && transferMode == "RECEIVE") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (cameraPermissionState.status.isGranted) {
                            com.example.ui.screens.CameraPreview(
                                isPaused = false,
                                isTorchEnabled = false,
                                isFrontCamera = false,
                                zoomLevel = 0f,
                                onBarcodeScanned = { value, format ->
                                    if (value.startsWith("ANIM_QR|")) {
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
                                                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                                    try {
                                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                            vibrator?.vibrate(android.os.VibrationEffect.createOneShot(25, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                                        } else {
                                                            @Suppress("DEPRECATION")
                                                            vibrator?.vibrate(25)
                                                        }
                                                    } catch (e: Exception) {}
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                                    Text(stringResource(R.string.permission_camera))
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (livePreviewBitmap != null) {
                                val is1DBarcode = type in listOf("EAN_8", "EAN_13", "UPC_E", "UPC_A", "CODE_39", "CODE_93", "CODE_128", "BARCODE_128", "ITF", "CODABAR", "PDF_417")
                                val isAnimQr = type == "QR_ANIMATED"
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        bitmap = livePreviewBitmap!!.asImageBitmap(),
                                        contentDescription = "Live Preview",
                                        modifier = if (is1DBarcode) {
                                            Modifier.fillMaxWidth().height(110.dp).padding(4.dp)
                                        } else if (isAnimQr) {
                                            Modifier.size(170.dp).padding(2.dp)
                                        } else {
                                            Modifier.size(165.dp).padding(2.dp)
                                        }
                                    )
                                    if (!is1DBarcode && !isAnimQr) {
                                        val hasActiveLogo = !removeLogo && (selectedLogoBitmap != null || getSocialLogo(context, type) != null)
                                        val scanQuality = CodeGenerator.getScanQuality(
                                            fgColor = selectedForegroundColor,
                                            bgColor = selectedBackgroundColor,
                                            hasLogo = hasActiveLogo,
                                            eccLevel = selectedErrorCorrectionLevel
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when (scanQuality.level) {
                                                ScanQualityLevel.EXCELLENT -> Color(0xFF10B981).copy(alpha = 0.12f)
                                                ScanQualityLevel.GOOD -> Color(0xFF3B82F6).copy(alpha = 0.12f)
                                                ScanQualityLevel.WARNING -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                                ScanQualityLevel.POOR -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp)
                                                .clickable { showQualityInfoDialog = true }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = when (scanQuality.level) {
                                                        ScanQualityLevel.EXCELLENT, ScanQualityLevel.GOOD -> Icons.Filled.CheckCircle
                                                        ScanQualityLevel.WARNING, ScanQualityLevel.POOR -> Icons.Filled.Warning
                                                    },
                                                    contentDescription = null,
                                                    tint = when (scanQuality.level) {
                                                        ScanQualityLevel.EXCELLENT -> Color(0xFF059669)
                                                        ScanQualityLevel.GOOD -> Color(0xFF2563EB)
                                                        ScanQualityLevel.WARNING -> Color(0xFFD97706)
                                                        ScanQualityLevel.POOR -> Color(0xFFDC2626)
                                                    },
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(5.dp))
                                                Text(
                                                    text = "${scanQuality.label} • ${String.format(java.util.Locale.US, "%.1f", scanQuality.contrastRatio)}:1 • ECC: ${selectedErrorCorrectionLevel.name}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = when (scanQuality.level) {
                                                        ScanQualityLevel.EXCELLENT -> Color(0xFF065F46)
                                                        ScanQualityLevel.GOOD -> Color(0xFF1E40AF)
                                                        ScanQualityLevel.WARNING -> Color(0xFF92400E)
                                                        ScanQualityLevel.POOR -> Color(0xFF991B1B)
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (scanQuality.level == ScanQualityLevel.POOR || scanQuality.level == ScanQualityLevel.WARNING) {
                                                    TextButton(
                                                        onClick = {
                                                            selectedForegroundColor = android.graphics.Color.BLACK
                                                            selectedBackgroundColor = android.graphics.Color.WHITE
                                                            selectedErrorCorrectionLevel = ErrorCorrectionLevel.H
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                                        modifier = Modifier.height(24.dp)
                                                    ) {
                                                        Text("Corregir", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Icon(
                                                        Icons.Filled.Info,
                                                        contentDescription = "Detalles",
                                                        modifier = Modifier.size(14.dp),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (isAnimQr) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (isAnimating) stringResource(R.string.anim_playing) else stringResource(R.string.anim_paused),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isAnimating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "${currentChunkIndex + 1} / ${fileChunks.size} (${if (fileChunks.isNotEmpty()) ((currentChunkIndex + 1) * 100) / fileChunks.size else 0}%)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { if (fileChunks.isNotEmpty()) (currentChunkIndex + 1).toFloat() / fileChunks.size else 0f },
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                if (type == "QR_ANIMATED") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(10.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.FileUpload, 
                                                contentDescription = null, 
                                                tint = MaterialTheme.colorScheme.primary, 
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Selecciona un archivo para transferir",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Icon(Icons.Filled.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                stringResource(R.string.generate_instruction),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodySmall,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        androidx.compose.material3.HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            thickness = 1.dp
        )

        // SCROLLABLE OPTIONS SECTION
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (type == "QR_ANIMATED") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("⚡ Rayo Óptico (RGB 3X)", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Transmisión multiplexada a todo color (~1 MB/s)", color = Color(0xFFE0E0E0), style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = { 
                                if (isPro) {
                                    onNavigateToUltraTransfer?.invoke()
                                } else {
                                    showPaywallDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black)
                        ) {
                            Icon(if (isPro) Icons.Filled.Check else Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isPro) "Abrir" else "PRO", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = transferMode == "SEND",
                        onClick = { transferMode = "SEND" },
                        label = { Text("Enviar Archivo") }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(
                        selected = transferMode == "RECEIVE",
                        onClick = { transferMode = "RECEIVE" },
                        label = { Text("Recibir Archivo") }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (transferMode == "RECEIVE" && (receivingAnimatedQr || receivedChunks.isNotEmpty())) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        val percent = if (totalAnimatedChunks > 0) ((receivedChunks.size.toFloat() / totalAnimatedChunks) * 100).toInt() else 0
                        Text(
                            text = "Recibiendo $animatedFileName: ${receivedChunks.size} / $totalAnimatedChunks ($percent%)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = { if (totalAnimatedChunks > 0) receivedChunks.size.toFloat() / totalAnimatedChunks else 0f },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        if (receivedChunks.size == totalAnimatedChunks && totalAnimatedChunks > 0) {
                            Button(
                                onClick = {
                                    try {
                                        val sortedData = receivedChunks.entries.sortedBy { it.key }.joinToString("") { it.value }
                                        val bytes = android.util.Base64.decode(sortedData, android.util.Base64.NO_WRAP)
                                        val fName = animatedFileName.ifEmpty { "archivo_transferido" }
                                        
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
                                                Toast.makeText(context, "Archivo guardado en Descargas: $fName", Toast.LENGTH_LONG).show()
                                                receivingAnimatedQr = false
                                                receivedChunks.clear()
                                            } ?: run {
                                                Toast.makeText(context, "Error al crear archivo", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                                            val file = java.io.File(downloadsDir, fName)
                                            java.io.FileOutputStream(file).use { it.write(bytes) }
                                            Toast.makeText(context, "Archivo guardado en Descargas: $fName", Toast.LENGTH_LONG).show()
                                            receivingAnimatedQr = false
                                            receivedChunks.clear()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, "Error al guardar el archivo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Guardar Archivo en Descargas")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            val explanation = when(type) {
                "QR_SECRET" -> stringResource(R.string.exp_secret)
                "QR_VCARD_PRO" -> stringResource(R.string.exp_vcard)
                "QR_WIFI_PRO" -> stringResource(R.string.exp_wifi)
                "QR_STICKERS" -> stringResource(R.string.exp_stickers)
                "QR_WATERMARK" -> stringResource(R.string.exp_watermark)
                "QR_ANIMATED" -> stringResource(R.string.exp_animated)
                else -> null
            }
            if (explanation != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("💡", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

        // Form Fields Area with Real-Time validation
        if (type != "QR_ANIMATED" || transferMode == "SEND") {
            when (type) {
            "QR_SECRET" -> {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.secret_desc),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                OutlinedTextField(
                    value = secretMessage,
                    onValueChange = { secretMessage = it; p1Error = null },
                    label = { Text("Mensaje Secreto / Confidencial *") },
                    placeholder = { Text(stringResource(R.string.secret_placeholder)) },
                    isError = p1Error != null,
                    supportingText = p1Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = secretPin,
                    onValueChange = { secretPin = it; p2Error = null },
                    label = { Text(stringResource(R.string.secret_label)) },
                    placeholder = { Text("Ej: 8492 o MiClaveSecreta") },
                    isError = p2Error != null,
                    supportingText = p2Error?.let { { Text(it) } } ?: { Text(stringResource(R.string.secret_support)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (isSecretPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isSecretPinVisible = !isSecretPinVisible }) {
                            Icon(
                                if (isSecretPinVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Mostrar u ocultar PIN"
                            )
                        }
                    }
                )
                content = if (secretMessage.isNotBlank() && secretPin.isNotBlank()) SecretQrHelper.encrypt(secretMessage, secretPin) else ""
            }
            "QR_VCARD_PRO" -> {
                OutlinedTextField(
                    value = p1,
                    onValueChange = { p1 = it; p1Error = null },
                    label = { Text("Nombre Completo *") },
                    isError = p1Error != null,
                    supportingText = p1Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = p2,
                    onValueChange = { p2 = it; p2Error = null },
                    label = { Text(stringResource(R.string.phone_label)) },
                    isError = p2Error != null,
                    supportingText = p2Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = p3,
                    onValueChange = { p3 = it; p3Error = null },
                    label = { Text(stringResource(R.string.email_label)) },
                    isError = p3Error != null,
                    supportingText = p3Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = vcardOrg,
                    onValueChange = { vcardOrg = it },
                    label = { Text(stringResource(R.string.company_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = vcardUrl,
                    onValueChange = { vcardUrl = it },
                    label = { Text("Sitio Web o Perfil") },
                    modifier = Modifier.fillMaxWidth()
                )
                content = buildString {
                    append("BEGIN:VCARD\nVERSION:3.0\n")
                    append("FN:$p1\nN:$p1;;;;\n")
                    if (p2.isNotBlank()) append("TEL;TYPE=CELL:$p2\n")
                    if (p3.isNotBlank()) append("EMAIL:$p3\n")
                    if (vcardOrg.isNotBlank()) append("ORG:$vcardOrg\n")
                    if (vcardUrl.isNotBlank()) append("URL:$vcardUrl\n")
                    append("END:VCARD")
                }
            }
            "QR_WIFI_PRO" -> {
                OutlinedTextField(
                    value = p1,
                    onValueChange = { p1 = it; p1Error = null },
                    label = { Text(stringResource(R.string.label_ssid)) },
                    isError = p1Error != null,
                    supportingText = p1Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = p2,
                    onValueChange = { p2 = it; p2Error = null },
                    label = { Text(stringResource(R.string.label_password)) },
                    isError = p2Error != null,
                    supportingText = p2Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = p3,
                    onValueChange = { p3 = it; p3Error = null },
                    label = { Text("Seguridad (WPA, WPA2, WEP o nopass)") },
                    placeholder = { Text("WPA") },
                    modifier = Modifier.fillMaxWidth()
                )
                content = "WIFI:S:$p1;T:${p3.ifEmpty { "WPA" }};P:$p2;;"
            }
            "QR_STICKERS" -> {
                OutlinedTextField(
                    value = p1,
                    onValueChange = { p1 = it; p1Error = null },
                    label = { Text(stringResource(R.string.tag_label)) },
                    isError = p1Error != null,
                    supportingText = p1Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = p2,
                    onValueChange = { p2 = it; p2Error = null },
                    label = { Text("Contenido o Enlace del QR *") },
                    isError = p2Error != null,
                    supportingText = p2Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                content = p2
                
                if (livePreviewBitmap != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { com.example.utils.HapticUtil.playSuccessHaptic(context); exportStickerSheetPdf(context, livePreviewBitmap!!, p1.ifEmpty { "QR Stickers" }, p2) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("🖨️ Descargar Hoja A4 con 12 Stickers (PDF)")
                    }
                }
            }
            "QR_WATERMARK" -> {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it; generalError = null },
                    label = { Text("Contenido o Enlace del QR *") },
                    isError = generalError != null,
                    supportingText = generalError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = p1,
                    onValueChange = { p1 = it },
                    label = { Text("Texto de la Marca de Agua (Ej: ORIGINAL, CONFIDENCIAL)") },
                    placeholder = { Text("OFICIAL") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "QR_ANIMATED" -> {
                if (fileChunks.isEmpty()) {
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Seleccionar Archivo a Enviar", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedFileName ?: "Archivo",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    val cycleSec = String.format(java.util.Locale.US, "%.1f", (fileChunks.size * animDelayMs) / 1000f)
                                    Text(
                                        text = "${fileChunks.size} fragmentos · Ciclo: ~${cycleSec}s",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                OutlinedButton(
                                    onClick = { filePickerLauncher.launch("*/*") },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Cambiar", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Playback Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                isAnimating = false
                                currentChunkIndex = if (currentChunkIndex > 0) currentChunkIndex - 1 else fileChunks.size - 1
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.SkipPrevious, contentDescription = "Anterior", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Anterior", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = { isAnimating = !isAnimating },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAnimating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Icon(if (isAnimating) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isAnimating) "Pausar" else "Reproducir", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = {
                                isAnimating = false
                                currentChunkIndex = (currentChunkIndex + 1) % fileChunks.size
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Text("Siguiente", style = MaterialTheme.typography.labelSmall)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.SkipNext, contentDescription = "Siguiente", modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Speed Configuration
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            val fps = String.format(java.util.Locale.US, "%.1f", 1000f / animDelayMs)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.anim_speed_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${animDelayMs}ms (${fps} FPS)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            // Preset Speed Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = animDelayMs == 150L,
                                    onClick = { animDelayMs = 150L },
                                    label = { Text("150ms (Ultra)", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = animDelayMs == 200L,
                                    onClick = { animDelayMs = 200L },
                                    label = { Text(stringResource(R.string.anim_speed_fast), style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = animDelayMs == 280L,
                                    onClick = { animDelayMs = 280L },
                                    label = { Text("280ms (Normal)", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = animDelayMs == 400L,
                                    onClick = { animDelayMs = 400L },
                                    label = { Text("400ms (Segura)", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = animDelayMs == 600L,
                                    onClick = { animDelayMs = 600L },
                                    label = { Text("600ms (Lenta)", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = animDelayMs == 800L,
                                    onClick = { animDelayMs = 800L },
                                    label = { Text("800ms (Muy lenta)", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            Slider(
                                value = animDelayMs.toFloat(),
                                onValueChange = { animDelayMs = (it / 10).toLong() * 10L },
                                valueRange = 100f..1000f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            "QR_WIFI" -> {
                OutlinedTextField(
                    value = p1,
                    onValueChange = { p1 = it; p1Error = null },
                    label = { Text(stringResource(R.string.label_ssid)) },
                    isError = p1Error != null,
                    supportingText = p1Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = p2,
                    onValueChange = { p2 = it; p2Error = null },
                    label = { Text(stringResource(R.string.label_password)) },
                    isError = p2Error != null,
                    supportingText = p2Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = p3,
                    onValueChange = { p3 = it; p3Error = null },
                    label = { Text(stringResource(R.string.label_security)) },
                    isError = p3Error != null,
                    supportingText = p3Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                content = "WIFI:S:$p1;T:${p3.ifEmpty { "WPA" }};P:$p2;;"
            }
            "QR_PHONE" -> {
                OutlinedTextField(
                    value = p1,
                    onValueChange = { p1 = it; p1Error = null },
                    label = { Text(stringResource(R.string.label_phone)) },
                    isError = p1Error != null,
                    supportingText = p1Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                content = "tel:$p1"
            }
            "QR_WHATSAPP" -> {
                OutlinedTextField(
                    value = p1,
                    onValueChange = { p1 = it; p1Error = null },
                    label = { Text(stringResource(R.string.label_phone)) },
                    isError = p1Error != null,
                    supportingText = p1Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                content = "https://wa.me/${p1.replace(Regex("[^0-9]"), "")}"
            }
            "QR_URL" -> {
                OutlinedTextField(
                    value = p1,
                    onValueChange = { p1 = it; p1Error = null },
                    label = { Text(stringResource(R.string.label_url)) },
                    isError = p1Error != null,
                    supportingText = p1Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                content = if (p1.startsWith("http")) p1 else "https://$p1"
            }
            "QR_EMAIL" -> {
                OutlinedTextField(
                    value = p1,
                    onValueChange = { p1 = it; p1Error = null },
                    label = { Text(stringResource(R.string.label_recipient_email)) },
                    isError = p1Error != null,
                    supportingText = p1Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = p2,
                    onValueChange = { p2 = it; p2Error = null },
                    label = { Text(stringResource(R.string.label_subject)) },
                    isError = p2Error != null,
                    supportingText = p2Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                content = "mailto:$p1?subject=$p2"
            }
            "QR_SMS" -> {
                OutlinedTextField(
                    value = p1,
                    onValueChange = { p1 = it; p1Error = null },
                    label = { Text(stringResource(R.string.label_recipient_sms)) },
                    isError = p1Error != null,
                    supportingText = p1Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = p2,
                    onValueChange = { p2 = it },
                    label = { Text(stringResource(R.string.label_body_sms)) },
                    modifier = Modifier.fillMaxWidth()
                )
                content = "sms:$p1?body=$p2"
            }
            "QR_CONTACT" -> {
                OutlinedTextField(
                    value = p1,
                    onValueChange = { p1 = it; p1Error = null },
                    label = { Text(stringResource(R.string.label_contact_name)) },
                    isError = p1Error != null,
                    supportingText = p1Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = p2,
                    onValueChange = { p2 = it; p2Error = null },
                    label = { Text(stringResource(R.string.label_contact_phone)) },
                    isError = p2Error != null,
                    supportingText = p2Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = p3,
                    onValueChange = { p3 = it; p3Error = null },
                    label = { Text(stringResource(R.string.label_contact_email)) },
                    isError = p3Error != null,
                    supportingText = p3Error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                content = "BEGIN:VCARD\nVERSION:3.0\nN:$p1\nTEL:$p2\nEMAIL:$p3\nEND:VCARD"
            }
            else -> {
                val isNumericBarcode = type in listOf("EAN_8", "EAN_13", "UPC_E", "UPC_A", "ITF")
                val maxLength = when(type) {
                    "EAN_8", "UPC_E" -> 8
                    "UPC_A" -> 12
                    "EAN_13" -> 13
                    else -> Int.MAX_VALUE
                }
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { 
                        if (isNumericBarcode) {
                            val filtered = it.filter { char -> char.isDigit() }
                            if (filtered.length <= maxLength) {
                                content = filtered
                                generalError = null
                            }
                        } else {
                            content = it
                            generalError = null
                        }
                    },
                    label = { Text(stringResource(R.string.label_text_content)) },
                    isError = generalError != null,
                    supportingText = generalError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = if (isNumericBarcode) androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number) else androidx.compose.foundation.text.KeyboardOptions.Default
                )
            }
        }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (type != "QR_ANIMATED") {
            val isQrCode = type.startsWith("QR_") && type != "QR_ANIMATED"
            var activeCustomTab by remember { mutableIntStateOf(0) }
            var colorSubTab by remember { mutableIntStateOf(0) }
            var logoSubTab by remember { mutableIntStateOf(0) }
            var plantillaSubTab by remember { mutableStateOf("Popular") }
    val templates = remember(plantillaSubTab) {
        when (plantillaSubTab) {
            "Wifi" -> listOf(
                QrTemplate("Clásico", android.graphics.Color.BLACK, android.graphics.Color.WHITE, com.example.utils.BodyShape.SQUARE),
                QrTemplate("Router", android.graphics.Color.parseColor("#0052CC"), android.graphics.Color.parseColor("#E6F0FF"), com.example.utils.BodyShape.ROUNDED),
                QrTemplate("Conexión", android.graphics.Color.parseColor("#FF9900"), android.graphics.Color.parseColor("#FFF5E6"), com.example.utils.BodyShape.SQUARE)
            )
            "Negocio" -> listOf(
                QrTemplate("Elegante", android.graphics.Color.parseColor("#333333"), android.graphics.Color.parseColor("#F5F5F5"), com.example.utils.BodyShape.ROUNDED),
                QrTemplate("Corporativo", android.graphics.Color.parseColor("#003366"), android.graphics.Color.parseColor("#FFFFFF"), com.example.utils.BodyShape.SQUARE),
                QrTemplate("Premium", android.graphics.Color.parseColor("#D4AF37"), android.graphics.Color.parseColor("#000000"), com.example.utils.BodyShape.DIAMOND)
            )
            "Evento" -> listOf(
                QrTemplate("Fiesta", android.graphics.Color.parseColor("#FF00FF"), android.graphics.Color.parseColor("#1A1A1A"), com.example.utils.BodyShape.CIRCLE),
                QrTemplate("Concierto", android.graphics.Color.parseColor("#FF3300"), android.graphics.Color.parseColor("#1A1A1A"), com.example.utils.BodyShape.STAR),
                QrTemplate("Boda", android.graphics.Color.parseColor("#A8B5A1"), android.graphics.Color.parseColor("#FFFFFF"), com.example.utils.BodyShape.HEART)
            )
            else -> listOf(
                QrTemplate("Clásico", android.graphics.Color.BLACK, android.graphics.Color.WHITE, com.example.utils.BodyShape.SQUARE),
                QrTemplate("Océano", android.graphics.Color.parseColor("#003366"), android.graphics.Color.parseColor("#E6F2FF"), com.example.utils.BodyShape.ROUNDED),
                QrTemplate("Bosque", android.graphics.Color.parseColor("#004D00"), android.graphics.Color.parseColor("#E6FFE6"), com.example.utils.BodyShape.CIRCLE),
                QrTemplate("Neón", android.graphics.Color.parseColor("#FF00FF"), android.graphics.Color.parseColor("#1A1A1A"), com.example.utils.BodyShape.SQUARE),
                QrTemplate("Atardecer", android.graphics.Color.parseColor("#CC3300"), android.graphics.Color.parseColor("#FFF0E6"), com.example.utils.BodyShape.ROUNDED)
            )
        }
    }
            var showAdvancedColorPicker by remember { mutableStateOf(false) }

            // High-Contrast Curated Palettes
            val curatedPalettes = remember {
                listOf(
                    Pair("Clásico", Pair(android.graphics.Color.BLACK, android.graphics.Color.WHITE)),
                    Pair("Océano", Pair(android.graphics.Color.parseColor("#0D47A1"), android.graphics.Color.parseColor("#E3F2FD"))),
                    Pair("Esmeralda", Pair(android.graphics.Color.parseColor("#1B5E20"), android.graphics.Color.parseColor("#E8F5E9"))),
                    Pair("Púrpura", Pair(android.graphics.Color.parseColor("#4A148C"), android.graphics.Color.parseColor("#F3E5F5"))),
                    Pair("Atardecer", Pair(android.graphics.Color.parseColor("#E65100"), android.graphics.Color.parseColor("#FFF3E0"))),
                    Pair("Cyberpunk", Pair(android.graphics.Color.parseColor("#00E5FF"), android.graphics.Color.parseColor("#121212"))),
                    Pair("Rubí", Pair(android.graphics.Color.parseColor("#B71C1C"), android.graphics.Color.parseColor("#FFEBEE"))),
                    Pair("Moca", Pair(android.graphics.Color.parseColor("#3E2723"), android.graphics.Color.parseColor("#EFEBE9")))
                )
            }

            val presetColors = remember {
                listOf(
                    android.graphics.Color.BLACK,
                    android.graphics.Color.WHITE,
                    android.graphics.Color.parseColor("#0D47A1"), // Deep Blue
                    android.graphics.Color.parseColor("#1B5E20"), // Deep Green
                    android.graphics.Color.parseColor("#B71C1C"), // Deep Red
                    android.graphics.Color.parseColor("#E65100"), // Orange
                    android.graphics.Color.parseColor("#4A148C"), // Purple
                    android.graphics.Color.parseColor("#00838F"), // Teal
                    android.graphics.Color.parseColor("#FFD600"), // Yellow
                    android.graphics.Color.parseColor("#C2185B"), // Magenta
                    android.graphics.Color.parseColor("#00E5FF"), // Cyan
                    android.graphics.Color.parseColor("#607D8B"), // Blue Grey
                    android.graphics.Color.parseColor("#3E2723"), // Dark Brown
                    android.graphics.Color.parseColor("#212121")  // Graphite
                )
            }

                    // TAB 0: PLANTILLAS (Ready-to-use Themes)
                    if (isQrCode) ExpandableSection(title = "Plantillas Prediseñadas", icon = Icons.Default.GridView, initiallyExpanded = false) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Active Template status bar
                            if (selectedTemplateDef != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Plantilla activa: ${selectedTemplateDef?.name}",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "Puedes editar los textos en la pestaña 'Texto'",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                        TextButton(
                                            onClick = {
                                                selectedTemplateDef = null
                                                selectedForegroundColor = android.graphics.Color.BLACK
                                                selectedBackgroundColor = android.graphics.Color.WHITE
                                            }
                                        ) {
                                            Text("Quitar", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Category filter chips
                            val categories = listOf("Todos") + TemplateRenderer.AVAILABLE_TEMPLATES.map { it.category }.distinct()
                            if (plantillaSubTab !in categories) {
                                plantillaSubTab = "Todos"
                            }
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 12.dp)
                            ) {
                                items(categories) { tab ->
                                    val isSelected = tab == plantillaSubTab
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { plantillaSubTab = tab },
                                        label = { Text(tab, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            
                            val filteredTemplates = if (plantillaSubTab == "Todos") TemplateRenderer.AVAILABLE_TEMPLATES else TemplateRenderer.AVAILABLE_TEMPLATES.filter { it.category == plantillaSubTab }
                            val chunkedTemplates = filteredTemplates.chunked(2)
                            chunkedTemplates.forEach { rowTemplates ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowTemplates.forEach { templateDef ->
                                        val isSelected = selectedTemplateDef?.id == templateDef.id
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(175.dp)
                                                .clickable {
                                                    val isPremiumTemplate = templateDef.category == "Negocios" || templateDef.category == "Eventos" || templateDef.category == "Negocio" || templateDef.category == "Evento"
                                                    if (isPremiumTemplate && !isPro) {
                                                        showPaywallDialog = true
                                                    } else {
                                                        if (isSelected) {
                                                            selectedTemplateDef = null
                                                            selectedForegroundColor = android.graphics.Color.BLACK
                                                            selectedBackgroundColor = android.graphics.Color.WHITE
                                                        } else {
                                                            selectedTemplateDef = templateDef
                                                            selectedForegroundColor = templateDef.primaryColor
                                                            selectedBackgroundColor = android.graphics.Color.WHITE
                                                            if (customTitleText.isBlank()) customTitleText = templateDef.defaultText
                                                            if (customSubText.isBlank()) customSubText = templateDef.defaultSubtext
                                                        }
                                                    }
                                                },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                            )
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                                                val isPremiumTemplate = templateDef.category == "Negocios" || templateDef.category == "Eventos" || templateDef.category == "Negocio" || templateDef.category == "Evento"
                                                if (isPremiumTemplate && !isPro) {
                                                    Surface(
                                                        modifier = Modifier.align(Alignment.TopEnd),
                                                        color = MaterialTheme.colorScheme.primaryContainer,
                                                        shape = CircleShape
                                                    ) {
                                                        Icon(Icons.Filled.Lock, contentDescription = "Premium", modifier = Modifier.padding(4.dp).size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    androidx.compose.foundation.Image(
                                                        painter = androidx.compose.ui.res.painterResource(id = templateDef.vectorResId),
                                                        contentDescription = templateDef.name,
                                                        modifier = Modifier.weight(1f).fillMaxWidth().padding(4.dp),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = templateDef.name, 
                                                        style = MaterialTheme.typography.labelMedium, 
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface, 
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = templateDef.category,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1
                                                    )
                                                }
                                                if (isSelected) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(24.dp).align(Alignment.TopEnd)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                Icons.Filled.Check,
                                                                contentDescription = "Seleccionado",
                                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
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
                    }

                    // TAB 1: LOGO
                    if (isQrCode) ExpandableSection(title = "Logotipo y Marca", icon = Icons.Default.Image) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Active logo preview & Action Bar
                            val currentLogo = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type))
                            if (currentLogo != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                bitmap = currentLogo.asImageBitmap(),
                                                contentDescription = "Logotipo actual",
                                                modifier = Modifier.size(52.dp).background(Color.White, RoundedCornerShape(10.dp)).padding(4.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        text = if (selectedLogoPresetId == "CUSTOM_UPLOAD") "Tu logotipo personalizado" else "Logotipo activo",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                                Text(
                                                    text = "Protegido con marco automático de alto contraste",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    removeLogo = true
                                                    selectedLogoBitmap = null
                                                    selectedLogoPresetId = null
                                                }
                                            ) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Quitar logotipo", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }

                                        androidx.compose.material3.HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                                        )

                                        // Logo Size Slider
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Tamaño del Logo",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "${(selectedLogoSizeRatio * 100).toInt()}% del QR",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Slider(
                                            value = selectedLogoSizeRatio,
                                            onValueChange = { selectedLogoSizeRatio = it },
                                            valueRange = 0.12f..0.28f,
                                            steps = 15,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(Modifier.height(4.dp))

                                        // Badge Shape Selector
                                        Text(
                                            text = "Forma del Fondo / Marco",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val badgeShapes = listOf(
                                                Pair("Redondeado", LogoBadgeShape.ROUNDED_SQUARE),
                                                Pair("Círculo", LogoBadgeShape.CIRCLE),
                                                Pair("Cuadrado", LogoBadgeShape.SQUARE),
                                                Pair("Sin Fondo", LogoBadgeShape.NONE)
                                            )
                                            badgeShapes.forEach { (label, shape) ->
                                                val isSelected = selectedLogoBadgeShape == shape
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { selectedLogoBadgeShape = shape },
                                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Sub-tabs (Subir Mi Logo, Redes, Símbolos, Texto)
                            SecondaryTabRow(
                                selectedTabIndex = logoSubTab.coerceIn(0, 3),
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            ) {
                                Tab(
                                    selected = logoSubTab == 0,
                                    onClick = { logoSubTab = 0 },
                                    text = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Subir Logo", fontWeight = if (logoSubTab == 0) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                )
                                Tab(
                                    selected = logoSubTab == 1,
                                    onClick = { logoSubTab = 1 },
                                    text = { Text("Redes", fontWeight = if (logoSubTab == 1) FontWeight.Bold else FontWeight.Normal) }
                                )
                                Tab(
                                    selected = logoSubTab == 2,
                                    onClick = { logoSubTab = 2 },
                                    text = { Text("Símbolos", fontWeight = if (logoSubTab == 2) FontWeight.Bold else FontWeight.Normal) }
                                )
                                Tab(
                                    selected = logoSubTab == 3,
                                    onClick = { logoSubTab = 3 },
                                    text = { Text("Siglas/Texto", fontWeight = if (logoSubTab == 3) FontWeight.Bold else FontWeight.Normal) }
                                )
                            }
                            Spacer(Modifier.height(14.dp))

                            // SUBTAB 0: SUBIR MI PROPIO LOGO DESDE GALERÍA / ARCHIVOS
                            if (logoSubTab == 0) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isPro) {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                            logoPickerLauncher.launch("image/*")
                                                        } else {
                                                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                                        }
                                                    } else {
                                                        showPaywallDialog = true
                                                    }
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                modifier = Modifier.size(64.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        Icons.Filled.AddPhotoAlternate,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(36.dp)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(12.dp))
                                            Text(
                                                text = "Toca aquí para subir tu propio logotipo",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = "Formatos compatibles: PNG, JPG, JPEG, WEBP\n(Ideal para marcas de empresas, logos personales o iconos)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            Spacer(Modifier.height(14.dp))
                                            Button(
                                                onClick = {
                                                    if (isPro) {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                            logoPickerLauncher.launch("image/*")
                                                        } else {
                                                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                                        }
                                                    } else {
                                                        showPaywallDialog = true
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(if (isPro) Icons.Filled.FileUpload else Icons.Filled.Lock, contentDescription = null)
                                                Spacer(Modifier.width(8.dp))
                                                Text(if (!isPro) "Usar Logo Propio (PRO)" else if (selectedLogoBitmap != null && selectedLogoPresetId == "CUSTOM_UPLOAD") "Cambiar imagen seleccionada" else "Elegir archivo de imagen")
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Filled.AutoAwesome,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "Consejo: Para mayor elegancia, usa una imagen cuadrada o con fondo transparente.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            } else if (logoSubTab == 1) {
                                val socialLogos = listOf(
                                    Triple("whatsapp", "WhatsApp", R.drawable.ic_whatsapp),
                                    Triple("instagram", "Instagram", R.drawable.ic_instagram),
                                    Triple("facebook", "Facebook", R.drawable.ic_facebook),
                                    Triple("tiktok", "TikTok", R.drawable.ic_tiktok),
                                    Triple("youtube", "YouTube", R.drawable.ic_youtube),
                                    Triple("telegram", "Telegram", R.drawable.ic_telegram),
                                    Triple("twitter", "X / Twitter", R.drawable.ic_twitter),
                                    Triple("messenger", "Messenger", R.drawable.ic_messenger),
                                    Triple("linkedin", "LinkedIn", R.drawable.ic_linkedin),
                                    Triple("app_logo", "QR Oficial", R.drawable.ic_app_logo)
                                )
                                val chunkedLogos = socialLogos.chunked(5)
                                chunkedLogos.forEach { rowLogos ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowLogos.forEach { (id, name, resId) ->
                                            val isSelected = selectedLogoPresetId == id && !removeLogo
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(68.dp)
                                                    .clickable {
                                                        val loaded = ContextCompat.getDrawable(context, resId)?.let { d ->
                                                            val bm = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
                                                            val c = Canvas(bm)
                                                            d.setBounds(0, 0, 256, 256)
                                                            d.draw(c)
                                                            bm
                                                        }
                                                        if (loaded != null) {
                                                            selectedLogoBitmap = loaded
                                                            selectedLogoPresetId = id
                                                            removeLogo = false
                                                        }
                                                    },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                ),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    androidx.compose.foundation.Image(
                                                        painter = androidx.compose.ui.res.painterResource(id = resId),
                                                        contentDescription = name,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        text = name,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                        repeat(5 - rowLogos.size) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else if (logoSubTab == 2) {
                                val utilitySymbols = listOf(
                                    Triple("wifi", "Wi-Fi", "📶"),
                                    Triple("phone", "Teléfono", "📞"),
                                    Triple("email", "Email", "✉️"),
                                    Triple("web", "Sitio Web", "🌐"),
                                    Triple("location", "Ubicación", "📍"),
                                    Triple("store", "Tienda", "🛒"),
                                    Triple("menu", "Menú", "🍽️"),
                                    Triple("star", "Favorito", "⭐"),
                                    Triple("heart", "Corazón", "❤️"),
                                    Triple("lock", "Seguro", "🔒"),
                                    Triple("contact", "Contacto", "👤"),
                                    Triple("card", "Pago", "💳")
                                )
                                val chunkedSymbols = utilitySymbols.chunked(4)
                                chunkedSymbols.forEach { rowSymbols ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowSymbols.forEach { (id, name, symbol) ->
                                            val isSelected = selectedLogoPresetId == id && !removeLogo
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(68.dp)
                                                    .clickable {
                                                        val bm = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
                                                        val c = Canvas(bm)
                                                        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                                            color = android.graphics.Color.parseColor("#0284C7")
                                                            style = Paint.Style.FILL
                                                        }
                                                        c.drawRoundRect(RectF(10f, 10f, 246f, 246f), 52f, 52f, p)
                                                        val bp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                                            color = android.graphics.Color.WHITE
                                                            style = Paint.Style.STROKE
                                                            strokeWidth = 8f
                                                        }
                                                        c.drawRoundRect(RectF(15f, 15f, 241f, 241f), 48f, 48f, bp)
                                                        val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                                            textAlign = Paint.Align.CENTER
                                                            textSize = 100f
                                                        }
                                                        val y = (256f / 2f) - ((tp.descent() + tp.ascent()) / 2f)
                                                        c.drawText(symbol, 128f, y, tp)
                                                        
                                                        selectedLogoBitmap = bm
                                                        selectedLogoPresetId = id
                                                        removeLogo = false
                                                    },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                ),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(text = symbol, fontSize = 22.sp)
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        text = name,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                        repeat(4 - rowSymbols.size) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else {
                                // Subtab 3: Insignia de Texto
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Insignia personalizada con letras / siglas (Ej. VIP, QR, PAY, PRO):",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = logoTextBadgeInput,
                                            onValueChange = { if (it.length <= 5) logoTextBadgeInput = it.uppercase() },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("Ej. VIP / QR") },
                                            singleLine = true
                                        )
                                        Button(
                                            onClick = {
                                                if (logoTextBadgeInput.isNotBlank()) {
                                                    val bm = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
                                                    val c = Canvas(bm)
                                                    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                                        color = logoTextBadgeColor
                                                        style = Paint.Style.FILL
                                                    }
                                                    c.drawRoundRect(RectF(10f, 10f, 246f, 246f), 48f, 48f, p)
                                                    val bp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                                        color = android.graphics.Color.WHITE
                                                        style = Paint.Style.STROKE
                                                        strokeWidth = 8f
                                                    }
                                                    c.drawRoundRect(RectF(15f, 15f, 241f, 241f), 44f, 44f, bp)
                                                    val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                                        color = android.graphics.Color.WHITE
                                                        isFakeBoldText = true
                                                        textAlign = Paint.Align.CENTER
                                                        textSize = when {
                                                            logoTextBadgeInput.length <= 2 -> 92f
                                                            logoTextBadgeInput.length <= 4 -> 64f
                                                            else -> 46f
                                                        }
                                                    }
                                                    val y = (256f / 2f) - ((tp.descent() + tp.ascent()) / 2f)
                                                    c.drawText(logoTextBadgeInput, 128f, y, tp)
                                                    
                                                    selectedLogoBitmap = bm
                                                    selectedLogoPresetId = "TEXT_BADGE"
                                                    removeLogo = false
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            enabled = logoTextBadgeInput.isNotBlank()
                                        ) {
                                            Text("Aplicar")
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Text("Color de la insignia:", style = MaterialTheme.typography.labelMedium)
                                    Spacer(Modifier.height(6.dp))
                                    val badgeColors = listOf(
                                        android.graphics.Color.parseColor("#0D47A1"),
                                        android.graphics.Color.parseColor("#DC2626"),
                                        android.graphics.Color.parseColor("#059669"),
                                        android.graphics.Color.parseColor("#D97706"),
                                        android.graphics.Color.parseColor("#7C3AED"),
                                        android.graphics.Color.parseColor("#0F172A")
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        badgeColors.forEach { col ->
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(Color(col), CircleShape)
                                                    .border(
                                                        width = if (logoTextBadgeColor == col) 2.5.dp else 1.dp,
                                                        color = if (logoTextBadgeColor == col) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                        shape = CircleShape
                                                    )
                                                    .clickable { logoTextBadgeColor = col }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // TAB 2: COLORES (Código y Fondo)
                    ExpandableSection(title = "Diseño y Colores", icon = Icons.Default.Palette, initiallyExpanded = !isQrCode) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Sub-tabs (Primer plano, Fondo)
                            SecondaryTabRow(
                                selectedTabIndex = colorSubTab,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            ) {
                                Tab(
                                    selected = colorSubTab == 0,
                                    onClick = { colorSubTab = 0 },
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(14.dp).background(Color(selectedForegroundColor), CircleShape).border(1.dp, Color.Gray, CircleShape))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Color Código", fontWeight = if (colorSubTab == 0) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                )
                                Tab(
                                    selected = colorSubTab == 1,
                                    onClick = { colorSubTab = 1 },
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(14.dp).background(if (selectedBackgroundColor == android.graphics.Color.TRANSPARENT) Color.Transparent else Color(selectedBackgroundColor), CircleShape).border(1.dp, Color.Gray, CircleShape))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Color Fondo", fontWeight = if (colorSubTab == 1) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                )
                            }
                            Spacer(Modifier.height(14.dp))

                            val currentColorHex = if (colorSubTab == 0) {
                                String.format("#%06X", (0xFFFFFF and selectedForegroundColor))
                            } else {
                                if (selectedBackgroundColor == android.graphics.Color.TRANSPARENT) "TRANSPARENTE" else String.format("#%06X", (0xFFFFFF and selectedBackgroundColor))
                            }

                            // Current color display
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                if (colorSubTab == 1 && selectedBackgroundColor == android.graphics.Color.TRANSPARENT) Color.Transparent else Color(if (colorSubTab == 0) selectedForegroundColor else selectedBackgroundColor),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (colorSubTab == 0) "Color actual del código QR" else "Color actual del fondo",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = currentColorHex,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    FilledTonalButton(
                                        onClick = { 
                                            if (isPro) {
                                                showFullColorPickerDialog = true 
                                            } else {
                                                showPaywallDialog = true
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Filled.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Rueda & HEX", style = MaterialTheme.typography.labelSmall)
                                        if (!isPro) {
                                            Spacer(Modifier.width(4.dp))
                                            Icon(Icons.Filled.Lock, contentDescription = "PRO", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }

                            if (colorSubTab == 1) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { selectedBackgroundColor = android.graphics.Color.TRANSPARENT },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Fondo Transparente")
                                    }
                                    OutlinedButton(
                                        onClick = { selectedBackgroundColor = android.graphics.Color.WHITE },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Fondo Blanco")
                                    }
                                }
                            }

                            Text("Paleta de colores recomendados:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))

                            val palette = listOf(
                                android.graphics.Color.BLACK,
                                android.graphics.Color.parseColor("#0F172A"),
                                android.graphics.Color.parseColor("#1D4ED8"),
                                android.graphics.Color.parseColor("#0284C7"),
                                android.graphics.Color.parseColor("#06B6D4"),
                                android.graphics.Color.parseColor("#059669"),
                                android.graphics.Color.parseColor("#16A34A"),
                                android.graphics.Color.parseColor("#65A30D"),
                                android.graphics.Color.parseColor("#D97706"),
                                android.graphics.Color.parseColor("#EA580C"),
                                android.graphics.Color.parseColor("#DC2626"),
                                android.graphics.Color.parseColor("#E11D48"),
                                android.graphics.Color.parseColor("#DB2777"),
                                android.graphics.Color.parseColor("#7C3AED"),
                                android.graphics.Color.parseColor("#475569"),
                                android.graphics.Color.WHITE
                            )

                            val chunkedColors = palette.chunked(8)
                            chunkedColors.forEach { rowColors ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    rowColors.forEach { colorInt ->
                                        val isCurrent = if (colorSubTab == 0) selectedForegroundColor == colorInt else selectedBackgroundColor == colorInt
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(Color(colorInt), RoundedCornerShape(8.dp))
                                                .border(
                                                    width = if (isCurrent) 2.5.dp else 1.dp,
                                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    if (colorSubTab == 0) {
                                                        selectedForegroundColor = colorInt
                                                    } else {
                                                        selectedBackgroundColor = colorInt
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isCurrent) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = if (colorInt == android.graphics.Color.WHITE) Color.Black else Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // TAB 4: ESTILO (Forma de los módulos QR)
                    if (isQrCode) ExpandableSection(title = "Estilo de Módulos y ECC", icon = Icons.Default.Brush) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Selecciona el estilo geométrico de los módulos QR:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))

                            // Grid of visual shape cards
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val chunkedShapes = bodyShapes.chunked(3)
                                if (selectedBodyShape == com.example.utils.BodyShape.EMOJI) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = customEmojiText,
                                            onValueChange = { if (it.length <= 2) customEmojiText = it },
                                            label = { Text("Escribe tu Emoji o Símbolo") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Recomendados (Sólidos / Alta legibilidad):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        androidx.compose.foundation.lazy.LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val recommendedSymbols = listOf(
                                                "❤", "❣", "💕", "💖", "★", "✪", "◆", "■", "●", "▲", "▼", "⬢", "✚", "✖", "✿", "❁", "♣", "♠", "♦", "☁", "☂", "☘", "⚡"
                                            )
                                            items(recommendedSymbols.size) { index ->
                                                val sym = recommendedSymbols[index]
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    modifier = Modifier.clickable { customEmojiText = sym }
                                                ) {
                                                    Text(
                                                        text = sym,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                chunkedShapes.forEach { rowShapes ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowShapes.forEach { (shapeName, shapeEnum) ->
                                            val isSelected = selectedBodyShape == shapeEnum
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                    if (shapeEnum == com.example.utils.BodyShape.EMOJI && !isPro) {
                                                        showPaywallDialog = true
                                                    } else {
                                                        selectedBodyShape = shapeEnum
                                                    }
                                                },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                                ),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    if (shapeEnum == com.example.utils.BodyShape.EMOJI && !isPro) {
                                                        Icon(Icons.Filled.Lock, contentDescription = "PRO", modifier = Modifier.align(Alignment.End).size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                    ShapeMiniPreview(
                                                        shape = shapeEnum,
                                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(36.dp)
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    Text(
                                                        text = shapeName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                        repeat(3 - rowShapes.size) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(18.dp))
                            Text(
                                text = "Nivel de Corrección de Errores (ECC):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Determina la resistencia al daño o suciedad y la capacidad para integrar logotipos legibles.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))

                            val eccList = listOf(
                                Pair(ErrorCorrectionLevel.L, "L • 7% (Ligero)"),
                                Pair(ErrorCorrectionLevel.M, "M • 15% (Medio)"),
                                Pair(ErrorCorrectionLevel.Q, "Q • 25% (Alto)"),
                                Pair(ErrorCorrectionLevel.H, "H • 30% (Máximo / Recomendado)")
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                eccList.forEach { (level, title) ->
                                    val isSelected = selectedErrorCorrectionLevel == level
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedErrorCorrectionLevel = level }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedErrorCorrectionLevel = level }
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = when (level) {
                                                        ErrorCorrectionLevel.L -> "Ideal para códigos muy densos con mucho texto."
                                                        ErrorCorrectionLevel.M -> "Equilibrio estándar para la mayoría de usos."
                                                        ErrorCorrectionLevel.Q -> "Excelente tolerancia para cartelería e impresiones."
                                                        ErrorCorrectionLevel.H -> "Máxima redundancia. Ideal si incluyes logotipo en el centro."
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                        }
                    }
                        }
                    // TAB 3: TEXTO (Se mantiene independiente)
                    // TAB 3: TEXTO
                    if (isQrCode) ExpandableSection(title = "Etiquetas de Texto", icon = Icons.Default.TextFields) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (selectedTemplateDef == null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                            Spacer(Modifier.width(10.dp))
                                            Text("Los textos decorativos pertenecen a las Plantillas con marco.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Button(
                                            onClick = { activeCustomTab = 0 },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Filled.GridView, contentDescription = null)
                                            Spacer(Modifier.width(6.dp))
                                            Text("Elegir una Plantilla")
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                ) {
                                    Text(
                                        text = "Editando textos para plantilla: ${selectedTemplateDef?.name}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        
                            Text(
                                text = "Título Superior del Marco:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            androidx.compose.material3.OutlinedTextField(
                                value = customTitleText,
                                onValueChange = { customTitleText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(selectedTemplateDef?.defaultText ?: "Ej. ESCANÉAME") },
                                singleLine = true,
                                enabled = selectedTemplateDef != null,
                                trailingIcon = {
                                    if (customTitleText.isNotEmpty()) {
                                        IconButton(onClick = { customTitleText = "" }) {
                                            Icon(Icons.Filled.Close, contentDescription = "Limpiar")
                                        }
                                    }
                                }
                            )
                            Spacer(Modifier.height(6.dp))

                            // Quick title chips
                            val quickTitles = listOf("ESCANÉAME", "SÍGUENOS", "PAGA AQUÍ", "MENÚ DIGITAL", "CONÉCTATE", "OFERTA")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(quickTitles) { t ->
                                    SuggestionChip(
                                        onClick = { if (selectedTemplateDef != null) customTitleText = t },
                                        label = { Text(t, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            
                            Text(
                                text = "Subtítulo / Información Inferior:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            androidx.compose.material3.OutlinedTextField(
                                value = customSubText,
                                onValueChange = { customSubText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(selectedTemplateDef?.defaultSubtext ?: "Ej. Apunta con la cámara") },
                                singleLine = true,
                                enabled = selectedTemplateDef != null,
                                trailingIcon = {
                                    if (customSubText.isNotEmpty()) {
                                        IconButton(onClick = { customSubText = "" }) {
                                            Icon(Icons.Filled.Close, contentDescription = "Limpiar")
                                        }
                                    }
                                }
                            )
                            Spacer(Modifier.height(6.dp))

                            // Quick subtext chips
                            val quickSubtexts = listOf("Apunta con la cámara", "¡Gracias por tu visita!", "Escanea para pagar", "Todos los derechos reservados")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(quickSubtexts) { st ->
                                    SuggestionChip(
                                        onClick = { if (selectedTemplateDef != null) customSubText = st },
                                        label = { Text(st, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))

                            if (selectedTemplateDef != null) {
                                OutlinedButton(
                                    onClick = {
                                        customTitleText = selectedTemplateDef!!.defaultText
                                        customSubText = selectedTemplateDef!!.defaultSubtext
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Restablecer textos por defecto")
                                }
                            }
                        }
                    }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (type != "QR_ANIMATED") {
            Button(
                onClick = performSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (editingCode != null) "Guardar Cambios y Actualizar" else stringResource(R.string.generate_button),
                    fontWeight = FontWeight.Bold
                )
            }

            // Opciones de Descarga e Impresión: Botón para abrir BottomSheet
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
                                        val logoToUse = if (removeLogo) null else selectedLogoBitmap
                                        exportSvg(
                                            context = context,
                                            content = contentToEncode,
                                            foregroundColor = selectedForegroundColor,
                                            backgroundColor = selectedBackgroundColor,
                                            bodyShape = selectedBodyShape,
                                            ecc = selectedErrorCorrectionLevel,
                                            logoBitmap = logoToUse,
                                            logoSizeRatio = selectedLogoSizeRatio,
                                            logoBadgeShape = selectedLogoBadgeShape
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
            }
        
            if (showFullColorPickerDialog) {
                FullColorPickerModalDialog(
                    initialForegroundColor = selectedForegroundColor,
                    initialBackgroundColor = selectedBackgroundColor,
                    isInitialForeground = isPickingForeground,
                    onApplyColors = { fg, bg ->
                        selectedForegroundColor = fg
                        selectedBackgroundColor = bg
                    },
                    onDismiss = { showFullColorPickerDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

    // Success dialog shown immediately after generating
    if (showSuccessDialog && savedBitmapForDialog != null) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.success_generated_title))
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Tu código está listo y guardado en tu historial. Puedes descargarlo o imprimirlo directamente:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Image(
                        bitmap = savedBitmapForDialog!!.asImageBitmap(),
                        contentDescription = "Código Generado",
                        modifier = Modifier.size(150.dp).padding(4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                saveBitmapToGallery(context, savedBitmapForDialog!!, Bitmap.CompressFormat.PNG)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("PNG (HD)")
                        }
                        Button(
                            onClick = {
                                saveBitmapToGallery(context, savedBitmapForDialog!!, Bitmap.CompressFormat.JPEG)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("JPG")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val printTitle = customTitleText.ifBlank { typeTitle }
                                val printSub = customSubText.ifBlank { "Escanea con la cámara de tu móvil" }
                                PdfAndPrintHelper.printQrPoster(context, savedBitmapForDialog!!, printTitle, printSub)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Póster A4", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                val printTitle = customTitleText.ifBlank { typeTitle }
                                PdfAndPrintHelper.printStickerSheet(context, savedBitmapForDialog!!, printTitle)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("12 Pegatinas", maxLines = 1)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { shareBitmap(context, savedBitmapForDialog!!) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Compartir")
                        }
                        FilledTonalButton(
                            onClick = {
                                val contentToEncode = if (type == "QR_SECRET") {
                                    if (secretMessage.isNotBlank() && secretPin.isNotBlank()) SecretQrHelper.encrypt(secretMessage, secretPin) else content
                                } else {
                                    content
                                }
                                val logoToUse = if (removeLogo) null else selectedLogoBitmap
                                exportSvg(
                                    context = context,
                                    content = contentToEncode,
                                    foregroundColor = selectedForegroundColor,
                                    backgroundColor = selectedBackgroundColor,
                                    bodyShape = selectedBodyShape,
                                    ecc = selectedErrorCorrectionLevel,
                                    logoBitmap = logoToUse,
                                    logoSizeRatio = selectedLogoSizeRatio,
                                    logoBadgeShape = selectedLogoBadgeShape
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Vector SVG")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Modal for Quality & Scan diagnostics info
    if (showQualityInfoDialog) {
        val hasActiveLogo = !removeLogo && (selectedLogoBitmap != null || getSocialLogo(context, type) != null)
        val quality = CodeGenerator.getScanQuality(selectedForegroundColor, selectedBackgroundColor, hasActiveLogo, selectedErrorCorrectionLevel)
        AlertDialog(
            onDismissRequest = { showQualityInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (quality.level) {
                            ScanQualityLevel.EXCELLENT, ScanQualityLevel.GOOD -> Icons.Filled.CheckCircle
                            ScanQualityLevel.WARNING, ScanQualityLevel.POOR -> Icons.Filled.Warning
                        },
                        contentDescription = null,
                        tint = when (quality.level) {
                            ScanQualityLevel.EXCELLENT -> Color(0xFF059669)
                            ScanQualityLevel.GOOD -> Color(0xFF2563EB)
                            ScanQualityLevel.WARNING -> Color(0xFFD97706)
                            ScanQualityLevel.POOR -> Color(0xFFDC2626)
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Diagnóstico de Escaneo")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Estado: ${quality.label}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (quality.level) {
                            ScanQualityLevel.EXCELLENT -> Color(0xFF059669)
                            ScanQualityLevel.GOOD -> Color(0xFF2563EB)
                            ScanQualityLevel.WARNING -> Color(0xFFD97706)
                            ScanQualityLevel.POOR -> Color(0xFFDC2626)
                        }
                    )
                    Text(
                        text = "• Ratio de Contraste: ${String.format(java.util.Locale.US, "%.1f", quality.contrastRatio)}:1 (Mínimo recomendado: 4.0:1)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Corrección de Errores (ECC): Nivel ${selectedErrorCorrectionLevel.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Logotipo central: ${if (hasActiveLogo) "Activo con marco protector" else "Sin logotipo"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (quality.recommendation.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Consejo: ${quality.recommendation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityInfoDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    // Modal for Ultra HD export resolution
    if (showExportResolutionModal) {
        val activeBitmapForUltra = generatedBitmap ?: livePreviewBitmap
        AlertDialog(
            onDismissRequest = { showExportResolutionModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.HighQuality, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Exportar Ultra HD")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Selecciona la resolución para imprentas, gigantografías, rotulación y carteles:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val resolutions = listOf(
                        Pair("2K Ultra HD (2048 x 2048 px)", 2048),
                        Pair("4K Ultra HD (4096 x 4096 px)", 4096),
                        Pair("8K Print Master (8192 x 8192 px)", 8192)
                    )
                    resolutions.forEach { (resLabel, resPx) ->
                        OutlinedButton(
                            onClick = {
                                val contentToEncode = if (type == "QR_SECRET") {
                                    if (secretMessage.isNotBlank() && secretPin.isNotBlank()) SecretQrHelper.encrypt(secretMessage, secretPin) else content
                                } else {
                                    content
                                }
                                val format = when (type) {
                                    "EAN_8" -> BarcodeFormat.EAN_8
                                    "EAN_13" -> BarcodeFormat.EAN_13
                                    "UPC_E" -> BarcodeFormat.UPC_E
                                    "UPC_A" -> BarcodeFormat.UPC_A
                                    "CODE_39" -> BarcodeFormat.CODE_39
                                    "CODE_93" -> BarcodeFormat.CODE_93
                                    "CODE_128", "BARCODE_128" -> BarcodeFormat.CODE_128
                                    "ITF" -> BarcodeFormat.ITF
                                    "PDF_417" -> BarcodeFormat.PDF_417
                                    "CODABAR" -> BarcodeFormat.CODABAR
                                    "DATA_MATRIX" -> BarcodeFormat.DATA_MATRIX
                                    "AZTEC" -> BarcodeFormat.AZTEC
                                    else -> BarcodeFormat.QR_CODE
                                }
                                val logoToUse = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type))
                                saveUltraHighResToGallery(
                                    context = context,
                                    content = contentToEncode,
                                    format = format,
                                    foregroundColor = selectedForegroundColor,
                                    backgroundColor = selectedBackgroundColor,
                                    bodyShape = selectedBodyShape,
                                    logoBitmap = logoToUse,
                                    ecc = selectedErrorCorrectionLevel,
                                    logoSizeRatio = selectedLogoSizeRatio,
                                    logoBadgeShape = selectedLogoBadgeShape,
                                    targetDimension = resPx,
                                    customEmoji = customEmojiText
                                )
                                showExportResolutionModal = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(resLabel)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportResolutionModal = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, format: Bitmap.CompressFormat) {
    try {
        val filename = "QROFICIAL_${System.currentTimeMillis()}.${if (format == Bitmap.CompressFormat.JPEG) "jpg" else "png"}"
        val mimeType = if (format == Bitmap.CompressFormat.JPEG) "image/jpeg" else "image/png"
        
        var fos: java.io.OutputStream? = null
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }
            val imageUri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                fos = resolver.openOutputStream(imageUri)
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
                // Background needs to be white for JPEG as it doesn't support alpha
                val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(newBitmap)
                canvas.drawColor(android.graphics.Color.WHITE)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                newBitmap.compress(format, 100, it)
                newBitmap.recycle()
            } else {
                bitmap.compress(format, 100, it)
            }
            Toast.makeText(context, context.getString(R.string.toast_saved, filename), Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun shareBitmap(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "shared_images")
        if (!cachePath.exists()) {
            cachePath.mkdirs()
        }
        val file = File(cachePath, "shared_code.png")
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
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.btn_share)))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun exportPdf(context: Context, bitmap: Bitmap, typeTitle: String, content: String) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Page size A3/A4 representation (595 x 842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        
        // Main Title Header
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText(context.getString(R.string.pdf_watermark_1), 50f, 65f, paint)

        // Type info
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("${context.getString(R.string.header_code_type)}: $typeTitle", 50f, 105f, paint)
        
        // Wrapped content metadata print
        paint.textSize = 12f
        paint.color = android.graphics.Color.DKGRAY
        canvas.drawText("${context.getString(R.string.header_content)}:", 50f, 135f, paint)
        
        paint.color = android.graphics.Color.BLACK
        val lines = content.chunked(65)
        var currentY = 155f
        lines.take(6).forEach { line ->
            canvas.drawText(line, 55f, currentY, paint)
            currentY += 16f
        }

        // Horizontal visual alignment line
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawLine(50f, currentY + 10f, 545f, currentY + 10f, paint)
        
        // Draw the main bitmap cleanly centered
        paint.color = android.graphics.Color.BLACK
        val scaledWidth = 280
        val scaledHeight = 280
        val left = (595f - scaledWidth) / 2
        val top = currentY + 45f
        
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
        canvas.drawBitmap(scaledBitmap, left, top, paint)

        // Standard PDF watermark footer
        paint.color = android.graphics.Color.GRAY
        paint.textSize = 10f
        canvas.drawText(context.getString(R.string.pdf_watermark_2), 50f, 800f, paint)

        pdfDocument.finishPage(page)

        val cachePath = File(context.cacheDir, "shared_images")
        if (!cachePath.exists()) {
            cachePath.mkdirs()
        }
        val file = File(cachePath, "shared_code.pdf")
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
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.btn_pdf)))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun exportStickerSheetPdf(context: Context, bitmap: Bitmap, title: String, content: String) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595 x 842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Page Header
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Hoja de Etiquetas Imprimibles (12 Stickers) - QR OFICIAL", 40f, 45f, paint)

        paint.textSize = 10f
        paint.color = android.graphics.Color.DKGRAY
        paint.isFakeBoldText = false
        canvas.drawText(context.getString(R.string.pdf_instruction), 40f, 62f, paint)

        // Draw 3 columns x 4 rows = 12 stickers
        val cols = 3
        val rows = 4
        val marginX = 40f
        val marginY = 80f
        val stickerWidth = 160f
        val stickerHeight = 175f
        val gapX = 17f
        val gapY = 15f

        val qrSize = 105
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, qrSize, qrSize, true)

        val borderPaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = marginX + c * (stickerWidth + gapX)
                val top = marginY + r * (stickerHeight + gapY)
                val right = left + stickerWidth
                val bottom = top + stickerHeight

                // Draw sticker card border
                canvas.drawRect(left, top, right, bottom, borderPaint)

                // Sticker Title
                paint.color = android.graphics.Color.BLACK
                paint.textSize = 10f
                paint.isFakeBoldText = true
                val titleToDraw = if (title.length > 22) title.take(20) + "..." else title
                canvas.drawText(titleToDraw, left + 10f, top + 20f, paint)

                // QR Code Bitmap centered
                val qrLeft = left + (stickerWidth - qrSize) / 2f
                val qrTop = top + 28f
                canvas.drawBitmap(scaledBitmap, qrLeft, qrTop, paint)

                // Footer / Brand
                paint.color = android.graphics.Color.GRAY
                paint.textSize = 7.5f
                paint.isFakeBoldText = false
                canvas.drawText(context.getString(R.string.pdf_scan_me), left + 28f, bottom - 10f, paint)
            }
        }

        pdfDocument.finishPage(page)

        val cachePath = File(context.cacheDir, "shared_images")
        if (!cachePath.exists()) {
            cachePath.mkdirs()
        }
        val file = File(cachePath, "hoja_stickers_12.pdf")
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
            context.startActivity(Intent.createChooser(shareIntent, "Imprimir o Compartir Hoja de Stickers"))
            com.example.utils.ReviewManager.askForReview(context)
        }
        Toast.makeText(context, context.getString(R.string.toast_pdf_success), Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al generar hoja de stickers: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun exportSvg(
    context: Context,
    content: String,
    foregroundColor: Int,
    backgroundColor: Int,
    bodyShape: BodyShape,
    ecc: ErrorCorrectionLevel,
    logoBitmap: Bitmap? = null,
    logoSizeRatio: Float = 0.22f,
    logoBadgeShape: LogoBadgeShape = LogoBadgeShape.ROUNDED_SQUARE
) {
    try {
        val svgString = CodeGenerator.generateSvg(
            content = content,
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor,
            bodyShape = bodyShape,
            ecc = ecc,
            logoBitmap = logoBitmap,
            logoSizeRatio = logoSizeRatio,
            logoBadgeShape = logoBadgeShape
        )
        val filename = "QROFICIAL_${System.currentTimeMillis()}.svg"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/svg+xml")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { os ->
                    os.write(svgString.toByteArray(Charsets.UTF_8))
                }
            }
        } else {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = File(downloadsDir, filename)
            file.writeText(svgString, Charsets.UTF_8)
        }

        // Also offer to share
        val cachePath = File(context.cacheDir, "shared_images")
        if (!cachePath.exists()) cachePath.mkdirs()
        val shareFile = File(cachePath, filename)
        shareFile.writeText(svgString, Charsets.UTF_8)
        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "com.aistudio.omnicode.zvyf.fileprovider",
            shareFile
        )
        if (contentUri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, "image/svg+xml")
                putExtra(Intent.EXTRA_STREAM, contentUri)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Vector SVG Guardado - Compartir"))
        }

        Toast.makeText(context, "Vector SVG guardado en Descargas: $filename", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error exportando SVG: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun saveUltraHighResToGallery(
    context: Context,
    content: String,
    format: BarcodeFormat,
    foregroundColor: Int,
    backgroundColor: Int,
    bodyShape: BodyShape,
    logoBitmap: Bitmap?,
    ecc: ErrorCorrectionLevel,
    logoSizeRatio: Float,
    logoBadgeShape: LogoBadgeShape,
    targetDimension: Int,
    customEmoji: String = "😀"
) {
    try {
        val ultraBitmap = CodeGenerator.generateBitmapOrThrow(
            content = content,
            format = format,
            width = targetDimension,
            height = targetDimension,
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor,
            bodyShape = bodyShape,
            logoBitmap = logoBitmap,
            errorCorrectionLevel = ecc,
            logoSizeRatio = logoSizeRatio,
            logoBadgeShape = logoBadgeShape,
            customEmoji = customEmoji
        )

        saveBitmapToGallery(context, ultraBitmap, Bitmap.CompressFormat.PNG)
        Toast.makeText(context, "¡Ultra HD (${targetDimension}x${targetDimension}px) generado con éxito!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error generando Ultra HD: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ColorWheelPicker(
    modifier: Modifier = Modifier,
    onColorSelected: (Int) -> Unit
) {
    var touchPosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f
                    val dx = change.position.x - center.x
                    val dy = change.position.y - center.y
                    val distance = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                    val angle = (kotlin.math.atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI).toFloat()
                    val hue = if (angle < 0) angle + 360f else angle
                    val saturation = (distance / radius).coerceIn(0f, 1f)
                    val color = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, 1f))
                    onColorSelected(color)
                    touchPosition = change.position
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    val distance = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                    val angle = (kotlin.math.atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI).toFloat()
                    val hue = if (angle < 0) angle + 360f else angle
                    val saturation = (distance / radius).coerceIn(0f, 1f)
                    val color = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, 1f))
                    onColorSelected(color)
                    touchPosition = offset
                }
            }
    ) {
        val radius = size.minDimension / 2f
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        
        val colors = intArrayOf(
            android.graphics.Color.RED,
            android.graphics.Color.YELLOW,
            android.graphics.Color.GREEN,
            android.graphics.Color.CYAN,
            android.graphics.Color.BLUE,
            android.graphics.Color.MAGENTA,
            android.graphics.Color.RED
        )
        val composeColors = colors.map { androidx.compose.ui.graphics.Color(it) }
        
        drawCircle(
            brush = androidx.compose.ui.graphics.Brush.sweepGradient(composeColors, center),
            radius = radius,
            center = center
        )
        
        drawCircle(
            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                colors = listOf(Color.White, Color.Transparent),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
        
        if (touchPosition != androidx.compose.ui.geometry.Offset.Zero) {
            val dist = kotlin.math.hypot((touchPosition.x - center.x).toDouble(), (touchPosition.y - center.y).toDouble())
            val drawPos = if (dist > radius) {
                val angle = kotlin.math.atan2((touchPosition.y - center.y).toDouble(), (touchPosition.x - center.x).toDouble())
                androidx.compose.ui.geometry.Offset(
                    center.x + (radius * kotlin.math.cos(angle)).toFloat(),
                    center.y + (radius * kotlin.math.sin(angle)).toFloat()
                )
            } else {
                touchPosition
            }
            
            drawCircle(
                color = Color.Black,
                radius = 12.dp.toPx(),
                center = drawPos,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = drawPos,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}
fun getSocialLogo(context: android.content.Context, type: String): android.graphics.Bitmap? {
    val resId = when (type) {
        "QR_FACEBOOK" -> com.example.R.drawable.ic_facebook
        "QR_INSTAGRAM" -> com.example.R.drawable.ic_instagram
        "QR_TIKTOK" -> com.example.R.drawable.ic_tiktok
        "QR_WHATSAPP" -> com.example.R.drawable.ic_whatsapp
        "QR_YOUTUBE" -> com.example.R.drawable.ic_youtube
        "QR_TELEGRAM" -> com.example.R.drawable.ic_telegram
        "QR_TWITTER" -> com.example.R.drawable.ic_twitter
        "QR_MESSENGER" -> com.example.R.drawable.ic_messenger
        "QR_LINKEDIN" -> com.example.R.drawable.ic_linkedin
        else -> null
    }
    return resId?.let { id ->
        androidx.core.content.ContextCompat.getDrawable(context, id)?.let { drawable ->
            val w = 256
            val h = 256
            val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, w, h)
            drawable.draw(canvas)
            bitmap
        }
    }
}

@Composable
fun ShapeMiniPreview(
    shape: com.example.utils.BodyShape,
    tint: Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val modSize = w * 0.38f
        val gap = w * 0.12f
        
        val offsets = listOf(
            androidx.compose.ui.geometry.Offset(0f, 0f),
            androidx.compose.ui.geometry.Offset(modSize + gap, 0f),
            androidx.compose.ui.geometry.Offset(0f, modSize + gap),
            androidx.compose.ui.geometry.Offset(modSize + gap, modSize + gap)
        )
        
        for (pos in offsets) {
            val cx = pos.x + modSize / 2f
            val cy = pos.y + modSize / 2f
            val r = modSize / 2f
            
            when (shape) {
                com.example.utils.BodyShape.SQUARE -> {
                    drawRect(color = tint, topLeft = pos, size = androidx.compose.ui.geometry.Size(modSize, modSize))
                }
                com.example.utils.BodyShape.ROUNDED -> {
                    drawRoundRect(
                        color = tint,
                        topLeft = pos,
                        size = androidx.compose.ui.geometry.Size(modSize, modSize),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(modSize * 0.32f, modSize * 0.32f)
                    )
                }
                com.example.utils.BodyShape.CIRCLE, com.example.utils.BodyShape.DOT -> {
                    drawCircle(
                        color = tint,
                        radius = if (shape == com.example.utils.BodyShape.DOT) r * 0.75f else r,
                        center = androidx.compose.ui.geometry.Offset(cx, cy)
                    )
                }
                com.example.utils.BodyShape.DIAMOND -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx, pos.y)
                        lineTo(pos.x + modSize, cy)
                        lineTo(cx, pos.y + modSize)
                        lineTo(pos.x, cy)
                        close()
                    }
                    drawPath(path, color = tint)
                }
                com.example.utils.BodyShape.CROSS -> {
                    val bar = modSize * 0.36f
                    drawRect(
                        color = tint,
                        topLeft = androidx.compose.ui.geometry.Offset(cx - bar / 2f, pos.y),
                        size = androidx.compose.ui.geometry.Size(bar, modSize)
                    )
                    drawRect(
                        color = tint,
                        topLeft = androidx.compose.ui.geometry.Offset(pos.x, cy - bar / 2f),
                        size = androidx.compose.ui.geometry.Size(modSize, bar)
                    )
                }
                com.example.utils.BodyShape.STAR -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        val numPoints = 5
                        val outerR = r
                        val innerR = r * 0.45f
                        for (i in 0 until numPoints * 2) {
                            val curR = if (i % 2 == 0) outerR else innerR
                            val angle = (i * Math.PI / numPoints) - (Math.PI / 2.0)
                            val px = cx + (curR * kotlin.math.cos(angle)).toFloat()
                            val py = cy + (curR * kotlin.math.sin(angle)).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(path, color = tint)
                }
                com.example.utils.BodyShape.HEXAGON -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        for (i in 0 until 6) {
                            val angle = (i * Math.PI / 3.0) - (Math.PI / 6.0)
                            val px = cx + (r * kotlin.math.cos(angle)).toFloat()
                            val py = cy + (r * kotlin.math.sin(angle)).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(path, color = tint)
                }
                com.example.utils.BodyShape.HEART -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        val mw = modSize
                        val mh = modSize
                        moveTo(cx, pos.y + mh * 0.9f)
                        cubicTo(pos.x - mw * 0.15f, pos.y + mh * 0.3f, cx - mw * 0.15f, pos.y - mh * 0.25f, cx, pos.y + mh * 0.2f)
                        cubicTo(cx + mw * 0.15f, pos.y - mh * 0.25f, pos.x + mw * 1.15f, pos.y + mh * 0.3f, cx, pos.y + mh * 0.9f)
                        close()
                    }
                    drawPath(path, color = tint)
                }
                com.example.utils.BodyShape.TRIANGLE -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx, pos.y)
                        lineTo(pos.x + modSize, pos.y + modSize)
                        lineTo(pos.x, pos.y + modSize)
                        close()
                    }
                    drawPath(path, color = tint)
                }
                com.example.utils.BodyShape.PENTAGON -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        for (i in 0 until 5) {
                            val angle = (i * 2.0 * Math.PI / 5.0) - (Math.PI / 2.0)
                            val px = cx + (r * kotlin.math.cos(angle)).toFloat()
                            val py = cy + (r * kotlin.math.sin(angle)).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(path, color = tint)
                }
                com.example.utils.BodyShape.OCTAGON -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        for (i in 0 until 8) {
                            val angle = (i * 2.0 * Math.PI / 8.0) - (Math.PI / 8.0)
                            val px = cx + (r * kotlin.math.cos(angle)).toFloat()
                            val py = cy + (r * kotlin.math.sin(angle)).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(path, color = tint)
                }
                com.example.utils.BodyShape.EMOJI -> {
                    val textPaint = android.graphics.Paint().apply {
                        color = tint.toArgb()
                        textSize = modSize * 1.1f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val textBounds = android.graphics.Rect()
                    textPaint.getTextBounds("😀", 0, 1, textBounds)
                    drawContext.canvas.nativeCanvas.drawText("😀", cx, cy - textBounds.exactCenterY(), textPaint)
                }
                else -> {}
            }
        }
    }
}

private fun compositeWithCachedTemplate(templateBitmap: android.graphics.Bitmap, qrBitmap: android.graphics.Bitmap): android.graphics.Bitmap {
    val finalTemplate = android.graphics.Bitmap.createBitmap(templateBitmap.width, templateBitmap.height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(finalTemplate)
    canvas.drawBitmap(templateBitmap, 0f, 0f, null)
    val targetQrSize = (finalTemplate.width * 0.46f).toInt()
    val scaledQr = android.graphics.Bitmap.createScaledBitmap(qrBitmap, targetQrSize, targetQrSize, true)
    
    val left = (finalTemplate.width - targetQrSize) / 2f
    val top = (finalTemplate.height - targetQrSize) / 2f
    
    canvas.drawBitmap(scaledQr, left, top, null)
    
    return finalTemplate
}

@Composable
fun FullColorPickerModalDialog(
    initialForegroundColor: Int,
    initialBackgroundColor: Int,
    isInitialForeground: Boolean,
    onApplyColors: (foregroundColor: Int, backgroundColor: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var isPickingForeground by remember { mutableStateOf(isInitialForeground) }
    var currentFg by remember { mutableIntStateOf(initialForegroundColor) }
    var currentBg by remember { mutableIntStateOf(initialBackgroundColor) }

    val activeColor = if (isPickingForeground) currentFg else currentBg
    val isTransparent = activeColor == android.graphics.Color.TRANSPARENT

    val hexFormatted = if (isTransparent) "#00000000" else String.format("#%06X", (0xFFFFFF and activeColor))
    var hexInputText by remember(activeColor) { mutableStateOf(hexFormatted) }
    var isHexValid by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    val presetPalette = remember {
        listOf(
            android.graphics.Color.BLACK,
            android.graphics.Color.WHITE,
            android.graphics.Color.parseColor("#0D47A1"), // Deep Blue
            android.graphics.Color.parseColor("#1B5E20"), // Deep Green
            android.graphics.Color.parseColor("#B71C1C"), // Deep Red
            android.graphics.Color.parseColor("#E65100"), // Deep Orange
            android.graphics.Color.parseColor("#4A148C"), // Deep Purple
            android.graphics.Color.parseColor("#00838F"), // Teal
            android.graphics.Color.parseColor("#FFD600"), // Yellow Gold
            android.graphics.Color.parseColor("#C2185B"), // Magenta
            android.graphics.Color.parseColor("#00E5FF"), // Cyan
            android.graphics.Color.parseColor("#3E2723"), // Dark Brown
            android.graphics.Color.parseColor("#607D8B"), // Slate Grey
            android.graphics.Color.parseColor("#212121")  // Graphite
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.ColorLens,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Rueda Cromática y HEX",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Target Selector (Code vs Background)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = isPickingForeground,
                        onClick = { isPickingForeground = true },
                        label = { Text("Color Código", fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(androidx.compose.ui.graphics.Color(currentFg), CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isPickingForeground,
                        onClick = { isPickingForeground = false },
                        label = { Text("Color Fondo", fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            val isBgTrans = currentBg == android.graphics.Color.TRANSPARENT
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        if (isBgTrans) Color.Transparent else androidx.compose.ui.graphics.Color(currentBg),
                                        CircleShape
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isBgTrans) Text("🔲", style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Color Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    if (isTransparent) Color.Transparent else androidx.compose.ui.graphics.Color(activeColor),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isTransparent) {
                                Text("🔲", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isPickingForeground) "Color de Módulos (Código)" else "Color de Lienzo (Fondo)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = hexFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!isTransparent) {
                                val r = android.graphics.Color.red(activeColor)
                                val g = android.graphics.Color.green(activeColor)
                                val b = android.graphics.Color.blue(activeColor)
                                Text(
                                    text = "RGB: $r, $g, $b",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(hexFormatted))
                                Toast.makeText(context, "Copiado $hexFormatted", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                Icons.Filled.ContentCopy,
                                contentDescription = "Copiar HEX",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Custom HEX Text Input
                OutlinedTextField(
                    value = hexInputText,
                    onValueChange = { input ->
                        var formatted = input.trim()
                        if (!formatted.startsWith("#")) {
                            formatted = "#$formatted"
                        }
                        hexInputText = formatted.uppercase()
                        try {
                            if (formatted.length == 7 || formatted.length == 9) {
                                val parsed = android.graphics.Color.parseColor(formatted)
                                if (isPickingForeground) {
                                    currentFg = parsed
                                } else {
                                    currentBg = parsed
                                }
                                isHexValid = true
                            } else {
                                isHexValid = formatted.length <= 9
                            }
                        } catch (e: Exception) {
                            isHexValid = false
                        }
                    },
                    label = { Text("Escribir código de color HEX") },
                    placeholder = { Text("#000000 o #FFFFFF") },
                    isError = !isHexValid,
                    supportingText = {
                        if (!isHexValid) {
                            Text("Formato inválido. Ejemplo: #FF5722 o #1A237E", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Ingresa cualquier color hexadecimal (#RRGGBB)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    val safeClip = if (clip.startsWith("#")) clip else "#$clip"
                                    try {
                                        val parsed = android.graphics.Color.parseColor(safeClip)
                                        hexInputText = safeClip.uppercase()
                                        if (isPickingForeground) currentFg = parsed else currentBg = parsed
                                        isHexValid = true
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Texto del portapapeles no es un color HEX válido", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.ContentPaste, contentDescription = "Pegar HEX")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                // 360° Round Color Wheel
                Text(
                    text = "Gira la rueda cromática de 360°:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ColorWheelPicker(
                        modifier = Modifier.size(220.dp),
                        onColorSelected = { color ->
                            if (isPickingForeground) {
                                currentFg = color
                            } else {
                                currentBg = color
                            }
                        }
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Quick Palette Swatches in Dialog
                Text(
                    text = "Paleta Rápida:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetPalette) { color ->
                        val isSelected = activeColor == color
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(androidx.compose.ui.graphics.Color(color), CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    if (isPickingForeground) {
                                        currentFg = color
                                    } else {
                                        currentBg = color
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                val isDark = (android.graphics.Color.red(color) * 0.299 + android.graphics.Color.green(color) * 0.587 + android.graphics.Color.blue(color) * 0.114) < 160
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = if (isDark) Color.White else Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (!isPickingForeground) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { currentBg = android.graphics.Color.TRANSPARENT },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔲 Fondo Transparente (Sin blanco)")
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Bottom Dialog Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            currentFg = android.graphics.Color.BLACK
                            currentBg = android.graphics.Color.WHITE
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Restablecer")
                    }

                    Button(
                        onClick = {
                            onApplyColors(currentFg, currentBg)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Aplicar Colores")
                    }
                }
            }
        }
    }
}
@Composable
fun ExpandableSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        androidx.compose.animation.AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun PaywallDialog(
    onDismiss: () -> Unit,
    onPurchaseLifetime: () -> Unit,
    onPurchaseMonthly: () -> Unit,
    onPurchaseYearly: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFFFFFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Close Button & Pro Badge Header (Solid background & high contrast)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = androidx.compose.ui.graphics.Color(0xFFFEF3C7),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.ui.graphics.Color(0xFFF59E0B))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color(0xFFB45309),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "ACCESO ILIMITADO PRO",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = androidx.compose.ui.graphics.Color(0xFF78350F)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = androidx.compose.ui.graphics.Color(0xFF1E293B)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Hero Headline (Solid, bold, legible)
                Text(
                    text = "Lleva tus Códigos QR al Nivel Profesional",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = androidx.compose.ui.graphics.Color(0xFF0F172A)
                )
                
                Spacer(Modifier.height(6.dp))
                
                Text(
                    text = "Desbloquea todas las funciones avanzadas para tu negocio o marca.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = androidx.compose.ui.graphics.Color(0xFF334155)
                )

                Spacer(Modifier.height(16.dp))

                // Pro Features list with checks (Solid light slate container & solid deep text)
                val proFeatures = listOf(
                    "🖼️ Marcos y fondos PNG personalizados (Sube tus propios artes)",
                    "📐 Vector SVG híbrido con logos incrustados en ultra alta calidad",
                    "🏷️ Plantillas de diseño exclusivas para hostelería, pago y marcas",
                    "⚡ Transferencia Óptica Ultra por pantalla a máxima velocidad",
                    "🚫 Cero anuncios y procesamiento 100% prioritario"
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = androidx.compose.ui.graphics.Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        proFeatures.forEach { feature ->
                            Row(
                                modifier = Modifier.padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color(0xFF059669),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = feature,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = androidx.compose.ui.graphics.Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- AGGRESSIVE PROMO CARD: 7 DAYS FREE TRIAL ($0 TODAY) ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPurchaseMonthly() },
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, androidx.compose.ui.graphics.Color(0xFF1D4ED8)),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFFDBEAFE)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = androidx.compose.ui.graphics.Color(0xFF1D4ED8)
                            ) {
                                Text(
                                    text = "🔥 OFERTA DE LANZAMIENTO",
                                    color = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "$0.00 HOY",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleLarge,
                                color = androidx.compose.ui.graphics.Color(0xFF1E3A8A)
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "7 Días de Prueba Gratis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = androidx.compose.ui.graphics.Color(0xFF0F172A)
                        )

                        Text(
                            text = "Disfruta de todo sin pagar nada por 1 semana. Luego solo $1.00/mes. Cancela cuando quieras en Google Play.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = androidx.compose.ui.graphics.Color(0xFF1E3A8A)
                        )

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = onPurchaseMonthly,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color(0xFF1D4ED8),
                                contentColor = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
                            )
                        ) {
                            Text(
                                text = "Comenzar 7 Días Gratis",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Secondary Plans: Annual & Lifetime (Solid white background & solid high-contrast borders)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Annual
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPurchaseYearly() },
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.ui.graphics.Color(0xFF94A3B8)),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Plan Anual", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFF0F172A))
                            Spacer(Modifier.height(4.dp))
                            Text("$10.00 / año", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = androidx.compose.ui.graphics.Color(0xFF1D4ED8))
                            Spacer(Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = androidx.compose.ui.graphics.Color(0xFFD1FAE5)
                            ) {
                                Text("Ahorra 17%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFF065F46), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    // Lifetime
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPurchaseLifetime() },
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.ui.graphics.Color(0xFF94A3B8)),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("De por Vida", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFF0F172A))
                            Spacer(Modifier.height(4.dp))
                            Text("$19.99", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = androidx.compose.ui.graphics.Color(0xFF7C3AED))
                            Spacer(Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = androidx.compose.ui.graphics.Color(0xFFEDE9FE)
                            ) {
                                Text("Pago único", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFF5B21B6), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Footer Guarantee / Disclaimer (Solid color, sharp and readable)
                Text(
                    text = "🔒 Pago 100% seguro gestionado por Google Play Store.\nPuedes cancelar la prueba gratuita en cualquier momento desde Google Play.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = androidx.compose.ui.graphics.Color(0xFF475569)
                )
            }
        }
    }
}
