package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class AppThemeSetting {
    WHITE,
    DARK,
    COLORFUL
}

private val WhiteColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onBackground = LightOnBackground,
    outline = LightBorder,
    surfaceVariant = LightBackground,
    onSurfaceVariant = LightOnSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onBackground = DarkOnBackground,
    outline = DarkBorder,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkOnSurface
)

private val ColorfulColorScheme = lightColorScheme(
    primary = ColorfulPrimary,
    onPrimary = ColorfulOnPrimary,
    secondary = ColorfulSecondary,
    background = ColorfulBackground,
    surface = ColorfulSurface,
    onSurface = ColorfulOnSurface,
    onBackground = ColorfulOnBackground,
    outline = ColorfulBorder,
    surfaceVariant = ColorfulBackground,
    onSurfaceVariant = ColorfulOnSurface
)

@Composable
fun PedaratAiTheme(
    themeSetting: AppThemeSetting = AppThemeSetting.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (themeSetting) {
        AppThemeSetting.WHITE -> WhiteColorScheme
        AppThemeSetting.DARK -> DarkColorScheme
        AppThemeSetting.COLORFUL -> ColorfulColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
