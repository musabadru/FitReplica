package com.fitreplica.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fitreplica.core.database.entity.ImageEntity
import com.fitreplica.core.model.ClothingId
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ImageDao {
    @Insert
    abstract suspend fun insertImage(image: ImageEntity)

    @Query("SELECT * FROM images WHERE itemId = :itemId ORDER BY takenAt ASC")
    abstract fun observeImagesForItem(itemId: ClothingId): Flow<List<ImageEntity>>

    @Query("DELETE FROM images WHERE id = :imageId")
    abstract suspend fun deleteImage(imageId: String)
}
