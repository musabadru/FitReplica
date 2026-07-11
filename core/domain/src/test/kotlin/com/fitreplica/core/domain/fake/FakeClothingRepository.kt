package com.fitreplica.core.domain.fake

import com.fitreplica.core.domain.repository.ClosetFilter
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.OutfitId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeClothingRepository : ClothingRepository {
    private val items = MutableStateFlow<List<ClothingItem>>(emptyList())

    val wearLog = mutableListOf<Triple<ClothingId, OutfitId?, String?>>()

    override fun observeItems(filter: ClosetFilter) = items.map { it }

    override fun observeItem(itemId: ClothingId) = items.map { list -> list.find { it.id == itemId } }

    override suspend fun addItem(item: ClothingItem) {
        items.value = items.value + item
    }

    override suspend fun logWear(
        itemId: ClothingId,
        outfitId: OutfitId?,
        context: String?,
    ) {
        wearLog += Triple(itemId, outfitId, context)
    }

    override suspend fun updateCondition(
        itemId: ClothingId,
        condition: Condition,
    ) {
        items.value =
            items.value.map { item ->
                if (item.id == itemId) item.copy(condition = condition) else item
            }
    }
}
