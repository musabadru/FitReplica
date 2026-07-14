package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.repository.LaundryRepository
import com.fitreplica.core.model.LaundryLoadId
import javax.inject.Inject

class CompleteLaundryLoadUseCase
    @Inject
    constructor(
        private val laundryRepository: LaundryRepository,
    ) {
        suspend operator fun invoke(loadId: LaundryLoadId) {
            laundryRepository.completeLoad(loadId)
        }
    }
