package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.repository.AnalyticsRepository
import javax.inject.Inject

class GetTimeToRepairUseCase
    @Inject
    constructor(
        private val analyticsRepository: AnalyticsRepository,
    ) {
        operator fun invoke() = analyticsRepository.observeTimeToRepair()
    }
