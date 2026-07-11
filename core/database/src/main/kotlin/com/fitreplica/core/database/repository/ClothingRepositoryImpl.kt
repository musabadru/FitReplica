package com.fitreplica.core.database.repository

import com.fitreplica.core.database.dao.ClothingDao
import com.fitreplica.core.database.entity.ClothingItemEntity
import com.fitreplica.core.database.entity.WearEventEntity
import com.fitreplica.core.domain.repository.ClosetFilter
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.OutfitId
import com.fitreplica.core.model.WearEventId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClothingRepositoryImpl
    @Inject
    constructor(
        private val clothingDao: ClothingDao,
    ) : ClothingRepository {
        override fun observeItems(filter: ClosetFilter): Flow<List<ClothingItem>> {
            val ftsQuery = filter.searchQuery?.toFtsQuery()
            val entities =
                if (ftsQuery.isNullOrBlank()) {
                    clothingDao.observeItemsFiltered(
                        type = filter.type,
                        status = filter.status,
                        condition = filter.condition,
                        brand = filter.brand,
                        colorPrimary = filter.colorPrimary,
                    )
                } else {
                    clothingDao.searchItems(
                        ftsQuery = ftsQuery,
                        type = filter.type,
                        status = filter.status,
                        condition = filter.condition,
                        brand = filter.brand,
                        colorPrimary = filter.colorPrimary,
                    )
                }
            return entities.map { list -> list.map { it.toDomain() } }
        }

        override fun observeItem(itemId: ClothingId): Flow<ClothingItem?> =
            clothingDao.observeItem(itemId).map { it?.toDomain() }

        override suspend fun addItem(item: ClothingItem) {
            clothingDao.insertItem(item.toEntity())
        }

        override suspend fun updateItem(item: ClothingItem) {
            clothingDao.updateItem(item.toEntity())
        }

        override suspend fun deleteItem(itemId: ClothingId) {
            clothingDao.deleteItem(itemId)
        }

        override suspend fun logWear(
            itemId: ClothingId,
            outfitId: OutfitId?,
            context: String?,
        ) {
            clothingDao.logWear(
                itemId,
                WearEventEntity(
                    id = WearEventId(java.util.UUID.randomUUID().toString()),
                    itemId = itemId,
                    outfitId = outfitId,
                    dateTime = System.currentTimeMillis(),
                    context = context,
                    notes = null,
                ),
            )
        }

        override suspend fun updateCondition(
            itemId: ClothingId,
            condition: Condition,
        ) {
            clothingDao.updateCondition(itemId, condition)
        }
    }

// "blue nike jacket" -> "blue* nike* jacket*": each term becomes an FTS4 prefix match
// so partial words find results, matching the free-text search behaviour from §3.1.
// Splits on any run of non-alphanumeric characters rather than just whitespace, so
// punctuation acts as a term separator instead of being deleted in place — "blue-jacket"
// must become "blue* jacket*", not "bluejacket*", since FTS4's default tokenizer indexed
// "blue" and "jacket" as separate tokens in the first place. This also keeps FTS4 syntax
// characters (*, ", (, ), :, etc.) out of the MATCH expression — left unescaped, a term
// like `nike*` or `"blue jacket"` produces a malformed expression that throws
// SQLiteException at query time. Terms that split to nothing (e.g. a search of just
// "***") are dropped; if every term does, the caller falls back to the non-search
// filtered query instead of running an empty MATCH.
private fun String.toFtsQuery(): String =
    split(Regex("[^\\p{L}\\p{N}]+"))
        .filter { it.isNotBlank() }
        .joinToString(separator = " ") { term -> "$term*" }

private fun ClothingItemEntity.toDomain(): ClothingItem =
    ClothingItem(
        id = id,
        name = name,
        type = type,
        brand = brand,
        colorPrimary = colorPrimary,
        colorSecondary = colorSecondary,
        condition = condition,
        status = status,
        size = size,
        timesWorn = timesWorn,
        lastWornAt = lastWornAt,
        addedAt = addedAt,
        sku = sku,
        avatarSlot = avatarSlot,
        purchasePrice = purchasePrice,
        purchaseDate = purchaseDate,
        purchaseLocation = purchaseLocation,
        notes = notes,
    )

private fun ClothingItem.toEntity(): ClothingItemEntity =
    ClothingItemEntity(
        id = id,
        name = name,
        type = type,
        brand = brand,
        colorPrimary = colorPrimary,
        colorSecondary = colorSecondary,
        condition = condition,
        status = status,
        size = size,
        timesWorn = timesWorn,
        lastWornAt = lastWornAt,
        addedAt = addedAt,
        sku = sku,
        avatarSlot = avatarSlot,
        purchasePrice = purchasePrice,
        purchaseDate = purchaseDate,
        purchaseLocation = purchaseLocation,
        notes = notes,
    )
