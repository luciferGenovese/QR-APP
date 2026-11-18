package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CodeEntity
import com.example.data.CodeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val context: android.content.Context, private val repository: CodeRepository, private val prefs: android.content.SharedPreferences) : ViewModel() {
    val history: StateFlow<List<CodeEntity>> = repository.allCodes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _themeState = kotlinx.coroutines.flow.MutableStateFlow(prefs.getString("selected_theme", "modern") ?: "modern")
    val themeState: StateFlow<String> = _themeState

    // Settings State
    private val _scanBatchMode = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("scan_batch_mode", false))
    val scanBatchMode: StateFlow<Boolean> = _scanBatchMode

    private val _scanKeepScreenOn = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("scan_keep_screen_on", true))
    val scanKeepScreenOn: StateFlow<Boolean> = _scanKeepScreenOn

    private val _scanSound = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("scan_sound", true))
    val scanSound: StateFlow<Boolean> = _scanSound

    private val _scanVibration = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("scan_vibration", true))
    val scanVibration: StateFlow<Boolean> = _scanVibration

    private val _autoCopyClipboard = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("auto_copy_clipboard", false))
    val autoCopyClipboard: StateFlow<Boolean> = _autoCopyClipboard

    private val _autoOpenLinks = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("auto_open_links", false))
    val autoOpenLinks: StateFlow<Boolean> = _autoOpenLinks

    private val _searchEngine = kotlinx.coroutines.flow.MutableStateFlow(prefs.getString("search_engine", "Google") ?: "Google")
    val searchEngine: StateFlow<String> = _searchEngine

    private val _soundPreset = kotlinx.coroutines.flow.MutableStateFlow(prefs.getString("sound_preset", "chaching") ?: "chaching")
    val soundPreset: StateFlow<String> = _soundPreset

    fun setSoundPreset(value: String) {
        prefs.edit().putString("sound_preset", value).apply()
        _soundPreset.value = value
    }

    fun playScanFeedback() {
        com.example.utils.SoundFxHelper.playFeedback(
            context = context,
            preset = _soundPreset.value,
            vibrate = _scanVibration.value,
            sound = _scanSound.value
        )
    }

    fun setTheme(theme: String) {
        prefs.edit().putString("selected_theme", theme).apply()
        _themeState.value = theme
    }

    fun setScanBatchMode(value: Boolean) {
        prefs.edit().putBoolean("scan_batch_mode", value).apply()
        _scanBatchMode.value = value
    }

    fun setScanKeepScreenOn(value: Boolean) {
        prefs.edit().putBoolean("scan_keep_screen_on", value).apply()
        _scanKeepScreenOn.value = value
    }

    fun setScanSound(value: Boolean) {
        prefs.edit().putBoolean("scan_sound", value).apply()
        _scanSound.value = value
    }

    fun setScanVibration(value: Boolean) {
        prefs.edit().putBoolean("scan_vibration", value).apply()
        _scanVibration.value = value
    }

    fun setAutoCopyClipboard(value: Boolean) {
        prefs.edit().putBoolean("auto_copy_clipboard", value).apply()
        _autoCopyClipboard.value = value
    }

    fun setAutoOpenLinks(value: Boolean) {
        prefs.edit().putBoolean("auto_open_links", value).apply()
        _autoOpenLinks.value = value
    }

    fun setSearchEngine(value: String) {
        prefs.edit().putString("search_engine", value).apply()
        _searchEngine.value = value
    }

    private val _editingCode = kotlinx.coroutines.flow.MutableStateFlow<CodeEntity?>(null)
    val editingCode: StateFlow<CodeEntity?> = _editingCode

    fun setEditingCode(code: CodeEntity?) {
        _editingCode.value = code
    }

    fun saveCode(content: String, format: String, type: String, bitmap: android.graphics.Bitmap? = null) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val entity = CodeEntity(content = content, format = format, type = type)
            val id = repository.insert(entity)
            if (bitmap != null) {
                try {
                    val file = java.io.File(context.filesDir, "qr_$id.png")
                    val out = java.io.FileOutputStream(file)
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()
                } catch(e: Exception) { e.printStackTrace() }
            }
        }
    }
    
    fun updateCode(code: CodeEntity, bitmap: android.graphics.Bitmap? = null) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.update(code)
            if (bitmap != null) {
                try {
                    val file = java.io.File(context.filesDir, "qr_${code.id}.png")
                    val out = java.io.FileOutputStream(file)
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()
                } catch(e: Exception) { e.printStackTrace() }
            }
        }
    }
    
    fun deleteCode(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
    
    fun toggleFavorite(code: com.example.data.CodeEntity) {
        viewModelScope.launch {
            repository.update(code.copy(isFavorite = !code.isFavorite))
        }
    }


    fun deleteAllCodes() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
}

class MainViewModelFactory(
    private val context: android.content.Context,
    private val repository: CodeRepository,
    private val prefs: android.content.SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(context, repository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
