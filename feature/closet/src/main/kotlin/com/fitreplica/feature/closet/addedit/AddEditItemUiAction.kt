package com.fitreplica.feature.closet.addedit

import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.SizeCategory
import com.fitreplica.core.model.SizeSystem
import com.fitreplica.core.model.Status

sealed interface AddEditItemUiAction {
    data class OnNameChanged(val value: String) : AddEditItemUiAction

    data class OnTypeChanged(val value: ClothingType) : AddEditItemUiAction

    data class OnBrandChanged(val value: String) : AddEditItemUiAction

    data class OnColorPrimaryChanged(val value: String) : AddEditItemUiAction

    data class OnColorSecondaryChanged(val value: String) : AddEditItemUiAction

    data class OnConditionChanged(val value: Condition) : AddEditItemUiAction

    data class OnStatusChanged(val value: Status) : AddEditItemUiAction

    data class OnSizeLabelChanged(val value: String) : AddEditItemUiAction

    data class OnSizeSystemChanged(val value: SizeSystem) : AddEditItemUiAction

    data class OnSizeCategoryChanged(val value: SizeCategory) : AddEditItemUiAction

    data class OnNotesChanged(val value: String) : AddEditItemUiAction

    // identifier is an existing Image.id in edit mode, a StagedPhoto.sourceUri in add mode.
    data class OnPhotoAdded(val sourceUri: String) : AddEditItemUiAction

    data class OnPhotoRemoved(val identifier: String) : AddEditItemUiAction

    data class OnSetPrimaryPhoto(val identifier: String) : AddEditItemUiAction

    data object OnSaveClicked : AddEditItemUiAction
}
