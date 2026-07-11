package com.fitreplica.feature.closet.list

import com.fitreplica.core.domain.repository.ClosetFilter
import com.fitreplica.core.model.ClothingItem

enum class ClosetViewMode { GRID, LIST }

data class ClosetUiState(
    val items: List<ClothingItem> = emptyList(),
    val filter: ClosetFilter = ClosetFilter(),
    val viewMode: ClosetViewMode = ClosetViewMode.GRID,
    val isLoading: Boolean = true,
)
