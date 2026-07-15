package com.fitreplica.feature.outfit

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fitreplica.avatar.api.AvatarRenderer

const val OUTFIT_ROUTE = "outfit"

fun NavGraphBuilder.outfitGraph(avatarRendererProvider: () -> AvatarRenderer) {
    composable(OUTFIT_ROUTE) {
        // App owns concrete renderer selection so feature modules never depend on :avatar:impl-*.
        OutfitScreen(avatarRenderer = avatarRendererProvider())
    }
}
