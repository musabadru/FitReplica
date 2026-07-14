package com.fitreplica.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.domain.repository.SuggestionEngine
import com.fitreplica.core.domain.usecase.GetClosetAnalyticsUseCase
import com.fitreplica.core.domain.usecase.GetContextBreakdownUseCase
import com.fitreplica.core.domain.usecase.GetTimeToRepairUseCase
import com.fitreplica.core.domain.usecase.GetWearStreakUseCase
import com.fitreplica.core.model.SuggestionContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class AnalyticsViewModel
    @Inject
    constructor(
        getClosetAnalyticsUseCase: GetClosetAnalyticsUseCase,
        getWearStreakUseCase: GetWearStreakUseCase,
        getTimeToRepairUseCase: GetTimeToRepairUseCase,
        getContextBreakdownUseCase: GetContextBreakdownUseCase,
        clothingRepository: ClothingRepository,
        suggestionEngine: SuggestionEngine,
    ) : ViewModel() {
        private val suggestions =
            clothingRepository.observeItems().map { closet ->
                suggestionEngine.suggest(closet, SuggestionContext())
            }

        val uiState =
            combine(
                getClosetAnalyticsUseCase(),
                getWearStreakUseCase(),
                getTimeToRepairUseCase(),
                getContextBreakdownUseCase(),
                suggestions,
            ) { analytics, streaks, repairs, contexts, suggestions ->
                AnalyticsUiState(
                    analytics = analytics,
                    wearStreaks = streaks,
                    repairTimes = repairs,
                    contextBreakdown = contexts,
                    suggestions = suggestions,
                    isLoading = false,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = AnalyticsUiState(),
            )
    }
