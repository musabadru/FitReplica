package com.fitreplica.feature.closet.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitreplica.core.domain.repository.ClosetFilter
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.domain.usecase.LogWearEventUseCase
import com.fitreplica.core.model.ClothingId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
        // Kept separate from filterState: only free-text search needs debouncing to avoid
        // re-querying on every keystroke. Chip taps (type/status/condition) are discrete,
        // low-frequency events that should apply immediately — debouncing them too meant the
        // displayed filter (read from the un-debounced state) briefly disagreed with the
        // still-stale item list for up to SEARCH_DEBOUNCE_MILLIS after every chip tap.
        private val searchQueryState = MutableStateFlow("")
        private val filterState = MutableStateFlow(ClosetFilter())
        private val viewModeState = MutableStateFlow(ClosetViewMode.GRID)

        private val effectiveFilter =
            combine(searchQueryState.debounce(SEARCH_DEBOUNCE_MILLIS), filterState) { query, filter ->
                filter.copy(searchQuery = query.ifBlank { null })
            }

        val uiState =
            combine(
                effectiveFilter.flatMapLatest { filter -> clothingRepository.observeItems(filter) },
                searchQueryState,
                filterState,
                viewModeState,
            ) { items, searchQuery, filter, viewMode ->
                ClosetUiState(
                    items = items,
                    searchQuery = searchQuery,
                    filter = filter,
                    viewMode = viewMode,
                    isLoading = false,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = ClosetUiState(),
            )

        fun onAction(action: ClosetUiAction) {
            when (action) {
                is ClosetUiAction.OnSearchQueryChanged ->
                    searchQueryState.value = action.query

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
            viewModelScope.launch {
                try {
                    logWearEventUseCase(itemId)
                } catch (e: CancellationException) {
                    throw e
                } catch (ignored: Exception) {
                    // Non-critical: a failed wear-event log shouldn't crash the app. There's
                    // no dedicated UI surface for this on the list screen (unlike photo/save
                    // failures elsewhere) — the user can simply retry "Wear now".
                }
            }
        }
    }
