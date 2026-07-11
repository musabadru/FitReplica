package com.fitreplica.core.domain.repository

import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import kotlinx.coroutines.flow.Flow

interface ClothingRepository {
    fun observeItems(): Flow<List<ClothingItem>>
    fun observeItem(itemId: ClothingId): Flow<ClothingItem?>
    suspend fun addItem(item: ClothingItem)
    suspend fun logWear(itemId: ClothingId, context: String?)
}
