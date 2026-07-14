package com.fitreplica.feature.laundry

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitreplica.core.domain.repository.ClosetFilter
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.domain.repository.LaundryRepository
import com.fitreplica.core.domain.usecase.CompleteLaundryLoadUseCase
import com.fitreplica.core.domain.usecase.CreateLaundryLoadUseCase
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.LaundryLoadId
import com.fitreplica.core.model.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val TAG = "LaundryViewModel"
private const val LAUNDRY_ERROR = "Couldn't update laundry. Try again."

@HiltViewModel
class LaundryViewModel
    @Inject
    constructor(
        clothingRepository: ClothingRepository,
        laundryRepository: LaundryRepository,
        private val createLaundryLoadUseCase: CreateLaundryLoadUseCase,
        private val completeLaundryLoadUseCase: CompleteLaundryLoadUseCase,
    ) : ViewModel() {
        private val selectedItemIds = MutableStateFlow<Set<ClothingId>>(emptySet())
        private val errorMessage = MutableStateFlow<String?>(null)

        val uiState =
            combine(
                clothingRepository.observeItems(ClosetFilter(status = Status.DIRTY)),
                laundryRepository.observeLoads(),
                selectedItemIds,
                errorMessage,
            ) { dirtyItems, loads, selectedIds, error ->
                LaundryUiState(
                    dirtyItems = dirtyItems,
                    loads = loads,
                    selectedItemIds = selectedIds,
                    isLoading = false,
                    errorMessage = error,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = LaundryUiState(),
            )

        fun onAction(action: LaundryUiAction) {
            when (action) {
                is LaundryUiAction.OnItemSelectionChanged -> updateSelection(action.itemId, action.selected)
                LaundryUiAction.OnCreateLoadClicked -> createLoad()
                is LaundryUiAction.OnCompleteLoadClicked -> completeLoad(action.loadId)
            }
        }

        private fun updateSelection(
            itemId: ClothingId,
            selected: Boolean,
        ) {
            selectedItemIds.value =
                if (selected) {
                    selectedItemIds.value + itemId
                } else {
                    selectedItemIds.value - itemId
                }
        }

        @Suppress("TooGenericExceptionCaught")
        private fun createLoad() {
            viewModelScope.launch {
                errorMessage.value = null
                try {
                    createLaundryLoadUseCase(selectedItemIds.value.toList())
                    selectedItemIds.value = emptySet()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to create laundry load", e)
                    errorMessage.value = LAUNDRY_ERROR
                }
            }
        }

        @Suppress("TooGenericExceptionCaught")
        private fun completeLoad(loadId: LaundryLoadId) {
            viewModelScope.launch {
                errorMessage.value = null
                try {
                    completeLaundryLoadUseCase(loadId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to complete laundry load $loadId", e)
                    errorMessage.value = LAUNDRY_ERROR
                }
            }
        }
    }
