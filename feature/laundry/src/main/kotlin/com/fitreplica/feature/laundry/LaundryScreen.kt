package com.fitreplica.feature.laundry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.LaundryLoad

@Composable
fun LaundryScreen(
    modifier: Modifier = Modifier,
    viewModel: LaundryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Laundry", style = MaterialTheme.typography.headlineMedium)
                    }
                }
                item {
                    DirtyItemsSection(
                        dirtyItems = uiState.dirtyItems,
                        selectedIds = uiState.selectedItemIds,
                        onSelectionChanged = { itemId, selected ->
                            viewModel.onAction(LaundryUiAction.OnItemSelectionChanged(itemId, selected))
                        },
                        onCreateLoad = { viewModel.onAction(LaundryUiAction.OnCreateLoadClicked) },
                    )
                }
                item { HorizontalDivider() }
                item {
                    Text(
                        "Loads",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (uiState.loads.isEmpty()) {
                    item {
                        Text(
                            "No laundry loads yet.",
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                } else {
                    items(uiState.loads, key = { it.id.value }) { load ->
                        LaundryLoadRow(
                            load = load,
                            onComplete = {
                                viewModel.onAction(LaundryUiAction.OnCompleteLoadClicked(load.id))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DirtyItemsSection(
    dirtyItems: List<ClothingItem>,
    selectedIds: Set<ClothingId>,
    onSelectionChanged: (ClothingId, Boolean) -> Unit,
    onCreateLoad: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Dirty items", style = MaterialTheme.typography.titleLarge)
        if (dirtyItems.isEmpty()) {
            Text("Nothing dirty right now.")
        } else {
            dirtyItems.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = item.id in selectedIds,
                        onCheckedChange = { selected -> onSelectionChanged(item.id, selected) },
                    )
                    Text(item.name, modifier = Modifier.weight(1f))
                }
            }
            Button(
                onClick = onCreateLoad,
                enabled = selectedIds.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Create load")
            }
        }
    }
}

@Composable
private fun LaundryLoadRow(
    load: LaundryLoad,
    onComplete: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${load.itemIds.size} items", style = MaterialTheme.typography.titleMedium)
            Text(if (load.completedAt == null) "In laundry" else "Completed", style = MaterialTheme.typography.bodyMedium)
        }
        if (load.completedAt == null) {
            Button(onClick = onComplete) {
                Text("Complete")
            }
        }
    }
}
