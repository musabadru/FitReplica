package com.fitreplica.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitreplica.core.model.ClothingId

// Separated from ClothingItemEntity: an item can have multiple photos, thumbnails,
// and cached compressed versions — a one-to-many relationship, not a single URI column.
@Entity(
    tableName = "images",
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
data class ImageEntity(
    @PrimaryKey val id: String,
    val itemId: ClothingId,
    val uri: String,
    val thumbnailUri: String,
    // At most one row per itemId should have isPrimary = true. Enforced transactionally
    // in ImageDao.insertImage/setPrimaryImage, not by a schema constraint — see ImageDao.
    val isPrimary: Boolean,
    val takenAt: Long,
)
