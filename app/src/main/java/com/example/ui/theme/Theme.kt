package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ClinicalTeal,
    onPrimary = Color.White,
    primaryContainer = MintSurface,
    onPrimaryContainer = MintOnSurface,
    secondary = MedicalEmerald,
    onSecondary = Color.White,
    secondaryContainer = MedicalEmeraldContainer,
    onSecondaryContainer = InkPrimaryLight,
    tertiary = WarmCoral,
    onTertiary = Color.White,
    tertiaryContainer = WarmCoralContainer,
    onTertiaryContainer = InkPrimaryLight,
    background = CanvasLight,
    onBackground = InkPrimaryLight,
    surface = SurfaceCardLight,
    onSurface = InkPrimaryLight,
    surfaceVariant = SurfaceMutedLight,
    onSurfaceVariant = InkSecondaryLight,
    outline = OutlineSubtleLight,
    error = AlertRedLight,
    onError = Color.White,
    errorContainer = AlertRedContainerLight,
    onErrorContainer = InkPrimaryLight
)

private val DarkColorScheme = darkColorScheme(
    primary = TealAccentDark,
    onPrimary = ClinicalTealDark,
    primaryContainer = MintContainerDark,
    onPrimaryContainer = InkPrimaryDark,
    secondary = TealAccentDark,
    onSecondary = ClinicalTealDark,
    secondaryContainer = SurfaceMutedDark,
    onSecondaryContainer = InkPrimaryDark,
    tertiary = WarmCoralContainer,
    onTertiary = InkPrimaryLight,
    background = CanvasDark,
    onBackground = InkPrimaryDark,
    surface = SurfaceCardDark,
    onSurface = InkPrimaryDark,
    surfaceVariant = SurfaceMutedDark,
    onSurfaceVariant = InkSecondaryDark,
    outline = OutlineSubtleDark,
    error = AlertRedDark,
    onError = AlertRedContainerDark,
    errorContainer = AlertRedContainerDark,
    onErrorContainer = InkPrimaryDark
)

val NursingShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun NursingAiTheme(
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
        typography = NursingTypography,
        shapes = NursingShapes,
        content = content
    )
}
