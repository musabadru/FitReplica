package com.fitreplica.core.database.repository

import com.fitreplica.core.database.dao.ClothingDao
import com.fitreplica.core.database.entity.ClothingItemEntity
import com.fitreplica.core.database.entity.WearEventEntity
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.WearEventId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClothingRepositoryImpl
    @Inject
    constructor(
        private val clothingDao: ClothingDao,
    ) : ClothingRepository {
        override fun observeItems(): Flow<List<ClothingItem>> =
            clothingDao.observeItems().map { entities -> entities.map { it.toDomain() } }

        override fun observeItem(itemId: ClothingId): Flow<ClothingItem?> =
            clothingDao.observeItem(itemId).map { it?.toDomain() }

        override suspend fun addItem(item: ClothingItem) {
            clothingDao.insertItem(item.toEntity())
        }

        override suspend fun logWear(
            itemId: ClothingId,
            context: String?,
        ) {
            clothingDao.logWear(
                itemId,
                WearEventEntity(
                    id = WearEventId(java.util.UUID.randomUUID().toString()),
                    itemId = itemId,
                    outfitId = null,
                    dateTime = System.currentTimeMillis(),
                    context = context,
                    notes = null,
                ),
            )
        }
    }

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
