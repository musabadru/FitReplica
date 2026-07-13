package com.fitreplica.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.model.WearHistoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        clothingRepository: ClothingRepository,
    ) : ViewModel() {
        private val modeState = MutableStateFlow(HistoryMode.TIMELINE)
        private val visibleMonthState = MutableStateFlow(YearMonth.now())
        private val selectedDateState = MutableStateFlow(LocalDate.now())

        internal val uiState =
            combine(
                clothingRepository.observeWearHistory(),
                modeState,
                visibleMonthState,
                selectedDateState,
            ) { entries, mode, visibleMonth, selectedDate ->
                HistoryUiState(
                    mode = mode,
                    entries = entries,
                    timelineGroups = entries.toTimelineGroups(),
                    visibleMonth = visibleMonth,
                    selectedDate = selectedDate,
                    calendarDays = entries.toCalendarDays(visibleMonth, selectedDate),
                    selectedDayEntries = entries.forDate(selectedDate),
                    isLoading = false,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = HistoryUiState(),
            )

        internal fun onAction(action: HistoryUiAction) {
            when (action) {
                is HistoryUiAction.OnModeChanged -> modeState.value = action.mode
                is HistoryUiAction.OnDateSelected -> {
                    selectedDateState.value = action.date
                    visibleMonthState.value = YearMonth.from(action.date)
                }
                HistoryUiAction.OnPreviousMonthClicked ->
                    visibleMonthState.update { month -> month.minusMonths(1) }
                HistoryUiAction.OnNextMonthClicked ->
                    visibleMonthState.update { month -> month.plusMonths(1) }
            }
        }
    }

private fun List<WearHistoryEntry>.toTimelineGroups(): List<HistoryDayGroup> =
    groupBy { it.localDate() }
        .map { (date, entries) -> HistoryDayGroup(date = date, entries = entries) }

private fun List<WearHistoryEntry>.toCalendarDays(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
): List<HistoryCalendarDay> {
    val countsByDate = groupingBy { it.localDate() }.eachCount()
    return (1..visibleMonth.lengthOfMonth()).map { day ->
        val date = visibleMonth.atDay(day)
        HistoryCalendarDay(
            date = date,
            wearCount = countsByDate[date] ?: 0,
            isSelected = date == selectedDate,
        )
    }
}

private fun List<WearHistoryEntry>.forDate(date: LocalDate): List<WearHistoryEntry> = filter { it.localDate() == date }

private fun WearHistoryEntry.localDate(): LocalDate =
    Instant
        .ofEpochMilli(wornAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
