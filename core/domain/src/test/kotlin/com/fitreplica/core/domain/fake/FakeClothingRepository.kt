package com.fitreplica.core.domain.fake

import com.fitreplica.core.domain.repository.ClosetFilter
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.ConditionEvent
import com.fitreplica.core.model.ConditionEventId
import com.fitreplica.core.model.OutfitId
import com.fitreplica.core.model.WearEventId
import com.fitreplica.core.model.WearHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeClothingRepository : ClothingRepository {
    private val items = MutableStateFlow<List<ClothingItem>>(emptyList())
    private val conditionEvents = MutableStateFlow<List<ConditionEvent>>(emptyList())
    val wearHistory = MutableStateFlow<List<WearHistoryEntry>>(emptyList())

    val wearLog = mutableListOf<Triple<ClothingId, OutfitId?, String?>>()

    // Mirrors ClothingDao's structured-filter + FTS-prefix semantics closely enough that
    // tests exercising ClosetFilter behave the same against the fake as the real DAO.
    override fun observeItems(filter: ClosetFilter) = items.map { list -> list.filter { it.matches(filter) } }

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
        notes: String?,
    ) {
        val current = items.value.firstOrNull { it.id == itemId } ?: return
        if (current.condition == condition) return
        items.value =
            items.value.map { item ->
                if (item.id == itemId) item.copy(condition = condition) else item
            }
        conditionEvents.value =
            listOf(
                ConditionEvent(
                    id = ConditionEventId("fake-condition-${conditionEvents.value.size + 1}"),
                    itemId = itemId,
                    previousCondition = current.condition,
                    newCondition = condition,
                    changedAt = conditionEvents.value.size.toLong(),
                    notes = notes,
                ),
            ) + conditionEvents.value
    }

    override fun observeConditionEvents(itemId: ClothingId) =
        conditionEvents.map { events -> events.filter { it.itemId == itemId } }
}

private fun ClothingItem.matches(filter: ClosetFilter): Boolean {
    val matchesStructuredFields =
        (filter.type == null || type == filter.type) &&
            (filter.status == null || status == filter.status) &&
            (filter.condition == null || condition == filter.condition) &&
            (filter.brand == null || brand == filter.brand) &&
            (filter.colorPrimary == null || colorPrimary == filter.colorPrimary)
    return matchesStructuredFields && matchesSearchQuery(filter.searchQuery)
}

// Mirrors ClothingRepositoryImpl.toFtsQuery's tokenization: punctuation is a term
// separator (not just whitespace), and each query term must be a *prefix* of some
// haystack token — a substring check would accept "lu" matching inside "blue" (a false
// positive FTS4's prefix MATCH would reject) and reject "nike*" (a false negative, since
// the real query strips the `*` before searching).
private fun ClothingItem.matchesSearchQuery(query: String?): Boolean {
    if (query.isNullOrBlank()) return true
    val haystackTokens = tokensOf(listOfNotNull(name, brand, type.name, colorPrimary).joinToString(" "))
    val queryTerms = tokensOf(query)
    return queryTerms.isEmpty() || queryTerms.all { term -> haystackTokens.any { it.startsWith(term) } }
}

private fun tokensOf(text: String): List<String> =
    text.lowercase().split(Regex("[^\\p{L}\\p{N}]+")).filter { it.isNotBlank() }
