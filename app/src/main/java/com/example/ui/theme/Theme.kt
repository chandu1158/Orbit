package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class IosColors(
    val background: Color,
    val secondaryBackground: Color,
    val tertiaryBackground: Color,
    val groupedBackground: Color,
    val keypadButton: Color,
    val keypadButtonPressed: Color,
    val separator: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val searchBg: Color,
    val accentBlue: Color,
    val callGreen: Color,
    val callRed: Color
)

val LocalIosColors = staticCompositionLocalOf {
    IosColors(
        background = IosLightBackground,
        secondaryBackground = IosLightSecondaryBg,
        tertiaryBackground = IosLightTertiaryBg,
        groupedBackground = IosLightGroupedBg,
        keypadButton = IosLightKeypadButton,
        keypadButtonPressed = IosLightKeypadButtonPressed,
        separator = IosLightSeparator,
        textPrimary = IosLightTextPrimary,
        textSecondary = IosLightTextSecondary,
        searchBg = IosLightSearchBg,
        accentBlue = IosBlue,
        callGreen = IosGreen,
        callRed = IosRed
    )
}

private val LightIosColors = IosColors(
    background = IosLightBackground,
    secondaryBackground = IosLightSecondaryBg,
    tertiaryBackground = IosLightTertiaryBg,
    groupedBackground = IosLightGroupedBg,
    keypadButton = IosLightKeypadButton,
    keypadButtonPressed = IosLightKeypadButtonPressed,
    separator = IosLightSeparator,
    textPrimary = IosLightTextPrimary,
    textSecondary = IosLightTextSecondary,
    searchBg = IosLightSearchBg,
    accentBlue = IosBlue,
    callGreen = IosGreen,
    callRed = IosRed
)

private val DarkIosColors = IosColors(
    background = IosDarkBackground,
    secondaryBackground = IosDarkSecondaryBg,
    tertiaryBackground = IosDarkTertiaryBg,
    groupedBackground = IosDarkGroupedBg,
    keypadButton = IosDarkKeypadButton,
    keypadButtonPressed = IosDarkKeypadButtonPressed,
    separator = IosDarkSeparator,
    textPrimary = IosDarkTextPrimary,
    textSecondary = IosDarkTextSecondary,
    searchBg = IosDarkSearchBg,
    accentBlue = IosBlue,
    callGreen = IosGreen,
    callRed = IosRed
)

private val LightColorScheme = lightColorScheme(
    primary = IosBlue,
    onPrimary = Color.White,
    primaryContainer = IosLightSecondaryBg,
    onPrimaryContainer = IosLightTextPrimary,
    secondary = IosBlue,
    onSecondary = Color.White,
    background = IosLightBackground,
    onBackground = IosLightTextPrimary,
    surface = IosLightBackground,
    onSurface = IosLightTextPrimary,
    surfaceVariant = IosLightSecondaryBg,
    onSurfaceVariant = IosLightTextSecondary,
    outline = IosLightSeparator,
    error = IosRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = IosBlue,
    onPrimary = Color.White,
    primaryContainer = IosDarkSecondaryBg,
    onPrimaryContainer = IosDarkTextPrimary,
    secondary = IosBlue,
    onSecondary = Color.White,
    background = IosDarkBackground,
    onBackground = IosDarkTextPrimary,
    surface = IosDarkBackground,
    onSurface = IosDarkTextPrimary,
    surfaceVariant = IosDarkSecondaryBg,
    onSurfaceVariant = IosDarkTextSecondary,
    outline = IosDarkSeparator,
    error = IosRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val iosColors = if (darkTheme) DarkIosColors else LightIosColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalIosColors provides iosColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
