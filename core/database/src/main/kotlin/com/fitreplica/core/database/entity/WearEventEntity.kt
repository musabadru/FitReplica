package com.fitreplica.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.OutfitId
import com.fitreplica.core.model.WearEventId

@Entity(
    tableName = "wear_events",
    foreignKeys = [
        ForeignKey(
            entity = ClothingItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("itemId")],
)
data class WearEventEntity(
    @PrimaryKey val id: WearEventId,
    val itemId: ClothingId,
    val outfitId: OutfitId?,
    val dateTime: Long,
    val context: String?,
    val notes: String?,
)
