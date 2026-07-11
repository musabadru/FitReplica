package com.fitreplica.feature.outfit

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val OUTFIT_ROUTE = "outfit"

fun NavGraphBuilder.outfitGraph() {
    composable(OUTFIT_ROUTE) {
        OutfitScreen()
    }
}
