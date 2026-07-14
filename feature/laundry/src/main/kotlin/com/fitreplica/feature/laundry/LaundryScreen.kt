package com.fitreplica.feature.laundry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
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
                    Text(
                        "Dirty items",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (uiState.dirtyItems.isEmpty()) {
                    item {
                        Text(
                            "Nothing dirty right now.",
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                } else {
                    items(uiState.dirtyItems, key = { it.id.value }) { item ->
                        DirtyItemRow(
                            item = item,
                            selected = item.id in uiState.selectedItemIds,
                            onSelectionChanged = { itemId, selected ->
                                viewModel.onAction(LaundryUiAction.OnItemSelectionChanged(itemId, selected))
                            },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                    item {
                        Button(
                            onClick = { viewModel.onAction(LaundryUiAction.OnCreateLoadClicked) },
                            enabled = uiState.selectedItemIds.isNotEmpty() && !uiState.isCreatingLoad,
                            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                        ) {
                            Text("Create load")
                        }
                    }
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
private fun DirtyItemRow(
    item: ClothingItem,
    selected: Boolean,
    onSelectionChanged: (ClothingId, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .toggleable(
                    value = selected,
                    role = Role.Checkbox,
                    onValueChange = { checked -> onSelectionChanged(item.id, checked) },
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = null,
        )
        Text(item.name, modifier = Modifier.weight(1f))
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
            Text(
                if (load.completedAt == null) "In laundry" else "Completed",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (load.completedAt == null) {
            Button(onClick = onComplete) {
                Text("Complete")
            }
        }
    }
}
