package com.fitreplica.feature.analytics

import com.fitreplica.core.model.ClosetAnalytics
import com.fitreplica.core.model.ContextBreakdown
import com.fitreplica.core.model.OutfitSuggestion
import com.fitreplica.core.model.RepairTime
import com.fitreplica.core.model.WearStreak

data class AnalyticsUiState(
    val analytics: ClosetAnalytics? = null,
    val wearStreaks: List<WearStreak> = emptyList(),
    val repairTimes: List<RepairTime> = emptyList(),
    val contextBreakdown: List<ContextBreakdown> = emptyList(),
    val suggestions: List<OutfitSuggestion> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
