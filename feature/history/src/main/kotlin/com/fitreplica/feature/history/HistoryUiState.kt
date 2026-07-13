package com.fitreplica.feature.history

import com.fitreplica.core.model.WearHistoryEntry
import java.time.LocalDate
import java.time.YearMonth

enum class HistoryMode { TIMELINE, CALENDAR }

internal data class HistoryUiState(
    val mode: HistoryMode = HistoryMode.TIMELINE,
    val entries: List<WearHistoryEntry> = emptyList(),
    val timelineGroups: List<HistoryDayGroup> = emptyList(),
    val visibleMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val calendarDays: List<HistoryCalendarDay> = emptyList(),
    val selectedDayEntries: List<WearHistoryEntry> = emptyList(),
    val isLoading: Boolean = true,
)

internal data class HistoryDayGroup(
    val date: LocalDate,
    val entries: List<WearHistoryEntry>,
)

internal data class HistoryCalendarDay(
    val date: LocalDate,
    val wearCount: Int,
    val isSelected: Boolean,
)
