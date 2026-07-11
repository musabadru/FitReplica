package com.fitreplica.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.OutfitId

@Entity(
    tableName = "outfit_item_cross_ref",
    primaryKeys = ["outfitId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = OutfitEntity::class,
            parentColumns = ["id"],
            childColumns = ["outfitId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ClothingItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("itemId")],
)
data class OutfitItemCrossRef(
    val outfitId: OutfitId,
    val itemId: ClothingId,
    val position: Int,
)
