package com.fitreplica.feature.laundry

import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.LaundryLoad

data class LaundryUiState(
    val dirtyItems: List<ClothingItem> = emptyList(),
    val loads: List<LaundryLoad> = emptyList(),
    val selectedItemIds: Set<ClothingId> = emptySet(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
