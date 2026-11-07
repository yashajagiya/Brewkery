package com.example.brewkery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrewkeryColorScheme = lightColorScheme(
    primary = DarkChocolateMocha,
    onPrimary = Color.White,
    primaryContainer = SoftCaramelContainer,
    onPrimaryContainer = DeepEspressoText,
    secondary = GoldenWarmCaramel,
    onSecondary = Color.White,
    secondaryContainer = LightDivider,
    onSecondaryContainer = DeepEspressoText,
    background = WarmOatMilkBackground,
    onBackground = DeepEspressoText,
    surface = SilkyCreamSurface,
    onSurface = DeepEspressoText,
    surfaceVariant = LightDivider,
    onSurfaceVariant = MutedMochaText,
    outline = MutedMochaText
)

private val DarkBrewkeryColorScheme = darkColorScheme(
    primary = GoldenWarmCaramel,
    onPrimary = DarkChocolateMocha,
    primaryContainer = DarkChocolateMocha,
    onPrimaryContainer = GoldenWarmCaramel,
    secondary = SoftCaramelContainer,
    onSecondary = DarkChocolateMocha,
    background = DeepEspressoText,
    onBackground = WarmOatMilkBackground,
    surface = DarkChocolateMocha,
    onSurface = WarmOatMilkBackground
)

@Composable
fun BrewkeryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkBrewkeryColorScheme else BrewkeryColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
