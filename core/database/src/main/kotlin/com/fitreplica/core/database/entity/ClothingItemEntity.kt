package com.fitreplica.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.GarmentSize
import com.fitreplica.core.model.Status

@Entity(
    tableName = "clothing_items",
    indices = [
        Index("type"),
        Index("brand"),
        Index("colorPrimary"),
        Index("status"),
    ],
)
data class ClothingItemEntity(
    @PrimaryKey val id: ClothingId,
    val name: String,
    val type: ClothingType,
    val brand: String?,
    val colorPrimary: String,
    val colorSecondary: String?,
    val condition: Condition,
    val status: Status,
    val timesWorn: Int = 0,
    val lastWornAt: Long?,
    val addedAt: Long,
    val sku: String? = null,
    val avatarSlot: String? = null,
    val purchasePrice: Double? = null,
    val purchaseDate: Long? = null,
    val purchaseLocation: String? = null,
    val notes: String? = null,
    @Embedded(prefix = "size_") val size: GarmentSize?,
)
