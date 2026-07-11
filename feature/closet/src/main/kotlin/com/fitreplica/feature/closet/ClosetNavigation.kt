package com.fitreplica.feature.closet

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val CLOSET_ROUTE = "closet"

fun NavGraphBuilder.closetGraph() {
    composable(CLOSET_ROUTE) {
        ClosetScreen()
    }
}
