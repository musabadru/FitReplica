package com.fitreplica.feature.closet.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitreplica.core.common.Result
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.domain.repository.ImageRepository
import com.fitreplica.core.domain.usecase.DeleteClothingItemUseCase
import com.fitreplica.core.domain.usecase.LogWearEventUseCase
import com.fitreplica.core.model.ClothingId
import com.fitreplica.feature.closet.ITEM_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val PHOTO_SAVE_WARNING = "Couldn't save that photo (storage issue)."

@HiltViewModel
class ItemDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val clothingRepository: ClothingRepository,
        private val imageRepository: ImageRepository,
        private val logWearEventUseCase: LogWearEventUseCase,
        private val deleteClothingItemUseCase: DeleteClothingItemUseCase,
    ) : ViewModel() {
        private val itemId = ClothingId(checkNotNull(savedStateHandle.get<String>(ITEM_ID_ARG)))
        private val isDeletedState = MutableStateFlow(false)
        private val photoWarningState = MutableStateFlow<String?>(null)

        val uiState =
            combine(
                clothingRepository.observeItem(itemId),
                imageRepository.observeImages(itemId),
                isDeletedState,
                photoWarningState,
            ) { item, images, isDeleted, photoWarning ->
                ItemDetailUiState(
                    item = item,
                    images = images,
                    isLoading = false,
                    isDeleted = isDeleted,
                    photoWarning = photoWarning,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = ItemDetailUiState(),
            )

        fun onAction(action: ItemDetailUiAction) {
            when (action) {
                ItemDetailUiAction.OnWearNowClicked ->
                    viewModelScope.launch { logWearEventUseCase(itemId) }

                ItemDetailUiAction.OnDeleteConfirmed ->
                    viewModelScope.launch {
                        deleteClothingItemUseCase(itemId)
                        isDeletedState.value = true
                    }

                is ItemDetailUiAction.OnSetPrimaryImage ->
                    viewModelScope.launch { imageRepository.setPrimaryImage(itemId, action.imageId) }

                is ItemDetailUiAction.OnDeleteImage ->
                    viewModelScope.launch { imageRepository.deleteImage(action.imageId) }

                is ItemDetailUiAction.OnPhotoAdded ->
                    viewModelScope.launch {
                        val isFirstPhoto = uiState.value.images.isEmpty()
                        val result = imageRepository.addImage(itemId, action.sourceUri, isPrimary = isFirstPhoto)
                        if (result is Result.Error) photoWarningState.value = PHOTO_SAVE_WARNING
                    }
            }
        }
    }
