package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C51), onPrimary = Color.White,
    primaryContainer = Color(0xFFD1F5E4), onPrimaryContainer = Color(0xFF004E3B),
    secondary = Color(0xFF49645A), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4EEE8), onSecondaryContainer = Color(0xFF203C31),
    tertiary = Color(0xFF416477), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCECF5), onTertiaryContainer = Color(0xFF234959),
    background = Color(0xFFF5F7F4), onBackground = Color(0xFF182720),
    surface = Color.White, onSurface = Color(0xFF182720),
    surfaceVariant = Color(0xFFEAF0EB), onSurfaceVariant = Color(0xFF56665D),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF0F4EF),
    surfaceContainer = Color(0xFFEDF2ED), surfaceContainerHigh = Color(0xFFE6EDE6),
    surfaceContainerHighest = Color(0xFFDFE8E0),
    outline = Color(0xFF78877D), outlineVariant = Color(0xFFDCE4DC),
    inverseSurface = Color(0xFF182E25), inverseOnSurface = Color(0xFFF1F7F2),
    inversePrimary = Color(0xFF86DEB8),
    error = Color(0xFFB3261E), errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF86DEB8), onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF14513C), onPrimaryContainer = Color(0xFFB5F4D5),
    secondary = Color(0xFFB5CCBF), onSecondary = Color(0xFF20382D),
    secondaryContainer = Color(0xFF334B40), onSecondaryContainer = Color(0xFFD1E8DA),
    tertiary = Color(0xFFA7CEDE), onTertiary = Color(0xFF123543),
    tertiaryContainer = Color(0xFF2B4D5C), onTertiaryContainer = Color(0xFFC5EAF9),
    background = Color(0xFF101A16), onBackground = Color(0xFFE2ECE4),
    surface = Color(0xFF17241E), onSurface = Color(0xFFE2ECE4),
    surfaceVariant = Color(0xFF26362C), onSurfaceVariant = Color(0xFFB0C1B5),
    surfaceContainerLowest = Color(0xFF0C1510), surfaceContainerLow = Color(0xFF17241E),
    surfaceContainer = Color(0xFF1C2B22), surfaceContainerHigh = Color(0xFF25352B),
    surfaceContainerHighest = Color(0xFF304237),
    outline = Color(0xFF82978A), outlineVariant = Color(0xFF354A3D),
    inverseSurface = Color(0xFFE2ECE4), inverseOnSurface = Color(0xFF182E25),
    inversePrimary = Color(0xFF006C51)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "modern",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        "light", "pink", "liquid_glass" -> false
        "dark", "liquid_glass_dark" -> true
        else -> darkTheme
    }
    val base = if (useDark) DarkColorScheme else LightColorScheme
    val colorScheme = when {
        themeMode == "pink" -> LightColorScheme.copy(
            primary = Color(0xFF984562), onPrimary = Color.White,
            primaryContainer = Color(0xFFFFD9E5), onPrimaryContainer = Color(0xFF5F1733)
        )
        themeMode == "liquid_glass" -> LightColorScheme.copy(
            primary = Color(0xFF00687A), primaryContainer = Color(0xFFC6F1FB),
            onPrimaryContainer = Color(0xFF004E5D)
        )
        themeMode == "liquid_glass_dark" -> DarkColorScheme.copy(
            primary = Color(0xFF81D4E6), onPrimary = Color(0xFF003641),
            primaryContainer = Color(0xFF164F5B), onPrimaryContainer = Color(0xFFC6F1FB)
        )
        themeMode == "system" && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> base
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = AppShapes, content = content)
}
