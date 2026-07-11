package com.fitreplica.feature.closet.detail

sealed interface ItemDetailUiAction {
    data object OnWearNowClicked : ItemDetailUiAction

    data object OnDeleteConfirmed : ItemDetailUiAction

    data class OnSetPrimaryImage(val imageId: String) : ItemDetailUiAction

    data class OnDeleteImage(val imageId: String) : ItemDetailUiAction

    data class OnPhotoAdded(val sourceUri: String) : ItemDetailUiAction
}
