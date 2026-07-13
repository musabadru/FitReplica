package com.fitreplica.feature.history

import java.time.LocalDate

internal sealed interface HistoryUiAction {
    data class OnModeChanged(val mode: HistoryMode) : HistoryUiAction

    data class OnDateSelected(val date: LocalDate) : HistoryUiAction

    data object OnPreviousMonthClicked : HistoryUiAction

    data object OnNextMonthClicked : HistoryUiAction
}
