package com.fitreplica.feature.analytics

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val ANALYTICS_ROUTE = "analytics"

fun NavGraphBuilder.analyticsGraph() {
    composable(ANALYTICS_ROUTE) {
        AnalyticsScreen()
    }
}
