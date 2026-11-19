package com.example.ui.screens

import android.content.Context
import android.content.ContextWrapper
import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.example.BuildConfig
import com.example.R
import com.example.ui.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@Composable
fun SettingsScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SettingsGroup(stringResource(R.string.settings_personalization)) {
            SettingsMenuItem(Icons.Outlined.ColorLens, stringResource(R.string.set_theme), { onNavigate("settings/theme") })
            SettingsMenuItem(Icons.Outlined.Language, stringResource(R.string.set_language), { onNavigate("settings/language") })
        }
        SettingsGroup(stringResource(R.string.settings_tools)) {
            SettingsMenuItem(Icons.Outlined.QrCodeScanner, stringResource(R.string.set_category_scanning), { onNavigate("settings/scan") })
            SettingsMenuItem(Icons.Outlined.AutoAwesome, stringResource(R.string.set_category_auto_actions), { onNavigate("settings/auto") })
            SettingsMenuItem(Icons.Outlined.Security, stringResource(R.string.set_permissions), { onNavigate("settings/permissions") })
            SettingsMenuItem(Icons.Outlined.AutoAwesome, stringResource(R.string.home_transfer_file), { onNavigate("ultra_transfer") })
        }
        SettingsGroup(stringResource(R.string.settings_app)) {
            SettingsMenuItem(Icons.Outlined.Delete, stringResource(R.string.set_clear_data), { onNavigate("settings/data") })
            SettingsMenuItem(Icons.Outlined.Info, stringResource(R.string.set_about), { onNavigate("settings/about") })
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(10.dp).size(22.dp))
        }
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val themeMode by viewModel.themeState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.set_theme)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(16.dp)) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Row(
                    Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.QrCodeScanner, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                    Column {
                        Text(stringResource(R.string.theme_preview), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            ThemeSelectionRow(title = stringResource(R.string.set_theme_modern), selected = themeMode == "modern", onClick = { viewModel.setTheme("modern") })
            ThemeSelectionRow(title = stringResource(R.string.set_theme_system), selected = themeMode == "system", onClick = { viewModel.setTheme("system") })
            ThemeSelectionRow(title = stringResource(R.string.set_theme_light), selected = themeMode == "light", onClick = { viewModel.setTheme("light") })
            ThemeSelectionRow(title = stringResource(R.string.set_theme_dark), selected = themeMode == "dark", onClick = { viewModel.setTheme("dark") })
            ThemeSelectionRow(title = stringResource(R.string.set_theme_pink), selected = themeMode == "pink", onClick = { viewModel.setTheme("pink") })
            ThemeSelectionRow(title = stringResource(R.string.set_theme_liquid_glass), selected = themeMode == "liquid_glass", onClick = { viewModel.setTheme("liquid_glass") })
            ThemeSelectionRow(title = stringResource(R.string.set_theme_liquid_glass_dark), selected = themeMode == "liquid_glass_dark", onClick = { viewModel.setTheme("liquid_glass_dark") })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.set_language)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val currentLanguageTag = if (!currentLocales.isEmpty) currentLocales.get(0)?.language ?: "es" else "es"
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LanguageButton(title = "ES", selected = currentLanguageTag == "es", onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es")) })
                LanguageButton(title = "EN", selected = currentLanguageTag == "en", onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en")) })
                LanguageButton(title = "PT", selected = currentLanguageTag == "pt", onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("pt")) })
                LanguageButton(title = "FR", selected = currentLanguageTag == "fr", onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("fr")) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.set_clear_data)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text(
                text = stringResource(R.string.set_clear_data_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(stringResource(R.string.btn_delete_all))
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.dialog_title_clear_history)) },
            text = { Text(stringResource(R.string.dialog_desc_clear_history)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllCodes()
                        showConfirmDialog = false
                        Toast.makeText(context, context.getString(R.string.toast_history_cleared), Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_confirm_wipe))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanSettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val scanBatchMode by viewModel.scanBatchMode.collectAsState()
    val scanKeepScreenOn by viewModel.scanKeepScreenOn.collectAsState()
    val scanSound by viewModel.scanSound.collectAsState()
    val scanVibration by viewModel.scanVibration.collectAsState()
    val soundPreset by viewModel.soundPreset.collectAsState()
    val searchEngine by viewModel.searchEngine.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.set_category_scanning)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState())) {
            SettingsToggleRow(title = stringResource(R.string.set_scan_batch), description = stringResource(R.string.set_scan_batch_desc), checked = scanBatchMode, onCheckedChange = { viewModel.setScanBatchMode(it) })
            SettingsToggleRow(title = stringResource(R.string.set_scan_keep_screen_on), description = stringResource(R.string.set_scan_keep_screen_on_desc), checked = scanKeepScreenOn, onCheckedChange = { viewModel.setScanKeepScreenOn(it) })
            SettingsToggleRow(title = stringResource(R.string.set_scan_sound), description = stringResource(R.string.set_scan_sound_desc), checked = scanSound, onCheckedChange = { viewModel.setScanSound(it) })
            SettingsToggleRow(title = stringResource(R.string.set_scan_vibration), description = stringResource(R.string.set_scan_vibration_desc), checked = scanVibration, onCheckedChange = { viewModel.setScanVibration(it) })

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.set_scan_sound_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            val soundPresets = listOf("chaching" to stringResource(R.string.sound_cash_register), "laser" to stringResource(R.string.sound_laser), "bell" to stringResource(R.string.sound_bell), "gameboy" to stringResource(R.string.sound_gameboy), "haptic_only" to stringResource(R.string.sound_haptic))
            soundPresets.forEach { (key, label) ->
                ThemeSelectionRow(title = label, selected = soundPreset == key, onClick = { viewModel.setSoundPreset(key); viewModel.playScanFeedback() })
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.set_scan_search_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            val engines = listOf("Google" to stringResource(R.string.search_google), "OpenFoodFacts" to stringResource(R.string.search_openfoodfacts), "Amazon" to stringResource(R.string.search_amazon), "BarcodeLookup" to stringResource(R.string.search_barcode))
            engines.forEach { (key, label) ->
                ThemeSelectionRow(title = label, selected = searchEngine == key, onClick = { viewModel.setSearchEngine(key) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoActionsSettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val autoCopyClipboard by viewModel.autoCopyClipboard.collectAsState()
    val autoOpenLinks by viewModel.autoOpenLinks.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.set_category_auto_actions)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState())) {
            SettingsToggleRow(title = stringResource(R.string.set_auto_copy_clipboard), description = stringResource(R.string.set_auto_copy_clipboard_desc), checked = autoCopyClipboard, onCheckedChange = { viewModel.setAutoCopyClipboard(it) })
            SettingsToggleRow(title = stringResource(R.string.set_auto_open_links), description = stringResource(R.string.set_auto_open_links_desc), checked = autoOpenLinks, onCheckedChange = { viewModel.setAutoOpenLinks(it) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PermissionsSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.set_permissions)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.set_perm_camera), style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (cameraPermissionState.status.isGranted) stringResource(R.string.set_perm_granted) else stringResource(R.string.set_perm_denied),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (cameraPermissionState.status.isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "No se pudo abrir la configuración de la aplicación", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.set_perm_manage_btn))
            }
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.set_about)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.set_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = stringResource(R.string.set_developer), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Button(
                onClick = {
                    val activity = context.findActivity()
                    if (activity != null) {
                        val reviewManager = ReviewManagerFactory.create(context)
                        val request = reviewManager.requestReviewFlow()
                        request.addOnCompleteListener { task ->
                            if (task.isSuccessful && !activity.isFinishing && !activity.isDestroyed) {
                                val reviewInfo = task.result
                                reviewManager.launchReviewFlow(activity, reviewInfo)
                            } else {
                                // Fallback a Intent de Play Store si la API falla o no está disponible
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("market://details?id=${context.packageName}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                    }
                                    try {
                                        context.startActivity(webIntent)
                                    } catch (e: Exception) {}
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.set_rate_us))
            }
        }
    }
}

@Composable
fun LanguageButton(title: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Text(title, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun SettingsToggleRow(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ThemeSelectionRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            Modifier.selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null)
            Text(
                title, modifier = Modifier.weight(1f).padding(start = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
