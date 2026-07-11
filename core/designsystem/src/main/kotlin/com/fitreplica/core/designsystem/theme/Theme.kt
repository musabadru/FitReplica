package com.fitreplica.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors =
    lightColorScheme(
        primary = WardrobePrimary,
        onPrimary = WardrobeOnPrimary,
        secondary = WardrobeSecondary,
        tertiary = WardrobeTertiary,
        background = WardrobeBackground,
        surface = WardrobeSurface,
        error = WardrobeError,
    )

private val DarkColors =
    darkColorScheme(
        primary = WardrobePrimaryDark,
        onPrimary = WardrobeOnPrimaryDark,
        secondary = WardrobeSecondaryDark,
        tertiary = WardrobeTertiaryDark,
        background = WardrobeBackgroundDark,
        surface = WardrobeSurfaceDark,
        error = WardrobeErrorDark,
    )

@Composable
fun FitReplicaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Material You dynamic color is the default; the expressive palette above is the
    // explicit alternate a user can pick in onboarding, not the only option.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            darkTheme -> DarkColors
            else -> LightColors
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WardrobeTypography,
        shapes = WardrobeShapes,
        content = content,
    )
}
