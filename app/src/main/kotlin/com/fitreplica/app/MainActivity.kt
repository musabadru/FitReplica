package com.fitreplica.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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
                icon = { BottomNavigationIcon(selected = selected) },
                label = { Text(text = destination.label) },
                alwaysShowLabel = true,
            )
        }
    }
}

@Composable
private fun BottomNavigationIcon(selected: Boolean) {
    val iconColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    Box(
        modifier =
            Modifier
                .size(BottomNavIconSize)
                .clip(RoundedCornerShape(BottomNavIconCornerRadius))
                .background(iconColor),
    )
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
    val label: String,
)

private val TopLevelDestinations =
    listOf(
        TopLevelDestination(route = CLOSET_ROUTE, label = "Closet"),
        TopLevelDestination(route = OUTFIT_ROUTE, label = "Outfit"),
        TopLevelDestination(route = HISTORY_ROUTE, label = "History"),
        TopLevelDestination(route = LAUNDRY_ROUTE, label = "Laundry"),
        TopLevelDestination(route = ANALYTICS_ROUTE, label = "Analytics"),
    )

private val BottomNavIconSize = 8.dp
private val BottomNavIconCornerRadius = 2.dp
