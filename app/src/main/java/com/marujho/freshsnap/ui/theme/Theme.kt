package com.marujho.freshsnap.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Green,
    onPrimary = White,
    background = DarkBackground,
    onBackground = Grey,
    surface = DarkBlue,
    onSurface = Grey,
    surfaceVariant = DarkBlue,
    onSurfaceVariant = Grey,
    error = SoftRed
)

private val LightColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = White,
    background = Grey,
    onBackground = DarkBlue,
    surface = White,
    onSurface = DarkBlue,
    surfaceVariant = White,
    onSurfaceVariant = DarkBlue,
    error = SoftRed
)

@Composable
fun FreshSnapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}