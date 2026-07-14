package com.fitreplica.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.model.WearHistoryEntry
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        clothingRepository: ClothingRepository,
    ) : ViewModel() {
        private val modeState = MutableStateFlow(HistoryMode.TIMELINE)
        private val visibleMonthState = MutableStateFlow(YearMonth.now())
        private val selectedDateState = MutableStateFlow(LocalDate.now())
        private val retryState = MutableStateFlow(0)
        private val historyResult =
            retryState.flatMapLatest {
                clothingRepository
                    .observeWearHistory()
                    .map<List<WearHistoryEntry>, WearHistoryResult> { entries ->
                        WearHistoryResult.Success(entries = entries, projection = entries.toProjection())
                    }
                    .catch { emit(WearHistoryResult.Error) }
            }

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
                        HistoryUiState(
                            mode = mode,
                            entries = result.entries,
                            timelineGroups = result.projection.timelineGroups,
                            visibleMonth = visibleMonth,
                            selectedDate = selectedDate,
                            calendarLeadingBlankCount = visibleMonth.leadingBlankCount(),
                            calendarDays = result.projection.toCalendarDays(visibleMonth, selectedDate),
                            selectedDayEntries = result.projection.entriesForDate(selectedDate),
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
                HistoryUiAction.OnRetryClicked ->
                    retryState.update { retryCount -> retryCount.inc() }
            }
        }

        private fun moveVisibleMonthBy(months: Long) {
            val targetMonth = visibleMonthState.value.plusMonths(months)
            visibleMonthState.value = targetMonth
            selectedDateState.update { date -> date.withYearMonth(targetMonth) }
        }
    }

private sealed interface WearHistoryResult {
    data class Success(
        val entries: List<WearHistoryEntry>,
        val projection: WearHistoryProjection,
    ) : WearHistoryResult

    data object Error : WearHistoryResult
}

private data class DatedWearHistoryEntry(
    val entry: WearHistoryEntry,
    val date: LocalDate,
)

private data class WearHistoryProjection(
    val timelineGroups: List<HistoryDayGroup>,
    val wearCountsByDate: Map<LocalDate, Int>,
    val entriesByDate: Map<LocalDate, List<WearHistoryEntry>>,
)

private fun List<WearHistoryEntry>.toProjection(): WearHistoryProjection {
    val datedEntries = toDatedEntries()
    val entriesByDate = datedEntries.groupBy(keySelector = { it.date }, valueTransform = { it.entry })
    return WearHistoryProjection(
        timelineGroups =
            entriesByDate.map { (date, entries) ->
                HistoryDayGroup(date = date, entries = entries)
            },
        wearCountsByDate = entriesByDate.mapValues { (_, entries) -> entries.size },
        entriesByDate = entriesByDate,
    )
}

private fun List<WearHistoryEntry>.toDatedEntries(): List<DatedWearHistoryEntry> =
    map { entry -> DatedWearHistoryEntry(entry = entry, date = entry.localDate()) }

private fun WearHistoryProjection.toCalendarDays(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
): List<HistoryCalendarDay> {
    return (1..visibleMonth.lengthOfMonth()).map { day ->
        val date = visibleMonth.atDay(day)
        HistoryCalendarDay(
            date = date,
            wearCount = wearCountsByDate[date] ?: 0,
            isSelected = date == selectedDate,
        )
    }
}

private fun WearHistoryProjection.entriesForDate(date: LocalDate): List<WearHistoryEntry> =
    entriesByDate[date].orEmpty()

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
