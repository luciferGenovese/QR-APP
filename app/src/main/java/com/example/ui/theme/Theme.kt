package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF03A9F4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1F5FE),
    onSecondaryContainer = Color(0xFF01579B),
    tertiary = Color(0xFF00BCD4),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF1E293B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF8FAFC),
    surfaceContainerHighest = Color(0xFFFFFFFF),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
  )

private val PinkColorScheme = lightColorScheme(
    primary = Color(0xFFF07C92),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF0F3),
    onPrimaryContainer = Color(0xFFF07C92),
    secondary = Color(0xFFF07C92),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF0F3),
    onSecondaryContainer = Color(0xFFF07C92),
    tertiary = Color(0xFF7BB8D6),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEAF5FF),
    onTertiaryContainer = Color(0xFF7BB8D6),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF4A4A4A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF4A4A4A),
    surfaceVariant = Color(0xFFFFF0F3),
    onSurfaceVariant = Color(0xFF4A4A4A),
    outline = Color(0xFFE0E0E0)
)

private val LiquidGlassShapes = Shapes(
    extraSmall = RoundedCornerShape(24.dp),
    small = RoundedCornerShape(24.dp),
    medium = RoundedCornerShape(32.dp),
    large = RoundedCornerShape(40.dp),
    extraLarge = RoundedCornerShape(48.dp)
)

// Original Liquid Glass (Light Translucent Glass)
private val LiquidGlassColorScheme = lightColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0x38FFFFFF),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0x30FFFFFF),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF818CF8),
    onTertiary = Color(0xFF0F172A),
    tertiaryContainer = Color(0x30FFFFFF),
    onTertiaryContainer = Color.White,
    background = Color.Transparent, 
    onBackground = Color.White,
    surface = Color(0x28FFFFFF), 
    onSurface = Color.White,
    surfaceVariant = Color(0x20FFFFFF),
    onSurfaceVariant = Color(0xFFE2E8F0),
    surfaceContainer = Color(0x33FFFFFF),
    surfaceContainerHigh = Color(0x4DFFFFFF),
    surfaceContainerHighest = Color(0xF2042637),
    outline = Color(0x66FFFFFF),
    outlineVariant = Color(0x44FFFFFF),
    inverseSurface = Color(0xF0042637),
    inverseOnSurface = Color.White
)

// Liquid Glass Dark (Dark Translucent Glass)
private val LiquidGlassDarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0x38FFFFFF),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0x30FFFFFF),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF818CF8),
    onTertiary = Color(0xFF0F172A),
    tertiaryContainer = Color(0x30FFFFFF),
    onTertiaryContainer = Color.White,
    background = Color.Transparent, 
    onBackground = Color.White,
    surface = Color(0x28FFFFFF), 
    onSurface = Color.White,
    surfaceVariant = Color(0x20FFFFFF),
    onSurfaceVariant = Color(0xFFE2E8F0),
    surfaceContainer = Color(0x33FFFFFF),
    surfaceContainerHigh = Color(0x4DFFFFFF),
    surfaceContainerHighest = Color(0xF00F172A),
    outline = Color(0x66FFFFFF),
    outlineVariant = Color(0x44FFFFFF),
    inverseSurface = Color(0xF00F172A),
    inverseOnSurface = Color.White
)

@Composable
fun MyApplicationTheme(
  themeMode: String = "system",
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      themeMode == "liquid_glass" -> LiquidGlassColorScheme
      themeMode == "liquid_glass_dark" -> LiquidGlassDarkColorScheme
      themeMode == "pink" -> PinkColorScheme
      themeMode == "system" && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }
  
  val isLiquid = themeMode == "liquid_glass" || themeMode == "liquid_glass_dark"
  val shapes = if (isLiquid) LiquidGlassShapes else MaterialTheme.shapes
  MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = shapes, content = content)
}
