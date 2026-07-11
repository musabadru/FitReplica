package com.fitreplica.core.model

data class ClothingItem(
    val id: ClothingId,
    val name: String,
    val type: ClothingType,
    val brand: String?,
    val colorPrimary: String,
    val colorSecondary: String?,
    val condition: Condition,
    val status: Status,
    val size: GarmentSize?,
    val timesWorn: Int = 0,
    val lastWornAt: Long?,
    val addedAt: Long,
    val sku: String? = null,
    val avatarSlot: String? = null,
    val purchasePrice: Double? = null,
    val purchaseDate: Long? = null,
    val purchaseLocation: String? = null,
    val notes: String? = null,
)
