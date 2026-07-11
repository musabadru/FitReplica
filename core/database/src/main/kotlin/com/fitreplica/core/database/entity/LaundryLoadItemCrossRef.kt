package com.fitreplica.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.LaundryLoadId

@Entity(
    tableName = "laundry_load_item_cross_ref",
    primaryKeys = ["loadId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = LaundryLoadEntity::class,
            parentColumns = ["id"],
            childColumns = ["loadId"],
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
data class LaundryLoadItemCrossRef(
    val loadId: LaundryLoadId,
    val itemId: ClothingId,
)
