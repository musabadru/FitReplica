package com.fitreplica.feature.closet.list

import com.fitreplica.core.domain.repository.ClosetFilter
import com.fitreplica.core.model.ClothingItem

enum class ClosetViewMode { GRID, LIST }

data class ClosetUiState(
    val items: List<ClothingItem> = emptyList(),
    // The raw, un-debounced search text — kept separate from `filter` so the search field
    // itself never lags behind what the user is typing, even though the query actually sent
    // to observeItems() is debounced (see ClosetViewModel).
    val searchQuery: String = "",
    val filter: ClosetFilter = ClosetFilter(),
    val viewMode: ClosetViewMode = ClosetViewMode.GRID,
    val isLoading: Boolean = true,
)
