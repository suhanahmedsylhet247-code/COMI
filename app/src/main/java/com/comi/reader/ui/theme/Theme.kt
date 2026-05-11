package com.comi.reader.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E0FF),
    onPrimaryContainer = Color(0xFF1B1B6B),
    secondary = Color(0xFF9333EA),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E0FF),
    onSecondaryContainer = Color(0xFF3B0764),
    tertiary = Color(0xFFEC4899),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0F0),
    onTertiaryContainer = Color(0xFF5C0A33),
    background = Color(0xFFF8F8FF),
    onBackground = Color(0xFF1A1A2E),
    surface = Color(0xFFF8F8FF),
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFE8E0F0),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFEF4444)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E0FF),
    secondary = Color(0xFFA855F7),
    onSecondary = Color(0xFF3B0764),
    secondaryContainer = Color(0xFF6B21A8),
    onSecondaryContainer = Color(0xFFF3E0FF),
    tertiary = Color(0xFFF472B6),
    onTertiary = Color(0xFF5C0A33),
    tertiaryContainer = Color(0xFFBE185D),
    onTertiaryContainer = Color(0xFFFFE0F0),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFF87171)
)

private val AmoledDarkColorScheme = DarkColorScheme.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF1A1A1A)
)

@Composable
fun ComiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoledDark: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                val scheme = dynamicDarkColorScheme(context)
                if (amoledDark) scheme.copy(background = Color.Black, surface = Color.Black) else scheme
            } else dynamicLightColorScheme(context)
        }
        darkTheme && amoledDark -> AmoledDarkColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
