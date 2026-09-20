package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = HoneyYellowLight,
    onPrimary = DarkSurface,
    primaryContainer = HoneyYellow,
    secondary = DustyRoseLight,
    onSecondary = DarkSurface,
    secondaryContainer = DustyRose,
    tertiary = TealBrandLight,
    background = DarkSurface,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF334155),
    onSurface = Color.White,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = HoneyYellow,
    onPrimary = Color.White,
    primaryContainer = HoneyYellowSoft,
    secondary = DustyRose,
    onSecondary = Color.White,
    secondaryContainer = DustyRoseSoft,
    tertiary = TealBrand,
    onTertiary = Color.White,
    background = HoneyYellowCream,
    surface = LightSurface,
    surfaceVariant = HoneyYellowSoft,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
