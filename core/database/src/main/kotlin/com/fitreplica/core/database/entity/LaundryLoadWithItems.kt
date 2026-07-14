package com.fitreplica.core.database.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class LaundryLoadWithItems(
    @Embedded val load: LaundryLoadEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy =
            Junction(
                value = LaundryLoadItemCrossRef::class,
                parentColumn = "loadId",
                entityColumn = "itemId",
            ),
    )
    val items: List<ClothingItemEntity>,
)
