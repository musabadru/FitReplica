package com.fitreplica.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fitreplica.core.model.OutfitId

@Entity(tableName = "outfits")
data class OutfitEntity(
    @PrimaryKey val id: OutfitId,
    val name: String,
    val tags: List<String>,
    val rating: Int?,
    val createdAt: Long,
)
