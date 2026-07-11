package com.fitreplica.core.model

data class Image(
    val id: String,
    val itemId: ClothingId,
    val uri: String,
    val thumbnailUri: String,
    val isPrimary: Boolean,
    val takenAt: Long,
)
