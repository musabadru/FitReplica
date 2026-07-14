package com.fitreplica.feature.analytics

import android.util.Log
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val TAG = "AnalyticsViewModel"
private const val ANALYTICS_ERROR = "Couldn't load analytics. Try again."

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
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
        private val retryRequests = MutableStateFlow(0)

        private val analyticsState =
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
            }

        val uiState =
            retryRequests
                .flatMapLatest {
                    analyticsState.catch { throwable ->
                        Log.w(TAG, "Failed to load analytics", throwable)
                        emit(
                            AnalyticsUiState(
                                isLoading = false,
                                errorMessage = ANALYTICS_ERROR,
                            ),
                        )
                    }
                }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = AnalyticsUiState(),
            )

        fun onRetryClicked() {
            retryRequests.update { it + 1 }
        }
    }
