package com.fitreplica.feature.laundry

import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.LaundryLoad
import com.fitreplica.core.model.LaundryLoadId

data class LaundryUiState(
    val dirtyItems: List<ClothingItem> = emptyList(),
    val loads: List<LaundryLoad> = emptyList(),
    val selectedItemIds: Set<ClothingId> = emptySet(),
    val completingLoadIds: Set<LaundryLoadId> = emptySet(),
    val isLoading: Boolean = true,
    val isCreatingLoad: Boolean = false,
    val errorMessage: String? = null,
)
