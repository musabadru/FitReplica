package com.fitreplica.core.database.repository

import com.fitreplica.core.database.dao.ClothingDao
import com.fitreplica.core.database.entity.ClothingItemEntity
import com.fitreplica.core.database.entity.ConditionEventEntity
import com.fitreplica.core.database.entity.WearEventEntity
import com.fitreplica.core.domain.repository.AnalyticsRepository
import com.fitreplica.core.model.ClosetAnalytics
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.ContextBreakdown
import com.fitreplica.core.model.ItemCostPerWear
import com.fitreplica.core.model.RepairTime
import com.fitreplica.core.model.WearStreak
import com.fitreplica.core.model.Condition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val OVER_ROTATED_MIN_WEAR_COUNT = 5

class AnalyticsRepositoryImpl
    @Inject
    constructor(
        private val clothingDao: ClothingDao,
    ) : AnalyticsRepository {
        override fun observeClosetAnalytics(): Flow<ClosetAnalytics> =
            combine(
                clothingDao.observeItems(),
                clothingDao.observeWearEvents(),
                clothingDao.observeConditionEvents(),
            ) { items, wearEvents, _ ->
                val domainItems = items.map { it.toDomain() }
                ClosetAnalytics(
                    neverWorn = domainItems.filter { item -> wearEvents.none { it.itemId == item.id } },
                    overRotated = domainItems.filter { it.timesWorn >= OVER_ROTATED_MIN_WEAR_COUNT },
                    colorDistribution = domainItems.groupingBy { it.colorPrimary }.eachCount(),
                    typeDistribution = domainItems.groupingBy { it.type }.eachCount(),
                    costPerWear =
                        domainItems.mapNotNull { item ->
                            val price = item.purchasePrice ?: return@mapNotNull null
                            val wears = wearEvents.count { it.itemId == item.id }.coerceAtLeast(1)
                            ItemCostPerWear(item.id, item.name, price / wears)
                        },
                )
            }

        override fun observeWearStreaks(): Flow<List<WearStreak>> =
            combine(clothingDao.observeItems(), clothingDao.observeWearEvents()) { items, events ->
                val namesById = items.associate { it.id to it.name }
                events
                    .groupBy { it.itemId }
                    .map { (itemId, itemEvents) ->
                        WearStreak(
                            itemId = itemId,
                            itemName = namesById[itemId].orEmpty(),
                            wearCount = itemEvents.size,
                        )
                    }.sortedByDescending { it.wearCount }
            }

        override fun observeTimeToRepair(): Flow<List<RepairTime>> =
            combine(clothingDao.observeItems(), clothingDao.observeConditionEvents()) { items, events ->
                val namesById = items.associate { it.id to it.name }
                events.groupBy { it.itemId }.flatMap { (itemId, itemEvents) ->
                    itemEvents.sortedBy { it.changedAt }.repairTimes(itemId, namesById[itemId])
                }
            }

        override fun observeContextBreakdown(): Flow<List<ContextBreakdown>> =
            clothingDao.observeWearEvents().map { events ->
                events
                    .groupingBy { it.context?.takeIf(String::isNotBlank) ?: "Unspecified" }
                    .eachCount()
                    .map { (context, count) -> ContextBreakdown(context, count) }
                    .sortedByDescending { it.wearCount }
            }
    }

private fun List<ConditionEventEntity>.repairTimes(
    itemId: ClothingId,
    itemName: String?,
): List<RepairTime> {
    val results = mutableListOf<RepairTime>()
    var repairStartedAt: Long? = null
    forEach { event ->
        if (event.newCondition == Condition.NEEDS_REPAIR) {
            repairStartedAt = event.changedAt
        } else if (event.newCondition == Condition.GOOD && repairStartedAt != null) {
            results += RepairTime(itemId, itemName, event.changedAt - repairStartedAt!!)
            repairStartedAt = null
        }
    }
    return results
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
