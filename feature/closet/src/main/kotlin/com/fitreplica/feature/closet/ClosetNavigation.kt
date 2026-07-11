package com.fitreplica.feature.closet

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fitreplica.core.model.ClothingId
import com.fitreplica.feature.closet.addedit.AddEditItemScreen
import com.fitreplica.feature.closet.detail.ItemDetailScreen
import com.fitreplica.feature.closet.list.ClosetListScreen

const val CLOSET_ROUTE = "closet"
const val ITEM_ID_ARG = "itemId"

private const val ITEM_DETAIL_ROUTE = "closet/item/{$ITEM_ID_ARG}"
private const val ITEM_ADD_ROUTE = "closet/item/add"
private const val ITEM_EDIT_ROUTE = "closet/item/{$ITEM_ID_ARG}/edit"

// All four destinations sit flat inside the shared app-level NavHost (not a nested
// sub-graph) so CLOSET_ROUTE can stay the app's startDestination unchanged.
fun NavGraphBuilder.closetGraph(navController: NavController) {
    composable(CLOSET_ROUTE) {
        ClosetListScreen(
            onItemClick = { itemId -> navController.navigate(itemDetailRoute(itemId)) },
            onAddItemClick = { navController.navigate(ITEM_ADD_ROUTE) },
        )
    }
    composable(
        route = ITEM_DETAIL_ROUTE,
        arguments = listOf(navArgument(ITEM_ID_ARG) { type = NavType.StringType }),
    ) { backStackEntry ->
        val itemId = ClothingId(checkNotNull(backStackEntry.arguments?.getString(ITEM_ID_ARG)))
        ItemDetailScreen(
            onEditClick = { navController.navigate(itemEditRoute(itemId)) },
            onBackClick = { navController.popBackStack() },
            onDeleted = { navController.popBackStack() },
        )
    }
    composable(ITEM_ADD_ROUTE) {
        AddEditItemScreen(onSaved = { navController.popBackStack() }, onCancel = { navController.popBackStack() })
    }
    composable(
        route = ITEM_EDIT_ROUTE,
        arguments = listOf(navArgument(ITEM_ID_ARG) { type = NavType.StringType }),
    ) {
        AddEditItemScreen(onSaved = { navController.popBackStack() }, onCancel = { navController.popBackStack() })
    }
}

private fun itemDetailRoute(itemId: ClothingId) = "closet/item/${itemId.value}"

private fun itemEditRoute(itemId: ClothingId) = "closet/item/${itemId.value}/edit"
