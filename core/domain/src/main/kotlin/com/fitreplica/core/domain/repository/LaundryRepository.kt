package com.fitreplica.core.domain.repository

import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.LaundryLoad
import com.fitreplica.core.model.LaundryLoadId
import kotlinx.coroutines.flow.Flow

interface LaundryRepository {
    fun observeLoads(): Flow<List<LaundryLoad>>

    suspend fun createLoad(itemIds: List<ClothingId>)

    suspend fun completeLoad(loadId: LaundryLoadId)
}
