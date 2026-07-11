package com.fitreplica.feature.closet.list

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitreplica.core.designsystem.component.WardrobeCard
import com.fitreplica.core.designsystem.component.WardrobeEmptyState
import com.fitreplica.core.designsystem.component.WardrobeFilterChip
import com.fitreplica.core.designsystem.component.WardrobeStatusBadge
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.Status

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosetListScreen(
    onItemClick: (ClothingId) -> Unit,
    onAddItemClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClosetViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var filterPanelExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Closet") },
                actions = {
                    IconButton(onClick = { filterPanelExpanded = !filterPanelExpanded }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
                    IconButton(onClick = { viewModel.onAction(ClosetUiAction.OnViewModeToggled) }) {
                        val viewModeIcon =
                            if (uiState.viewMode == ClosetViewMode.GRID) {
                                Icons.AutoMirrored.Filled.ViewList
                            } else {
                                Icons.Default.GridView
                            }
                        Icon(viewModeIcon, contentDescription = "Toggle view")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItemClick) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = uiState.filter.searchQuery.orEmpty(),
                onValueChange = { viewModel.onAction(ClosetUiAction.OnSearchQueryChanged(it)) },
                label = { Text("Search") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (filterPanelExpanded) {
                ClosetFilterPanel(
                    filter = uiState.filter,
                    onAction = viewModel::onAction,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            if (uiState.items.isEmpty() && !uiState.isLoading) {
                WardrobeEmptyState(
                    message = "No items yet — tap + to add your first piece.",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                ClosetItemsList(
                    items = uiState.items,
                    viewMode = uiState.viewMode,
                    onItemClick = onItemClick,
                    onWearNowClick = { viewModel.onAction(ClosetUiAction.OnWearNowClicked(it)) },
                )
            }
        }
    }
}

@Composable
private fun ClosetItemsList(
    items: List<ClothingItem>,
    viewMode: ClosetViewMode,
    onItemClick: (ClothingId) -> Unit,
    onWearNowClick: (ClothingId) -> Unit,
) {
    if (viewMode == ClosetViewMode.GRID) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.id.value }) { item ->
                ClosetItemCard(item, onClick = { onItemClick(item.id) }, onWearNowClick = { onWearNowClick(item.id) })
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.id.value }) { item ->
                ClosetItemCard(item, onClick = { onItemClick(item.id) }, onWearNowClick = { onWearNowClick(item.id) })
            }
        }
    }
}

@Composable
private fun ClosetItemCard(
    item: ClothingItem,
    onClick: () -> Unit,
    onWearNowClick: () -> Unit,
) {
    WardrobeCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            item.brand?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                WardrobeStatusBadge(label = item.status.name)
                TextButton(onClick = onWearNowClick) { Text("Wear now") }
            }
            TextButton(onClick = onClick) { Text("View details") }
        }
    }
}

@Composable
private fun ClosetFilterPanel(
    filter: com.fitreplica.core.domain.repository.ClosetFilter,
    onAction: (ClosetUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        FilterChipRow(
            label = "Type",
            options = ClothingType.entries,
            selected = filter.type,
            onSelected = { onAction(ClosetUiAction.OnTypeFilterChanged(it)) },
        )
        FilterChipRow(
            label = "Status",
            options = Status.entries,
            selected = filter.status,
            onSelected = { onAction(ClosetUiAction.OnStatusFilterChanged(it)) },
        )
        FilterChipRow(
            label = "Condition",
            options = Condition.entries,
            selected = filter.condition,
            onSelected = { onAction(ClosetUiAction.OnConditionFilterChanged(it)) },
        )
        OutlinedTextField(
            value = filter.brand.orEmpty(),
            onValueChange = { onAction(ClosetUiAction.OnBrandFilterChanged(it.ifBlank { null })) },
            label = { Text("Brand") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        )
        OutlinedTextField(
            value = filter.colorPrimary.orEmpty(),
            onValueChange = { onAction(ClosetUiAction.OnColorFilterChanged(it.ifBlank { null })) },
            label = { Text("Color") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun <T : Enum<T>> FilterChipRow(
    label: String,
    options: List<T>,
    selected: T?,
    onSelected: (T?) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            WardrobeFilterChip(label = "All", selected = selected == null, onClick = { onSelected(null) })
            options.forEach { option ->
                WardrobeFilterChip(
                    label = option.name,
                    selected = selected == option,
                    onClick = { onSelected(if (selected == option) null else option) },
                )
            }
        }
    }
}
