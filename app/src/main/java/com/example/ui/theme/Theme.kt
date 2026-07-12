package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFE5E7EB),       // Light silver/grey
    secondary = Color(0xFF9CA3AF),     // Silver/grey
    tertiary = Color(0xFFD1D5DB),      // Lighter grey
    background = Color(0xFF111827),    // Deep rich charcoal black
    surface = Color(0xFF1F2937),       // Lighter surface charcoal
    onPrimary = Color(0xFF111827),
    onSecondary = Color(0xFF111827),
    onTertiary = Color(0xFF111827),
    onBackground = Color(0xFFF9FAFB),
    onSurface = Color(0xFFF9FAFB)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF374151),       // Charcoal
    secondary = Color(0xFF4B5563),     // Medium grey
    tertiary = Color(0xFF6B7280),      // Soft grey
    background = Color(0xFFF9FAFB),    // Off-white
    surface = Color(0xFFFFFFFF),       // Pure white
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color extraction by default so our white/grey theme is fully guaranteed
  dynamicColor: Boolean = false,
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
