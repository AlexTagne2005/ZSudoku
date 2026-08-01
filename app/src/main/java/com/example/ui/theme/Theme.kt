package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = SandBackground,
    primaryContainer = SagePrimaryContainer,
    onPrimaryContainer = SageOnPrimaryContainer,
    secondary = SandGoldSecondary,
    tertiary = TerracottaAccent,
    background = SandBackground,
    surface = ParchmentSurface,
    surfaceVariant = WarmWoodVariant,
    onBackground = SlateTextPrimary,
    onSurface = SlateTextPrimary,
    onSurfaceVariant = SlateTextMuted
)

private val DarkColorScheme = darkColorScheme(
    primary = MoonlitTealPrimary,
    onPrimary = NightBlueBackground,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,
    secondary = MoonAmberSecondary,
    background = NightBlueBackground,
    surface = DarkSlateSurface,
    surfaceVariant = DeepSteelVariant,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextMuted
)

private val ForestLightScheme = lightColorScheme(
    primary = ForestPrimaryLight,
    onPrimary = ForestBgLight,
    background = ForestBgLight,
    surface = ForestSurfaceLight,
    surfaceVariant = ForestVariantLight,
    onBackground = SlateTextPrimary,
    onSurface = SlateTextPrimary,
    onSurfaceVariant = SlateTextMuted
)

private val ForestDarkScheme = darkColorScheme(
    primary = ForestPrimaryDark,
    onPrimary = ForestBgDark,
    background = ForestBgDark,
    surface = ForestSurfaceDark,
    surfaceVariant = ForestVariantDark,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextMuted
)

private val OceanLightScheme = lightColorScheme(
    primary = OceanPrimaryLight,
    onPrimary = OceanBgLight,
    background = OceanBgLight,
    surface = OceanSurfaceLight,
    surfaceVariant = OceanVariantLight,
    onBackground = SlateTextPrimary,
    onSurface = SlateTextPrimary,
    onSurfaceVariant = SlateTextMuted
)

private val OceanDarkScheme = darkColorScheme(
    primary = OceanPrimaryDark,
    onPrimary = OceanBgDark,
    background = OceanBgDark,
    surface = OceanSurfaceDark,
    surfaceVariant = OceanVariantDark,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextMuted
)

private val SunsetLightScheme = lightColorScheme(
    primary = SunsetPrimaryLight,
    onPrimary = SunsetBgLight,
    background = SunsetBgLight,
    surface = SunsetSurfaceLight,
    surfaceVariant = SunsetVariantLight,
    onBackground = SlateTextPrimary,
    onSurface = SlateTextPrimary,
    onSurfaceVariant = SlateTextMuted
)

private val SunsetDarkScheme = darkColorScheme(
    primary = SunsetPrimaryDark,
    onPrimary = SunsetBgDark,
    background = SunsetBgDark,
    surface = SunsetSurfaceDark,
    surfaceVariant = SunsetVariantDark,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextMuted
)

private val OledDarkScheme = darkColorScheme(
    primary = OledPrimary,
    onPrimary = OledOnPrimary,
    primaryContainer = Color(0xFF00363D),
    onPrimaryContainer = Color(0xFF80F2FF),
    secondary = Color(0xFFFFD600),
    background = OledBg,
    surface = OledSurface,
    surfaceVariant = OledVariant,
    onBackground = OledTextPrimary,
    onSurface = OledTextPrimary,
    onSurfaceVariant = OledTextMuted
)

@Composable
fun ZenSudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorThemeName: String = "ZEN",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> when (colorThemeName) {
            "FOREST" -> if (darkTheme) ForestDarkScheme else ForestLightScheme
            "OCEAN" -> if (darkTheme) OceanDarkScheme else OceanLightScheme
            "SUNSET" -> if (darkTheme) SunsetDarkScheme else SunsetLightScheme
            "OLED" -> OledDarkScheme
            else -> if (darkTheme) DarkColorScheme else LightColorScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
