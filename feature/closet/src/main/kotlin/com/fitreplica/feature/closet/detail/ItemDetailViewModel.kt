package com.fitreplica.feature.closet.detail

import android.util.Log
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val PHOTO_SAVE_WARNING = "Couldn't save that photo (storage issue)."
private const val PHOTO_ACTION_ERROR = "Couldn't update that photo. Try again."
private const val DELETE_ERROR = "Couldn't delete this item. Try again."
private const val TAG = "ItemDetailViewModel"

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
        private val deleteErrorState = MutableStateFlow<String?>(null)

        val uiState =
            combine(
                clothingRepository.observeItem(itemId),
                imageRepository.observeImages(itemId),
                isDeletedState,
                photoWarningState,
                deleteErrorState,
            ) { item, images, isDeleted, photoWarning, deleteError ->
                ItemDetailUiState(
                    item = item,
                    images = images,
                    isLoading = false,
                    isDeleted = isDeleted,
                    photoWarning = photoWarning,
                    deleteError = deleteError,
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

                ItemDetailUiAction.OnDeleteConfirmed -> deleteItem()

                is ItemDetailUiAction.OnSetPrimaryImage -> setPrimaryImage(action.imageId)

                is ItemDetailUiAction.OnDeleteImage -> deleteImage(action.imageId)

                is ItemDetailUiAction.OnPhotoAdded -> onPhotoAdded(action.sourceUri)
            }
        }

        @Suppress("TooGenericExceptionCaught")
        private fun deleteItem() {
            viewModelScope.launch {
                deleteErrorState.value = null
                try {
                    deleteClothingItemUseCase(itemId)
                    isDeletedState.value = true
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete item $itemId", e)
                    deleteErrorState.value = DELETE_ERROR
                }
            }
        }

        @Suppress("TooGenericExceptionCaught")
        private fun setPrimaryImage(imageId: String) {
            viewModelScope.launch {
                try {
                    imageRepository.setPrimaryImage(itemId, imageId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to set primary image $imageId for item $itemId", e)
                    photoWarningState.value = PHOTO_ACTION_ERROR
                }
            }
        }

        @Suppress("TooGenericExceptionCaught")
        private fun deleteImage(imageId: String) {
            viewModelScope.launch {
                try {
                    imageRepository.deleteImage(imageId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete image $imageId for item $itemId", e)
                    photoWarningState.value = PHOTO_ACTION_ERROR
                }
            }
        }

        private fun onPhotoAdded(sourceUri: String) {
            viewModelScope.launch {
                // Cleared up front so a success after a prior failure dismisses the stale
                // warning, and a repeated identical failure still re-emits (StateFlow skips
                // re-publishing an unchanged value, so going through null first is required).
                photoWarningState.value = null
                val isFirstPhoto = uiState.value.images.isEmpty()
                val result = imageRepository.addImage(itemId, sourceUri, isPrimary = isFirstPhoto)
                if (result is Result.Error) photoWarningState.value = PHOTO_SAVE_WARNING
            }
        }
    }
