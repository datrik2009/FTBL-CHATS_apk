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

private val DarkColorScheme = darkColorScheme(
    primary = PitchDarkPrimary,
    onPrimary = PitchOnSurface,
    primaryContainer = PitchDarkPrimaryContainer,
    onPrimaryContainer = PitchOnPrimaryContainer,
    secondary = PitchDarkSecondary,
    background = PitchDarkBackground,
    surface = PitchDarkSurface,
    surfaceVariant = PitchDarkSurfaceVariant,
    onBackground = PitchDarkOnSurface,
    onSurface = PitchDarkOnSurface,
    onSurfaceVariant = PitchDarkOnSurfaceVariant,
    outline = PitchDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = PitchPrimary,
    onPrimary = PitchOnPrimary,
    primaryContainer = PitchPrimaryContainer,
    onPrimaryContainer = PitchOnPrimaryContainer,
    secondary = PitchSecondary,
    onSecondary = PitchOnSecondary,
    secondaryContainer = PitchSecondaryContainer,
    onSecondaryContainer = PitchOnSecondaryContainer,
    tertiary = PitchTertiary,
    onTertiary = PitchOnTertiary,
    background = PitchBackground,
    surface = PitchSurface,
    surfaceVariant = PitchSurfaceVariant,
    surfaceContainer = PitchSurfaceContainer,
    surfaceContainerHigh = PitchSurfaceContainerHigh,
    onBackground = PitchOnSurface,
    onSurface = PitchOnSurface,
    onSurfaceVariant = PitchOnSurfaceVariant,
    outline = PitchOutline,
    outlineVariant = PitchOutlineVariant,
    error = PitchError,
    onError = PitchOnError
)

@Composable
fun FtblChatsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our bespoke Pitch Precision styling by default
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
