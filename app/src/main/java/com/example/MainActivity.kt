package com.example

import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.CodeRepository
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.screens.CreateScreen
import com.example.ui.screens.GenerateScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TemplatesScreen
import androidx.compose.material.icons.filled.Style
import com.example.ui.theme.MyApplicationTheme

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        try {
            val crashPrefs = getSharedPreferences("crash_prefs", android.content.Context.MODE_PRIVATE)
            var cause = throwable.cause
            var fullLog = throwable.stackTraceToString()
            while (cause != null) {
                fullLog += "\nCaused by: " + cause.stackTraceToString()
                cause = cause.cause
            }
            crashPrefs.edit().putString("last_crash", fullLog).apply()
        } catch (_: Exception) {
        }
        defaultHandler?.uncaughtException(thread, throwable)
    }

    setContent {
      val database = remember { AppDatabase.getDatabase(this@MainActivity) }
      val repository = remember { CodeRepository(database.codeDao()) }
      val prefs = remember { getSharedPreferences("settings_prefs", android.content.Context.MODE_PRIVATE) }
      val factory = remember { MainViewModelFactory(this@MainActivity.applicationContext, repository, prefs) }
      val viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
      
      val themeMode by viewModel.themeState.collectAsState()
      val useDarkTheme = when (themeMode) {
        "light", "pink", "liquid_glass" -> false
        "dark", "liquid_glass_dark" -> true
        else -> androidx.compose.foundation.isSystemInDarkTheme()
      }
      
      MyApplicationTheme(themeMode = themeMode, darkTheme = useDarkTheme) {
        MainApp(viewModel, themeMode)
      }
    }
  }
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun MainApp(viewModel: MainViewModel, themeMode: String) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "main_tabs"
    
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    
    val isMainTabs = currentRoute == "main_tabs"
    val isLiquidGlass = themeMode == "liquid_glass" || themeMode == "liquid_glass_dark"

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize().then(
            when (themeMode) {
                "liquid_glass" -> Modifier.background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color(0xFF64B5F6),
                            androidx.compose.ui.graphics.Color(0xFF0097A7),
                            androidx.compose.ui.graphics.Color(0xFF00796B)
                        )
                    )
                )
                "liquid_glass_dark" -> Modifier.background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color(0xFF0A192F),
                            androidx.compose.ui.graphics.Color(0xFF0D2538),
                            androidx.compose.ui.graphics.Color(0xFF0E3A52),
                            androidx.compose.ui.graphics.Color(0xFF082032)
                        )
                    )
                )
                else -> Modifier.background(MaterialTheme.colorScheme.background)
            }
        )
    ) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = if (isLiquidGlass) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onBackground,
        topBar = {
            if (isMainTabs) {
                val titleRes = when (pagerState.currentPage) {
                    0 -> stringResource(R.string.nav_home)
                    1 -> stringResource(R.string.nav_templates)
                    2 -> stringResource(R.string.nav_history)
                    3 -> stringResource(R.string.nav_settings)
                    else -> stringResource(R.string.nav_home)
                }
                @OptIn(ExperimentalMaterial3Api::class)
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = titleRes,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = if (isLiquidGlass) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {},
                    actions = {},
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (isLiquidGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                        titleContentColor = if (isLiquidGlass) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = if (isLiquidGlass) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = if (isLiquidGlass) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface
                    )
                )
            } else if (currentRoute == "create") {
                @OptIn(ExperimentalMaterial3Api::class)
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.nav_create),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = if (isLiquidGlass) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.btn_back),
                                tint = if (isLiquidGlass) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (isLiquidGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                        titleContentColor = if (isLiquidGlass) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = if (isLiquidGlass) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = if (isLiquidGlass) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            if (isMainTabs) {
                val navBarItemColors = if (isLiquidGlass) {
                    androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = androidx.compose.ui.graphics.Color.White,
                        selectedTextColor = androidx.compose.ui.graphics.Color.White,
                        unselectedIconColor = androidx.compose.ui.graphics.Color(0xCCFFFFFF),
                        unselectedTextColor = androidx.compose.ui.graphics.Color(0xCCFFFFFF),
                        indicatorColor = androidx.compose.ui.graphics.Color(0x44FFFFFF)
                    )
                } else {
                    androidx.compose.material3.NavigationBarItemDefaults.colors()
                }
                NavigationBar(
                    containerColor = if (isLiquidGlass) androidx.compose.ui.graphics.Color(0x33FFFFFF) else NavigationBarDefaults.containerColor,
                    modifier = if (isLiquidGlass) Modifier.border(1.dp, androidx.compose.ui.graphics.Color(0x55FFFFFF)) else Modifier
                ) {
                    NavigationBarItem(
                        colors = navBarItemColors,
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                        label = { Text(stringResource(R.string.nav_home), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                    )
                    NavigationBarItem(
                        colors = navBarItemColors,
                        icon = { Icon(Icons.Default.Style, contentDescription = stringResource(R.string.nav_templates)) },
                        label = { Text(stringResource(R.string.nav_templates), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )
                    NavigationBarItem(
                        colors = navBarItemColors,
                        icon = { Icon(Icons.Default.History, contentDescription = stringResource(R.string.nav_history)) },
                        label = { Text(stringResource(R.string.nav_history), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                        selected = pagerState.currentPage == 2,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        }
                    )
                    NavigationBarItem(
                        colors = navBarItemColors,
                        icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                        label = { Text(stringResource(R.string.nav_settings), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                        selected = pagerState.currentPage == 3,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(3)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main_tabs",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("main_tabs") {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToCreate = { navController.navigate("create") },
                            onNavigateToEdit = { navController.navigate("generate/QR_TEXT") },
                            onNavigateToBarcode = { navController.navigate("create") },
                            onNavigateToEditBarcode = { navController.navigate("generate/EAN_13") },
                            onNavigateToScan = {
                                navController.navigate("scan")
                            },
                            onNavigateToTransfer = {
                                navController.navigate("ultra_transfer")
                            }
                        )
                        1 -> TemplatesScreen(viewModel = viewModel)
                        2 -> HistoryScreen(
                            viewModel = viewModel, 
                            onNavigateToCreate = { navController.navigate("create") },
                            onEditCode = { code ->
                                viewModel.setEditingCode(code)
                                navController.navigate("edit_code")
                            }
                        )
                        3 -> SettingsScreen(
                            viewModel = viewModel,
                            onNavigate = { route -> navController.navigate(route) }
                        )
                    }
                }
            }
            composable("settings/theme") { 
                com.example.ui.screens.ThemeSettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) 
            }
            composable("settings/language") { 
                com.example.ui.screens.LanguageSettingsScreen(onBack = { navController.popBackStack() }) 
            }
            composable("settings/data") { 
                com.example.ui.screens.DataSettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) 
            }
            composable("settings/scan") { 
                com.example.ui.screens.ScanSettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) 
            }
            composable("settings/auto") { 
                com.example.ui.screens.AutoActionsSettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) 
            }
            composable("settings/permissions") { 
                com.example.ui.screens.PermissionsSettingsScreen(onBack = { navController.popBackStack() }) 
            }
            composable("settings/about") { 
                com.example.ui.screens.AboutSettingsScreen(onBack = { navController.popBackStack() }) 
            }
            composable("scan") {
                val scanBatchMode by viewModel.scanBatchMode.collectAsState()
                val context = androidx.compose.ui.platform.LocalContext.current
                ScanScreen(
                    viewModel = viewModel,
                    onResult = { content, format ->
                        viewModel.saveCode(content, format, "SCAN")
                        if (!scanBatchMode) {
                            android.widget.Toast.makeText(context, context.getString(R.string.toast_saved), android.widget.Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            android.widget.Toast.makeText(context, "Código guardado", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            composable("create") {
                CreateScreen(onOptionSelected = { type ->
                    if (type == "QR_ULTRA_TRANSFER") {
                        navController.navigate("ultra_transfer")
                    } else {
                        navController.navigate("generate/$type")
                    }
                })
            }
            composable("ultra_transfer") {
                com.example.ui.screens.UltraOpticalTransferScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("edit_code") {
                val editingCode by viewModel.editingCode.collectAsState()
                
                // Hold a stable reference during exit animation
                var stableCode by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(editingCode) }
                androidx.compose.runtime.LaunchedEffect(editingCode) {
                    if (editingCode != null) {
                        stableCode = editingCode
                    }
                }
                val code = stableCode
                
                if (code != null) {
                    val resolvedType = when {
                        code.format != "QR_CODE" && code.format.isNotBlank() && code.format in listOf("EAN_8", "EAN_13", "UPC_E", "UPC_A", "CODE_39", "CODE_93", "CODE_128", "BARCODE_128", "ITF", "PDF_417", "CODABAR", "DATA_MATRIX", "AZTEC") -> code.format
                        code.type.startsWith("QR_") -> code.type
                        code.content.startsWith("http://") || code.content.startsWith("https://") -> "QR_URL"
                        code.content.startsWith("WIFI:") -> "QR_WIFI"
                        code.content.startsWith("tel:") -> "QR_PHONE"
                        code.content.startsWith("mailto:") -> "QR_EMAIL"
                        code.content.startsWith("sms:") -> "QR_SMS"
                        code.content.startsWith("BEGIN:VCARD") -> "QR_CONTACT"
                        code.content.startsWith("https://wa.me/") -> "QR_WHATSAPP"
                        else -> "QR_TEXT"
                    }
                    GenerateScreen(
                        type = resolvedType,
                        editingCode = code,
                        onSave = { updatedContent, updatedFormat, bitmap ->
                            viewModel.updateCode(
                                code.copy(
                                    content = updatedContent,
                                    format = updatedFormat
                                ),
                                bitmap = bitmap
                            )
                            navController.popBackStack()
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
            composable(
                route = "apply_template/{resId}",
                arguments = listOf(androidx.navigation.navArgument("resId") { type = androidx.navigation.NavType.IntType })
            ) { backStackEntry ->
                val resId = backStackEntry.arguments?.getInt("resId") ?: 0
                GenerateScreen(
                    type = "QR_TEXT",
                    templateResId = resId,
                    onSave = { content, format, bitmap ->
                        viewModel.saveCode(content, format, "QR_TEXT", bitmap)
                        navController.popBackStack("main_tabs", inclusive = false)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("generate/{type}") { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "QR_TEXT"
                GenerateScreen(
                    type = type,
                    onNavigateToUltraTransfer = { navController.navigate("ultra_transfer") },
                    onSave = { content, format, bitmap ->
                        viewModel.saveCode(content, format, type, bitmap)
                        navController.popBackStack("main_tabs", inclusive = false)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
    }
}
