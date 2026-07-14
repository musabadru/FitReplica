package com.fitreplica.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.ConditionEventId

@Entity(
    tableName = "condition_events",
    foreignKeys = [
        ForeignKey(
            entity = ClothingItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["itemId", "changedAt", "id"]),
        Index(value = ["changedAt", "id"]),
    ],
)
data class ConditionEventEntity(
    @PrimaryKey val id: ConditionEventId,
    val itemId: ClothingId,
    val previousCondition: Condition,
    val newCondition: Condition,
    val changedAt: Long,
    val notes: String?,
)
