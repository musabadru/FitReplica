package com.fitreplica.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.fitreplica.core.designsystem.theme.FitReplicaTheme
import com.fitreplica.feature.analytics.analyticsGraph
import com.fitreplica.feature.closet.CLOSET_ROUTE
import com.fitreplica.feature.closet.closetGraph
import com.fitreplica.feature.history.historyGraph
import com.fitreplica.feature.laundry.laundryGraph
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
    Surface(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = CLOSET_ROUTE) {
            closetGraph()
            outfitGraph()
            historyGraph()
            laundryGraph()
            analyticsGraph()
        }
    }
}
