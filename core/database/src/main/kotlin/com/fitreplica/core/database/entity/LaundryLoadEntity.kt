package com.fitreplica.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fitreplica.core.model.LaundryLoadId

@Entity(tableName = "laundry_loads")
data class LaundryLoadEntity(
    @PrimaryKey val id: LaundryLoadId,
    val startedAt: Long,
    val completedAt: Long?,
)
