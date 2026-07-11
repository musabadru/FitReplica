package com.fitreplica.core.domain.repository

import com.fitreplica.core.common.Result
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.Image
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    fun observeImages(itemId: ClothingId): Flow<List<Image>>

    // sourceUri is a String, not android.net.Uri, so this interface stays Android-free —
    // the implementation (core:database) owns parsing/copying the platform URI.
    suspend fun addImage(
        itemId: ClothingId,
        sourceUri: String,
        isPrimary: Boolean,
    ): Result<Image>

    suspend fun setPrimaryImage(
        itemId: ClothingId,
        imageId: String,
    )

    suspend fun deleteImage(imageId: String)
}
