package com.fitreplica.feature.laundry

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val LAUNDRY_ROUTE = "laundry"

fun NavGraphBuilder.laundryGraph() {
    composable(LAUNDRY_ROUTE) {
        LaundryScreen()
    }
}
