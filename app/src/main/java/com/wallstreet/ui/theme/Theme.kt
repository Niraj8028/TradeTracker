package com.wallstreet.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


val LightBorderPrimary = DarkTextTertiary
val LightBorderSecondary = DarkTextSecondary

val DarkBorderPrimary = PrimaryBlueDark
val DarkBorderSecondary = DarkTextTertiary

data class BorderColors(
    val primary: Color,
    val secondary: Color
)

val LocalBorderColors = staticCompositionLocalOf {
    BorderColors(
        primary = Color.Unspecified,
        secondary = Color.Unspecified
    )
}


private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = PrimaryBlueLight,

    secondary = PrimaryBlue,
    onSecondary = White,

    background = DarkSurface,
    onBackground = DarkTextPrimary,

    surface = DarkBackground,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,

    error = DangerRed,
    onError = White,
    errorContainer = DangerRedDark,
    onErrorContainer = DangerRedLight,

    outline = DarkSurfaceVariant,
    outlineVariant = BorderSecondary,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = PrimaryBlueLight,

    secondary = PrimaryBlue,
    onSecondary = White,

    background = LightBackground,
    onBackground = LightTextPrimary,

    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    error = DangerRed,
    onError = White,
    errorContainer = DangerRedDark,
    onErrorContainer = DangerRedLight,

    outline = BorderPrimary,
    outlineVariant = BorderSecondary,
)

@Composable
fun WallStreetAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val borderColors = if (darkTheme) {
        BorderColors(
            primary = DarkBorderPrimary,
            secondary = DarkBorderSecondary
        )
    } else {
        BorderColors(
            primary = LightBorderPrimary,
            secondary = LightBorderSecondary
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme

        }
    }
    androidx.compose.runtime.CompositionLocalProvider(
        LocalBorderColors provides borderColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
