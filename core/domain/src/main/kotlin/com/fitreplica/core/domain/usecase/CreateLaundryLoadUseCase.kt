package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.repository.LaundryRepository
import com.fitreplica.core.model.ClothingId
import javax.inject.Inject

class CreateLaundryLoadUseCase
    @Inject
    constructor(
        private val laundryRepository: LaundryRepository,
    ) {
        suspend operator fun invoke(itemIds: List<ClothingId>) {
            require(itemIds.isNotEmpty()) { "Laundry load must contain at least one item." }
            laundryRepository.createLoad(itemIds.distinct())
        }
    }
