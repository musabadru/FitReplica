package com.fitreplica.feature.closet.addedit

import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.Image
import com.fitreplica.core.model.SizeCategory
import com.fitreplica.core.model.SizeSystem
import com.fitreplica.core.model.Status

// A photo picked/captured before the item itself has been saved — ImageEntity has a real
// FK to clothing_items, so it can't be persisted until a ClothingId row actually exists.
data class StagedPhoto(
    val sourceUri: String,
    val isPrimary: Boolean,
)

data class AddEditItemUiState(
    val editingItemId: ClothingId? = null,
    val isLoading: Boolean = false,
    val name: String = "",
    val type: ClothingType = ClothingType.TOP,
    val brand: String = "",
    val colorPrimary: String = "",
    val colorSecondary: String = "",
    val condition: Condition = Condition.NEW,
    val status: Status = Status.CLEAN,
    val sizeLabel: String = "",
    val sizeSystem: SizeSystem = SizeSystem.UNKNOWN,
    val sizeCategory: SizeCategory = SizeCategory.TOPS,
    val notes: String = "",
    val existingImages: List<Image> = emptyList(),
    val stagedPhotos: List<StagedPhoto> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val saveWarning: String? = null,
) {
    val isEditMode: Boolean get() = editingItemId != null
}
