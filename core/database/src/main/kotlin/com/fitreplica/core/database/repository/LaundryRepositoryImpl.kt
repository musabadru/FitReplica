package com.fitreplica.core.database.repository

import com.fitreplica.core.database.dao.LaundryDao
import com.fitreplica.core.database.entity.LaundryLoadEntity
import com.fitreplica.core.database.entity.LaundryLoadWithItems
import com.fitreplica.core.domain.repository.LaundryRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.LaundryLoad
import com.fitreplica.core.model.LaundryLoadId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class LaundryRepositoryImpl
    @Inject
    constructor(
        private val laundryDao: LaundryDao,
    ) : LaundryRepository {
        override fun observeLoads(): Flow<List<LaundryLoad>> =
            laundryDao.observeLoadsWithItems().map { loads -> loads.map { it.toDomain() } }

        override suspend fun createLoad(itemIds: List<ClothingId>) {
            laundryDao.createLoad(
                load =
                    LaundryLoadEntity(
                        id = LaundryLoadId(UUID.randomUUID().toString()),
                        startedAt = System.currentTimeMillis(),
                        completedAt = null,
                    ),
                itemIds = itemIds,
            )
        }

        override suspend fun completeLoad(loadId: LaundryLoadId) {
            laundryDao.completeLoad(loadId, System.currentTimeMillis())
        }
    }

private fun LaundryLoadWithItems.toDomain(): LaundryLoad =
    LaundryLoad(
        id = load.id,
        startedAt = load.startedAt,
        completedAt = load.completedAt,
        itemIds = items.map { it.id },
    )
