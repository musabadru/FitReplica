package com.fitreplica.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.model.WearHistoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val DAYS_IN_WEEK = 7
private const val HISTORY_ERROR_MESSAGE = "Unable to load wear history. Please try again."

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        clothingRepository: ClothingRepository,
    ) : ViewModel() {
        private val modeState = MutableStateFlow(HistoryMode.TIMELINE)
        private val visibleMonthState = MutableStateFlow(YearMonth.now())
        private val selectedDateState = MutableStateFlow(LocalDate.now())
        private val historyResult =
            clothingRepository
                .observeWearHistory()
                .map<List<WearHistoryEntry>, WearHistoryResult> { WearHistoryResult.Success(it) }
                .catch { emit(WearHistoryResult.Error) }

        internal val uiState =
            combine(
                historyResult,
                modeState,
                visibleMonthState,
                selectedDateState,
            ) { result, mode, visibleMonth, selectedDate ->
                when (result) {
                    WearHistoryResult.Error ->
                        HistoryUiState(
                            mode = mode,
                            visibleMonth = visibleMonth,
                            selectedDate = selectedDate,
                            calendarLeadingBlankCount = visibleMonth.leadingBlankCount(),
                            isLoading = false,
                            errorMessage = HISTORY_ERROR_MESSAGE,
                        )
                    is WearHistoryResult.Success -> {
                        val datedEntries = result.entries.toDatedEntries()
                        HistoryUiState(
                            mode = mode,
                            entries = result.entries,
                            timelineGroups = datedEntries.toTimelineGroups(),
                            visibleMonth = visibleMonth,
                            selectedDate = selectedDate,
                            calendarLeadingBlankCount = visibleMonth.leadingBlankCount(),
                            calendarDays = datedEntries.toCalendarDays(visibleMonth, selectedDate),
                            selectedDayEntries = datedEntries.forDate(selectedDate),
                            isLoading = false,
                        )
                    }
                }
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
                    moveVisibleMonthBy(-1)
                HistoryUiAction.OnNextMonthClicked ->
                    moveVisibleMonthBy(1)
            }
        }

        private fun moveVisibleMonthBy(months: Long) {
            val targetMonth = visibleMonthState.value.plusMonths(months)
            visibleMonthState.value = targetMonth
            selectedDateState.update { date -> date.withYearMonth(targetMonth) }
        }
    }

private sealed interface WearHistoryResult {
    data class Success(val entries: List<WearHistoryEntry>) : WearHistoryResult

    data object Error : WearHistoryResult
}

private data class DatedWearHistoryEntry(
    val entry: WearHistoryEntry,
    val date: LocalDate,
)

private fun List<WearHistoryEntry>.toDatedEntries(): List<DatedWearHistoryEntry> =
    map { entry -> DatedWearHistoryEntry(entry = entry, date = entry.localDate()) }

private fun List<DatedWearHistoryEntry>.toTimelineGroups(): List<HistoryDayGroup> =
    groupBy { it.date }
        .map { (date, entries) -> HistoryDayGroup(date = date, entries = entries.map { it.entry }) }

private fun List<DatedWearHistoryEntry>.toCalendarDays(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
): List<HistoryCalendarDay> {
    val countsByDate = groupingBy { it.date }.eachCount()
    return (1..visibleMonth.lengthOfMonth()).map { day ->
        val date = visibleMonth.atDay(day)
        HistoryCalendarDay(
            date = date,
            wearCount = countsByDate[date] ?: 0,
            isSelected = date == selectedDate,
        )
    }
}

private fun List<DatedWearHistoryEntry>.forDate(date: LocalDate): List<WearHistoryEntry> =
    filter { it.date == date }.map { it.entry }

private fun WearHistoryEntry.localDate(): LocalDate =
    Instant
        .ofEpochMilli(wornAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

private fun LocalDate.withYearMonth(yearMonth: YearMonth): LocalDate =
    yearMonth.atDay(dayOfMonth.coerceAtMost(yearMonth.lengthOfMonth()))

private fun YearMonth.leadingBlankCount(): Int {
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    return (atDay(1).dayOfWeek.value - firstDayOfWeek.value + DAYS_IN_WEEK) % DAYS_IN_WEEK
}
