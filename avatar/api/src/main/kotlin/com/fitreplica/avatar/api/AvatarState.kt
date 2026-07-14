package com.fitreplica.avatar.api

import com.fitreplica.core.model.ClothingItem

data class AvatarState(
    val config: AvatarConfig = AvatarConfig.default(),
    val outfit: List<ClothingItem> = emptyList(),
    val animationState: AvatarAnimationState = AvatarAnimationState.IDLE,
)
