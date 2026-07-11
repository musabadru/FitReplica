package com.fitreplica.avatar.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fitreplica.core.designsystem.component.WardrobeEmptyState
import com.fitreplica.core.model.ClothingItem
import javax.inject.Inject

class NoOpAvatarRenderer @Inject constructor() : AvatarRenderer {
    @Composable
    override fun Render(
        config: AvatarConfig,
        outfit: List<ClothingItem>,
        animationState: AvatarAnimationState,
        modifier: Modifier,
    ) {
        WardrobeEmptyState(
            message = "Avatar preview is off. Turn it on in Settings to preview outfits.",
            modifier = modifier,
        )
    }
}
