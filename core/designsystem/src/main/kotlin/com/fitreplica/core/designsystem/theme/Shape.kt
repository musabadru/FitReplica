package com.fitreplica.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val WardrobeShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

/** Reserved for primary CTAs (log wear, save outfit) — reads as the app's signature action. */
val WardrobeExpressiveCtaShape =
    RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 8.dp,
        bottomStart = 8.dp,
        bottomEnd = 20.dp,
    )
