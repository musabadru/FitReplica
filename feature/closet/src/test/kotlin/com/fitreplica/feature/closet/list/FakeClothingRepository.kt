package com.fitreplica.feature.closet.list

import com.fitreplica.core.domain.repository.ClosetFilter
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.OutfitId
import com.fitreplica.core.model.WearEventId
import com.fitreplica.core.model.WearHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// A minimal local fake — core:domain's own FakeClothingRepository lives in that module's
// test sourceSet and isn't published as a test fixture, so it isn't visible here.
class FakeClothingRepository : ClothingRepository {
    private val items = MutableStateFlow<List<ClothingItem>>(emptyList())
    val wearHistory = MutableStateFlow<List<WearHistoryEntry>>(emptyList())
    val wearLog = mutableListOf<Triple<ClothingId, OutfitId?, String?>>()

    override fun observeItems(filter: ClosetFilter) =
        items.map { list ->
            list.filter { item ->
                (filter.type == null || item.type == filter.type) &&
                    (filter.status == null || item.status == filter.status) &&
                    (filter.condition == null || item.condition == filter.condition) &&
                    (filter.brand == null || item.brand == filter.brand) &&
                    (filter.colorPrimary == null || item.colorPrimary == filter.colorPrimary) &&
                    matchesSearchQuery(item, filter.searchQuery)
            }
        }

    override fun observeItem(itemId: ClothingId) = items.map { list -> list.find { it.id == itemId } }

    override fun observeWearHistory(): Flow<List<WearHistoryEntry>> = wearHistory

    override suspend fun addItem(item: ClothingItem) {
        items.value = items.value + item
    }

    override suspend fun updateItem(item: ClothingItem) {
        items.value = items.value.map { if (it.id == item.id) item else it }
    }

    override suspend fun deleteItem(itemId: ClothingId) {
        items.value = items.value.filterNot { it.id == itemId }
    }

    override suspend fun logWear(
        itemId: ClothingId,
        outfitId: OutfitId?,
        context: String?,
    ) {
        wearLog += Triple(itemId, outfitId, context)
        val item = items.value.firstOrNull { it.id == itemId } ?: return
        wearHistory.value =
            listOf(
                WearHistoryEntry(
                    id = WearEventId("fake-wear-${wearLog.size}"),
                    itemId = itemId,
                    itemName = item.name,
                    itemType = item.type,
                    colorPrimary = item.colorPrimary,
                    outfitId = outfitId,
                    wornAt = wearLog.size.toLong(),
                    context = context,
                    notes = null,
                ),
            ) + wearHistory.value
    }

    override suspend fun updateCondition(
        itemId: ClothingId,
        condition: Condition,
    ) {
        items.value = items.value.map { if (it.id == itemId) it.copy(condition = condition) else it }
    }

    // A simple case-insensitive substring approximation of the real repository's FTS4 prefix
    // matching — not exact (see core:domain's FakeClothingRepository for a closer token-based
    // approximation), but enough for this fake to actually distinguish match/no-match rather
    // than ignoring searchQuery entirely, which would let a broken search filter pass silently.
    private fun matchesSearchQuery(
        item: ClothingItem,
        query: String?,
    ): Boolean {
        if (query.isNullOrBlank()) return true
        val haystack = listOfNotNull(item.name, item.brand, item.colorPrimary).joinToString(" ").lowercase()
        return haystack.contains(query.lowercase())
    }
}
