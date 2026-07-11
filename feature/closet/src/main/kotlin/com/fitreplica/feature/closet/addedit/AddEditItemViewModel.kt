package com.fitreplica.feature.closet.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitreplica.core.common.Result
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.domain.repository.ImageRepository
import com.fitreplica.core.domain.usecase.AddClothingItemUseCase
import com.fitreplica.core.domain.usecase.UpdateClothingItemUseCase
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.GarmentSize
import com.fitreplica.core.model.SizeCategory
import com.fitreplica.core.model.SizeSystem
import com.fitreplica.feature.closet.ITEM_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val PHOTO_SAVE_WARNING = "Item saved, but one or more photos couldn't be saved (storage issue)."

@HiltViewModel
class AddEditItemViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val clothingRepository: ClothingRepository,
        private val imageRepository: ImageRepository,
        private val addClothingItemUseCase: AddClothingItemUseCase,
        private val updateClothingItemUseCase: UpdateClothingItemUseCase,
    ) : ViewModel() {
        private val editingItemId: ClothingId? = savedStateHandle.get<String>(ITEM_ID_ARG)?.let { ClothingId(it) }
        private var addedAt: Long = System.currentTimeMillis()
        private var timesWorn = 0
        private var lastWornAt: Long? = null

        private val _uiState =
            MutableStateFlow(
                AddEditItemUiState(editingItemId = editingItemId, isLoading = editingItemId != null),
            )
        val uiState: StateFlow<AddEditItemUiState> = _uiState.asStateFlow()

        init {
            editingItemId?.let(::loadExistingItem)
        }

        private fun loadExistingItem(itemId: ClothingId) {
            viewModelScope.launch {
                val item = clothingRepository.observeItem(itemId).first()
                if (item != null) populateFrom(item)
                _uiState.update { it.copy(isLoading = false) }
            }
            viewModelScope.launch {
                imageRepository.observeImages(itemId).collect { images ->
                    _uiState.update { it.copy(existingImages = images) }
                }
            }
        }

        private fun populateFrom(item: ClothingItem) {
            addedAt = item.addedAt
            timesWorn = item.timesWorn
            lastWornAt = item.lastWornAt
            _uiState.update {
                it.copy(
                    name = item.name,
                    type = item.type,
                    brand = item.brand.orEmpty(),
                    colorPrimary = item.colorPrimary,
                    colorSecondary = item.colorSecondary.orEmpty(),
                    condition = item.condition,
                    status = item.status,
                    sizeLabel = item.size?.label.orEmpty(),
                    sizeSystem = item.size?.system ?: SizeSystem.UNKNOWN,
                    sizeCategory = item.size?.category ?: SizeCategory.TOPS,
                    notes = item.notes.orEmpty(),
                )
            }
        }

        @Suppress("CyclomaticComplexMethod")
        fun onAction(action: AddEditItemUiAction) {
            when (action) {
                is AddEditItemUiAction.OnNameChanged -> _uiState.update { it.copy(name = action.value) }
                is AddEditItemUiAction.OnTypeChanged -> _uiState.update { it.copy(type = action.value) }
                is AddEditItemUiAction.OnBrandChanged -> _uiState.update { it.copy(brand = action.value) }
                is AddEditItemUiAction.OnColorPrimaryChanged -> _uiState.update { it.copy(colorPrimary = action.value) }
                is AddEditItemUiAction.OnColorSecondaryChanged ->
                    _uiState.update {
                        it.copy(
                            colorSecondary = action.value,
                        )
                    }
                is AddEditItemUiAction.OnConditionChanged -> _uiState.update { it.copy(condition = action.value) }
                is AddEditItemUiAction.OnStatusChanged -> _uiState.update { it.copy(status = action.value) }
                is AddEditItemUiAction.OnSizeLabelChanged -> _uiState.update { it.copy(sizeLabel = action.value) }
                is AddEditItemUiAction.OnSizeSystemChanged -> _uiState.update { it.copy(sizeSystem = action.value) }
                is AddEditItemUiAction.OnSizeCategoryChanged -> _uiState.update { it.copy(sizeCategory = action.value) }
                is AddEditItemUiAction.OnNotesChanged -> _uiState.update { it.copy(notes = action.value) }
                is AddEditItemUiAction.OnPhotoAdded -> onPhotoAdded(action.sourceUri)
                is AddEditItemUiAction.OnPhotoRemoved -> onPhotoRemoved(action.identifier)
                is AddEditItemUiAction.OnSetPrimaryPhoto -> onSetPrimaryPhoto(action.identifier)
                AddEditItemUiAction.OnSaveClicked -> onSave()
            }
        }

        private fun onPhotoAdded(sourceUri: String) {
            val itemId = editingItemId
            if (itemId != null) {
                viewModelScope.launch {
                    val isFirstPhoto = _uiState.value.existingImages.isEmpty()
                    val result = imageRepository.addImage(itemId, sourceUri, isPrimary = isFirstPhoto)
                    if (result is Result.Error) {
                        _uiState.update { it.copy(saveWarning = PHOTO_SAVE_WARNING) }
                    }
                }
            } else {
                _uiState.update { state ->
                    val isFirstPhoto = state.stagedPhotos.isEmpty()
                    state.copy(stagedPhotos = state.stagedPhotos + StagedPhoto(sourceUri, isPrimary = isFirstPhoto))
                }
            }
        }

        private fun onPhotoRemoved(identifier: String) {
            if (editingItemId != null) {
                viewModelScope.launch { imageRepository.deleteImage(identifier) }
            } else {
                _uiState.update { state ->
                    val remaining = state.stagedPhotos.filterNot { it.sourceUri == identifier }
                    val removedWasPrimary = state.stagedPhotos.any { it.sourceUri == identifier && it.isPrimary }
                    val promoted =
                        if (removedWasPrimary && remaining.isNotEmpty()) {
                            remaining.mapIndexed { index, photo -> photo.copy(isPrimary = index == 0) }
                        } else {
                            remaining
                        }
                    state.copy(stagedPhotos = promoted)
                }
            }
        }

        private fun onSetPrimaryPhoto(identifier: String) {
            val itemId = editingItemId
            if (itemId != null) {
                viewModelScope.launch { imageRepository.setPrimaryImage(itemId, identifier) }
            } else {
                _uiState.update { state ->
                    state.copy(
                        stagedPhotos = state.stagedPhotos.map { it.copy(isPrimary = it.sourceUri == identifier) },
                    )
                }
            }
        }

        private fun onSave() {
            val state = _uiState.value
            val item =
                ClothingItem(
                    id = editingItemId ?: ClothingId(UUID.randomUUID().toString()),
                    name = state.name,
                    type = state.type,
                    brand = state.brand.ifBlank { null },
                    colorPrimary = state.colorPrimary,
                    colorSecondary = state.colorSecondary.ifBlank { null },
                    condition = state.condition,
                    status = state.status,
                    size =
                        state.sizeLabel.ifBlank { null }?.let { label ->
                            GarmentSize(label = label, system = state.sizeSystem, category = state.sizeCategory)
                        },
                    timesWorn = timesWorn,
                    lastWornAt = lastWornAt,
                    addedAt = addedAt,
                    notes = state.notes.ifBlank { null },
                )

            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }

                if (state.isEditMode) {
                    updateClothingItemUseCase(item)
                } else {
                    addClothingItemUseCase(item)
                }

                var warning: String? = null
                if (!state.isEditMode) {
                    state.stagedPhotos.forEach { staged ->
                        val result = imageRepository.addImage(item.id, staged.sourceUri, staged.isPrimary)
                        if (result is Result.Error) warning = PHOTO_SAVE_WARNING
                    }
                }

                _uiState.update { it.copy(isSaving = false, isSaved = true, saveWarning = warning ?: it.saveWarning) }
            }
        }
    }
