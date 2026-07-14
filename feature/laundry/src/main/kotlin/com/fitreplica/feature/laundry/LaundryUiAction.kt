package com.fitreplica.feature.laundry

import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.LaundryLoadId

sealed interface LaundryUiAction {
    data class OnItemSelectionChanged(
        val itemId: ClothingId,
        val selected: Boolean,
    ) : LaundryUiAction

    data object OnCreateLoadClicked : LaundryUiAction

    data class OnCompleteLoadClicked(val loadId: LaundryLoadId) : LaundryUiAction

    data object OnErrorShown : LaundryUiAction
}
