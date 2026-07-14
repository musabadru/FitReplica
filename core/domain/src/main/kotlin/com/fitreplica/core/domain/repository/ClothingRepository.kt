package com.fitreplica.core.domain.repository

import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.ConditionEvent
import com.fitreplica.core.model.OutfitId
import kotlinx.coroutines.flow.Flow

interface ClothingRepository {
    fun observeItems(filter: ClosetFilter = ClosetFilter()): Flow<List<ClothingItem>>

    fun observeItem(itemId: ClothingId): Flow<ClothingItem?>

    suspend fun addItem(item: ClothingItem)

    suspend fun updateItem(item: ClothingItem)

    suspend fun deleteItem(itemId: ClothingId)

    suspend fun logWear(
        itemId: ClothingId,
        outfitId: OutfitId?,
        context: String?,
    )

    suspend fun updateCondition(
        itemId: ClothingId,
        condition: Condition,
        notes: String? = null,
    )

    fun observeConditionEvents(itemId: ClothingId): Flow<List<ConditionEvent>>
}
