package com.fitreplica.feature.history

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val HISTORY_ROUTE = "history"

fun NavGraphBuilder.historyGraph() {
    composable(HISTORY_ROUTE) {
        HistoryScreen()
    }
}
