package com.fitreplica.avatar.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fitreplica.core.designsystem.component.WardrobeAvatarPlaceholder
import com.fitreplica.core.model.ClothingItem
import javax.inject.Inject

class NoOpAvatarRenderer
    @Inject
    constructor() : AvatarRenderer {
        @Composable
        override fun Render(
            config: AvatarConfig,
            outfit: List<ClothingItem>,
            animationState: AvatarAnimationState,
            modifier: Modifier,
        ) {
            WardrobeAvatarPlaceholder(
                message = "Add measurements to preview fit.",
                modifier = modifier,
            )
        }
    }
