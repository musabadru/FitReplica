package com.fitreplica.core.designsystem.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Named spring presets so transitions are tuned once here, not ad hoc per screen.
 */
object WardrobeMotion {
    /** List/filter changes — calm, minimal overshoot. */
    fun <T> calmSpring(): SpringSpec<T> =
        spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )

    /** Avatar reveal, hero transitions — more expressive, some bounce. */
    fun <T> expressiveSpring(): SpringSpec<T> =
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        )
}
