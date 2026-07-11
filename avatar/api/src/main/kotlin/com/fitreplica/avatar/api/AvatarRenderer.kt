package com.fitreplica.avatar.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fitreplica.core.model.ClothingItem

interface AvatarRenderer {
    @Composable
    fun Render(
        config: AvatarConfig,
        outfit: List<ClothingItem>,
        animationState: AvatarAnimationState,
        modifier: Modifier,
    )
}
