package com.fitreplica.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fitreplica.core.designsystem.theme.FitReplicaTheme
import com.fitreplica.feature.analytics.ANALYTICS_ROUTE
import com.fitreplica.feature.analytics.analyticsGraph
import com.fitreplica.feature.closet.CLOSET_ROUTE
import com.fitreplica.feature.closet.closetGraph
import com.fitreplica.feature.history.HISTORY_ROUTE
import com.fitreplica.feature.history.historyGraph
import com.fitreplica.feature.laundry.LAUNDRY_ROUTE
import com.fitreplica.feature.laundry.laundryGraph
import com.fitreplica.feature.outfit.OUTFIT_ROUTE
import com.fitreplica.feature.outfit.outfitGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitReplicaTheme {
                FitReplicaApp()
            }
        }
    }
}

@Composable
private fun FitReplicaApp() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { FitReplicaBottomNavigation(navController = navController) },
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = CLOSET_ROUTE,
            modifier = Modifier.padding(contentPadding),
        ) {
            closetGraph(navController)
            outfitGraph()
            historyGraph()
            laundryGraph()
            analyticsGraph()
        }
    }
}

@Composable
private fun FitReplicaBottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        TopLevelDestinations.forEach { destination ->
            val selected = currentRoute.isSelectedTopLevelRoute(destination.route)
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateToTopLevelDestination(destination.route) },
                icon = { Icon(imageVector = destination.icon, contentDescription = null) },
                label = { Text(text = stringResource(destination.labelResourceId)) },
                alwaysShowLabel = true,
            )
        }
    }
}

private fun NavHostController.navigateToTopLevelDestination(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun String?.isSelectedTopLevelRoute(route: String): Boolean =
    this == route || this?.substringBefore("/") == route

private data class TopLevelDestination(
    val route: String,
    @param:StringRes val labelResourceId: Int,
    val icon: ImageVector,
)

private val TopLevelDestinations =
    listOf(
        TopLevelDestination(
            route = CLOSET_ROUTE,
            labelResourceId = R.string.navigation_closet,
            icon = Icons.Default.Checkroom,
        ),
        TopLevelDestination(
            route = OUTFIT_ROUTE,
            labelResourceId = R.string.navigation_outfit,
            icon = Icons.Default.Style,
        ),
        TopLevelDestination(
            route = HISTORY_ROUTE,
            labelResourceId = R.string.navigation_history,
            icon = Icons.Default.History,
        ),
        TopLevelDestination(
            route = LAUNDRY_ROUTE,
            labelResourceId = R.string.navigation_laundry,
            icon = Icons.Default.LocalLaundryService,
        ),
        TopLevelDestination(
            route = ANALYTICS_ROUTE,
            labelResourceId = R.string.navigation_analytics,
            icon = Icons.Default.BarChart,
        ),
    )
