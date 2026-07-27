package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppPalette
import com.example.data.model.AppThemeMode

@Composable
fun CampRentTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    palette: AppPalette = AppPalette.NATURE_GREEN,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when (palette) {
        AppPalette.NATURE_GREEN -> if (darkTheme) {
            darkColorScheme(
                primary = NatureGreenPrimaryContainer,
                onPrimary = NatureGreenOnPrimaryContainer,
                primaryContainer = NatureGreenPrimary,
                onPrimaryContainer = NatureGreenOnPrimary,
                secondary = NatureGreenSecondaryContainer,
                onSecondary = NatureGreenOnSecondaryContainer,
                tertiary = SleekAmberPrimary,
                background = DarkNatureBackground,
                onBackground = Color(0xFFE2E3DD),
                surface = DarkNatureSurface,
                onSurface = Color(0xFFE2E3DD),
                surfaceVariant = DarkNatureSurfaceVariant,
                onSurfaceVariant = Color(0xFFC3C8BB),
                outline = DarkNatureOutline
            )
        } else {
            lightColorScheme(
                primary = NatureGreenPrimary,
                onPrimary = NatureGreenOnPrimary,
                primaryContainer = NatureGreenPrimaryContainer,
                onPrimaryContainer = NatureGreenOnPrimaryContainer,
                secondary = NatureGreenSecondary,
                onSecondary = NatureGreenOnSecondary,
                secondaryContainer = NatureGreenSecondaryContainer,
                onSecondaryContainer = NatureGreenOnSecondaryContainer,
                tertiary = SleekAmberPrimary,
                background = NatureGreenBackground,
                onBackground = NatureGreenOnBackground,
                surface = NatureGreenSurface,
                onSurface = NatureGreenOnSurface,
                surfaceVariant = NatureGreenSurfaceVariant,
                onSurfaceVariant = NatureGreenOnSurfaceVariant,
                outline = NatureGreenOutline,
                outlineVariant = NatureGreenOutlineVariant
            )
        }
        AppPalette.OCEAN_BLUE -> if (darkTheme) {
            darkColorScheme(
                primary = OceanBlueTertiary,
                secondary = OceanBlueSecondary,
                tertiary = AccentGold,
                background = OceanBlueDarkBackground,
                surface = Color(0xFF0F1E3D),
                onPrimary = Color.Black,
                onBackground = Color(0xFFE3EDF7),
                onSurface = Color(0xFFE3EDF7)
            )
        } else {
            lightColorScheme(
                primary = OceanBluePrimary,
                secondary = OceanBlueSecondary,
                tertiary = OceanBlueTertiary,
                background = OceanBlueBackground,
                surface = Color.White,
                onPrimary = Color.White,
                onBackground = Color(0xFF10213A),
                onSurface = Color(0xFF10213A)
            )
        }
        AppPalette.SUNSET_ORANGE -> if (darkTheme) {
            darkColorScheme(
                primary = SunsetOrangeTertiary,
                secondary = SunsetOrangeSecondary,
                tertiary = AccentGold,
                background = SunsetOrangeDarkBackground,
                surface = Color(0xFF38150C),
                onPrimary = Color.Black,
                onBackground = Color(0xFFFCEBE6),
                onSurface = Color(0xFFFCEBE6)
            )
        } else {
            lightColorScheme(
                primary = SunsetOrangePrimary,
                secondary = SunsetOrangeSecondary,
                tertiary = SunsetOrangeTertiary,
                background = SunsetOrangeBackground,
                surface = Color.White,
                onPrimary = Color.White,
                onBackground = Color(0xFF2E120A),
                onSurface = Color(0xFF2E120A)
            )
        }
        AppPalette.WOOD_EARTH -> if (darkTheme) {
            darkColorScheme(
                primary = WoodEarthTertiary,
                secondary = WoodEarthSecondary,
                tertiary = AccentGold,
                background = WoodEarthDarkBackground,
                surface = Color(0xFF2A1C14),
                onPrimary = Color.Black,
                onBackground = Color(0xFFF3EBE6),
                onSurface = Color(0xFFF3EBE6)
            )
        } else {
            lightColorScheme(
                primary = WoodEarthPrimary,
                secondary = WoodEarthSecondary,
                tertiary = WoodEarthTertiary,
                background = WoodEarthBackground,
                surface = Color.White,
                onPrimary = Color.White,
                onBackground = Color(0xFF2B1D14),
                onSurface = Color(0xFF2B1D14)
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

