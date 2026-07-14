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
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.LaundryLoadId
import com.fitreplica.core.model.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
        private val isCreatingLoad = MutableStateFlow(false)
        private val completingLoadIds = MutableStateFlow<Set<LaundryLoadId>>(emptySet())
        private val errorMessage = MutableStateFlow<String?>(null)
        private val dirtyItems =
            clothingRepository.observeItems(ClosetFilter(status = Status.DIRTY))
                .map { items -> items.filterNot { it.condition == Condition.RETIRED } }
        private val eligibleDirtyItemIds = dirtyItems.map { items -> items.mapTo(mutableSetOf()) { it.id } }
        private val visibleSelectedItemIds =
            combine(selectedItemIds, eligibleDirtyItemIds) { selectedIds, eligibleIds ->
                selectedIds.intersect(eligibleIds)
            }
        private val progressState =
            combine(isCreatingLoad, completingLoadIds) { isCreating, completingIds ->
                LaundryProgressState(isCreatingLoad = isCreating, completingLoadIds = completingIds)
            }

        val uiState =
            combine(
                dirtyItems,
                laundryRepository.observeLoads(),
                visibleSelectedItemIds,
                progressState,
                errorMessage,
            ) { dirtyItems, loads, selectedIds, progress, error ->
                LaundryUiState(
                    dirtyItems = dirtyItems,
                    loads = loads,
                    selectedItemIds = selectedIds,
                    completingLoadIds = progress.completingLoadIds,
                    isLoading = false,
                    isCreatingLoad = progress.isCreatingLoad,
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
                LaundryUiAction.OnErrorShown -> errorMessage.value = null
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
            if (isCreatingLoad.value) return
            val eligibleIds = uiState.value.dirtyItems.mapTo(mutableSetOf()) { it.id }
            val itemIds = selectedItemIds.value.intersect(eligibleIds)
            if (itemIds.isEmpty()) {
                selectedItemIds.value = emptySet()
                return
            }
            viewModelScope.launch {
                errorMessage.value = null
                isCreatingLoad.value = true
                try {
                    createLaundryLoadUseCase(itemIds.toList())
                    selectedItemIds.value = emptySet()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to create laundry load", e)
                    errorMessage.value = LAUNDRY_ERROR
                } finally {
                    isCreatingLoad.value = false
                }
            }
        }

        @Suppress("TooGenericExceptionCaught")
        private fun completeLoad(loadId: LaundryLoadId) {
            if (loadId in completingLoadIds.value) return
            viewModelScope.launch {
                errorMessage.value = null
                completingLoadIds.value = completingLoadIds.value + loadId
                try {
                    completeLaundryLoadUseCase(loadId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to complete laundry load $loadId", e)
                    errorMessage.value = LAUNDRY_ERROR
                } finally {
                    completingLoadIds.value = completingLoadIds.value - loadId
                }
            }
        }
    }

private data class LaundryProgressState(
    val isCreatingLoad: Boolean,
    val completingLoadIds: Set<LaundryLoadId>,
)
