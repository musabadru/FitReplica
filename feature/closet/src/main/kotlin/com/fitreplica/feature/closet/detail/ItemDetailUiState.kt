package com.fitreplica.feature.closet.detail

import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.Image

data class ItemDetailUiState(
    val item: ClothingItem? = null,
    val images: List<Image> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val photoWarning: String? = null,
    val deleteError: String? = null,
    val conditionError: String? = null,
)
