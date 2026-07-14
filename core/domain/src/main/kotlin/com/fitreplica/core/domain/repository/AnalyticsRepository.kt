package com.fitreplica.core.domain.repository

import com.fitreplica.core.model.ClosetAnalytics
import com.fitreplica.core.model.ContextBreakdown
import com.fitreplica.core.model.RepairTime
import com.fitreplica.core.model.WearStreak
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun observeClosetAnalytics(): Flow<ClosetAnalytics>

    fun observeWearStreaks(): Flow<List<WearStreak>>

    fun observeTimeToRepair(): Flow<List<RepairTime>>

    fun observeContextBreakdown(): Flow<List<ContextBreakdown>>
}
