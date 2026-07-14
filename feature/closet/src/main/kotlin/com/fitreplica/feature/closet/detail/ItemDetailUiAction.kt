package com.fitreplica.feature.closet.detail

import com.fitreplica.core.model.Condition

sealed interface ItemDetailUiAction {
    data object OnWearNowClicked : ItemDetailUiAction

    data object OnDeleteConfirmed : ItemDetailUiAction

    data class OnSetPrimaryImage(val imageId: String) : ItemDetailUiAction

    data class OnDeleteImage(val imageId: String) : ItemDetailUiAction

    data class OnPhotoAdded(val sourceUri: String) : ItemDetailUiAction

    data class OnConditionSelected(val condition: Condition) : ItemDetailUiAction
}
