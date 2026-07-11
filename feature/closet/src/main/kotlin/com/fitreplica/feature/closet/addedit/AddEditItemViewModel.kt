package com.fitreplica.feature.closet.addedit

import android.util.Log
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// Distinct from PHOTO_SAVE_WARNING: this fires when adding a photo in edit mode fails
// immediately (no save has just happened), whereas PHOTO_SAVE_WARNING fires only as part of
// a completed item save in add mode — conflating the two wording-wise implied a save problem
// when there wasn't one.
private const val PHOTO_ADD_WARNING = "Couldn't save that photo (storage issue)."
private const val PHOTO_SAVE_WARNING = "Item saved, but one or more photos couldn't be saved (storage issue)."
private const val SAVE_ERROR = "Couldn't save this item. Try again."
private const val TAG = "AddEditItemViewModel"

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

        // Metadata this form doesn't expose fields for. Left null for new items (correct — a
        // new item has no purchase history yet); populated from the loaded item in edit mode
        // so editing a field this form _does_ own never silently wipes one it doesn't.
        // addedAt in particular stays null until populateFrom sets it (edit mode) — computing
        // it eagerly at construction time for add mode would timestamp when the form was
        // *opened*, not when the user actually tapped Save.
        private var addedAt: Long? = null
        private var timesWorn = 0
        private var lastWornAt: Long? = null
        private var sku: String? = null
        private var avatarSlot: String? = null
        private var purchasePrice: Double? = null
        private var purchaseDate: Long? = null
        private var purchaseLocation: String? = null

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
            sku = item.sku
            avatarSlot = item.avatarSlot
            purchasePrice = item.purchasePrice
            purchaseDate = item.purchaseDate
            purchaseLocation = item.purchaseLocation
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

        // Called by the screen once it has acted on isSaved (navigated away), so a
        // configuration change afterwards doesn't re-trigger navigation: the ViewModel
        // survives rotation, so a stale isSaved = true would otherwise still be there the
        // next time the recreated screen's LaunchedEffect(uiState.isSaved) first runs.
        fun consumeSaved() {
            _uiState.update { it.copy(isSaved = false) }
        }

        private fun onPhotoAdded(sourceUri: String) {
            val itemId = editingItemId
            if (itemId != null) {
                viewModelScope.launch {
                    // Cleared up front so a subsequent success dismisses a stale warning, and
                    // a repeated identical failure still re-emits (StateFlow skips publishing
                    // an unchanged value, so going through null first is required).
                    _uiState.update { it.copy(saveWarning = null) }
                    val isFirstPhoto = _uiState.value.existingImages.isEmpty()
                    val result = imageRepository.addImage(itemId, sourceUri, isPrimary = isFirstPhoto)
                    if (result is Result.Error) {
                        _uiState.update { it.copy(saveWarning = PHOTO_ADD_WARNING) }
                    }
                }
            } else {
                _uiState.update { state ->
                    val isFirstPhoto = state.stagedPhotos.isEmpty()
                    val staged =
                        StagedPhoto(id = UUID.randomUUID().toString(), sourceUri = sourceUri, isPrimary = isFirstPhoto)
                    state.copy(stagedPhotos = state.stagedPhotos + staged)
                }
            }
        }

        private fun onPhotoRemoved(identifier: String) {
            if (editingItemId != null) {
                // Primary-photo promotion on delete is handled by ImageRepositoryImpl itself,
                // so edit-mode removal here doesn't need to special-case the primary photo.
                viewModelScope.launch { imageRepository.deleteImage(identifier) }
            } else {
                _uiState.update { state ->
                    val remaining = state.stagedPhotos.filterNot { it.id == identifier }
                    val removedWasPrimary = state.stagedPhotos.any { it.id == identifier && it.isPrimary }
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
                        stagedPhotos = state.stagedPhotos.map { it.copy(isPrimary = it.id == identifier) },
                    )
                }
            }
        }

        @Suppress("TooGenericExceptionCaught")
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
                    addedAt = addedAt ?: System.currentTimeMillis(),
                    sku = sku,
                    avatarSlot = avatarSlot,
                    purchasePrice = purchasePrice,
                    purchaseDate = purchaseDate,
                    purchaseLocation = purchaseLocation,
                    notes = state.notes.ifBlank { null },
                )

            viewModelScope.launch {
                // saveWarning is intentionally left untouched here: in edit mode it may hold
                // an in-flight photo-add failure from a concurrent onPhotoAdded call (itemId
                // != null branch) that has nothing to do with this save of unrelated field
                // edits, and clearing it up front used to erase that warning before the user
                // ever saw it. Add mode's own staged-photo outcome below always fully
                // replaces (not merges into) saveWarning, so it self-corrects each save
                // without needing an earlier clear.
                _uiState.update { it.copy(isSaving = true, saveError = null) }

                try {
                    if (state.isEditMode) {
                        updateClothingItemUseCase(item)
                    } else {
                        addClothingItemUseCase(item)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to save item ${item.id}", e)
                    _uiState.update { it.copy(isSaving = false, saveError = SAVE_ERROR) }
                    return@launch
                }

                var warning: String? = null
                if (!state.isEditMode) {
                    state.stagedPhotos.forEach { staged ->
                        val result = imageRepository.addImage(item.id, staged.sourceUri, staged.isPrimary)
                        if (result is Result.Error) warning = PHOTO_SAVE_WARNING
                    }
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSaved = true,
                        saveWarning = if (state.isEditMode) it.saveWarning else warning,
                    )
                }
            }
        }
    }
