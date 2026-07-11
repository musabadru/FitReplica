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

    // Mirrors ClothingDao's structured-filter + FTS-prefix semantics closely enough that
    // tests exercising ClosetFilter behave the same against the fake as the real DAO.
    override fun observeItems(filter: ClosetFilter) = items.map { list -> list.filter { it.matches(filter) } }

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

private fun ClothingItem.matches(filter: ClosetFilter): Boolean {
    val matchesStructuredFields =
        (filter.type == null || type == filter.type) &&
            (filter.status == null || status == filter.status) &&
            (filter.condition == null || condition == filter.condition) &&
            (filter.brand == null || brand == filter.brand) &&
            (filter.colorPrimary == null || colorPrimary == filter.colorPrimary)
    return matchesStructuredFields && matchesSearchQuery(filter.searchQuery)
}

private fun ClothingItem.matchesSearchQuery(query: String?): Boolean {
    if (query.isNullOrBlank()) return true
    val haystack = listOfNotNull(name, brand, type.name, colorPrimary).joinToString(" ").lowercase()
    val terms = query.trim().lowercase().split(Regex("\\s+"))
    return terms.all { term -> term.isBlank() || term in haystack }
}
