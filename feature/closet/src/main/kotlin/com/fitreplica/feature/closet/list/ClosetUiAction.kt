package com.fitreplica.feature.closet.list

import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.Status

sealed interface ClosetUiAction {
    data class OnSearchQueryChanged(val query: String) : ClosetUiAction

    data class OnTypeFilterChanged(val type: ClothingType?) : ClosetUiAction

    data class OnStatusFilterChanged(val status: Status?) : ClosetUiAction

    data class OnConditionFilterChanged(val condition: Condition?) : ClosetUiAction

    data class OnBrandFilterChanged(val brand: String?) : ClosetUiAction

    data class OnColorFilterChanged(val colorPrimary: String?) : ClosetUiAction

    data object OnViewModeToggled : ClosetUiAction

    data class OnWearNowClicked(val itemId: ClothingId) : ClosetUiAction
}
