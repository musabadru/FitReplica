package com.fitreplica.core.model

data class ClosetAnalytics(
    val neverWorn: List<ClothingItem>,
    val overRotated: List<ClothingItem>,
    val colorDistribution: Map<String, Int>,
    val typeDistribution: Map<ClothingType, Int>,
    val costPerWear: List<ItemCostPerWear>,
)

data class ItemCostPerWear(
    val itemId: ClothingId,
    val itemName: String,
    val costPerWear: Double,
)

data class WearStreak(
    val itemId: ClothingId,
    val itemName: String,
    val wearCount: Int,
)

data class RepairTime(
    val itemId: ClothingId,
    val itemName: String?,
    val durationMillis: Long,
)

data class ContextBreakdown(
    val context: String,
    val wearCount: Int,
)
