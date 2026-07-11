package com.fitreplica.feature.closet.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitreplica.core.domain.repository.ClosetFilter
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.domain.usecase.LogWearEventUseCase
import com.fitreplica.core.model.ClothingId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEARCH_DEBOUNCE_MILLIS = 300L
private const val STOP_TIMEOUT_MILLIS = 5_000L

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ClosetViewModel
    @Inject
    constructor(
        private val clothingRepository: ClothingRepository,
        private val logWearEventUseCase: LogWearEventUseCase,
    ) : ViewModel() {
        private val filterState = MutableStateFlow(ClosetFilter())
        private val viewModeState = MutableStateFlow(ClosetViewMode.GRID)

        // Debounced so DB-hitting queries don't re-run on every keystroke/chip tap, while
        // observeItems(filter) does the real filtering on Room's indexed columns/FTS4 —
        // this is what keeps a 500+ item closet snappy instead of filtering in memory.
        val uiState =
            combine(
                filterState
                    .debounce(SEARCH_DEBOUNCE_MILLIS)
                    .flatMapLatest { filter -> clothingRepository.observeItems(filter) },
                filterState,
                viewModeState,
            ) { items, filter, viewMode ->
                ClosetUiState(items = items, filter = filter, viewMode = viewMode, isLoading = false)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = ClosetUiState(),
            )

        fun onAction(action: ClosetUiAction) {
            when (action) {
                is ClosetUiAction.OnSearchQueryChanged ->
                    filterState.update { it.copy(searchQuery = action.query.ifBlank { null }) }

                is ClosetUiAction.OnTypeFilterChanged ->
                    filterState.update { it.copy(type = action.type) }

                is ClosetUiAction.OnStatusFilterChanged ->
                    filterState.update { it.copy(status = action.status) }

                is ClosetUiAction.OnConditionFilterChanged ->
                    filterState.update { it.copy(condition = action.condition) }

                is ClosetUiAction.OnBrandFilterChanged ->
                    filterState.update { it.copy(brand = action.brand) }

                is ClosetUiAction.OnColorFilterChanged ->
                    filterState.update { it.copy(colorPrimary = action.colorPrimary) }

                ClosetUiAction.OnViewModeToggled ->
                    viewModeState.update { current ->
                        if (current == ClosetViewMode.GRID) ClosetViewMode.LIST else ClosetViewMode.GRID
                    }

                is ClosetUiAction.OnWearNowClicked ->
                    logWearNow(action.itemId)
            }
        }

        private fun logWearNow(itemId: ClothingId) {
            viewModelScope.launch { logWearEventUseCase(itemId) }
        }
    }
