package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.repository.AnalyticsRepository
import javax.inject.Inject

class GetWearStreakUseCase
    @Inject
    constructor(
        private val analyticsRepository: AnalyticsRepository,
    ) {
        operator fun invoke() = analyticsRepository.observeWearStreaks()
    }
