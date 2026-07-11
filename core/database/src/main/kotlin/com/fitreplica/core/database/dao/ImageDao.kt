package com.fitreplica.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.fitreplica.core.database.entity.ImageEntity
import com.fitreplica.core.model.ClothingId
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ImageDao {
    // Enforces "at most one primary image per item" at the transaction boundary rather
    // than a schema constraint: SQLite supports partial unique indexes (`WHERE isPrimary`)
    // but Room's @Index annotation has no way to express one, and a hand-written partial
    // index would fail Room's migration schema validation (it can't be derived from the
    // entity's annotations, so the actual vs. expected schema comparison would mismatch).
    @Transaction
    open suspend fun insertImage(image: ImageEntity) {
        if (image.isPrimary) {
            clearPrimaryForItem(image.itemId)
        }
        insert(image)
    }

    @Transaction
    open suspend fun setPrimaryImage(
        itemId: ClothingId,
        imageId: String,
    ) {
        clearPrimaryForItem(itemId)
        markPrimary(imageId)
    }

    @Insert
    protected abstract suspend fun insert(image: ImageEntity)

    @Query("UPDATE images SET isPrimary = 0 WHERE itemId = :itemId AND isPrimary = 1")
    protected abstract suspend fun clearPrimaryForItem(itemId: ClothingId)

    @Query("UPDATE images SET isPrimary = 1 WHERE id = :imageId")
    protected abstract suspend fun markPrimary(imageId: String)

    @Query("SELECT * FROM images WHERE itemId = :itemId ORDER BY takenAt ASC")
    abstract fun observeImagesForItem(itemId: ClothingId): Flow<List<ImageEntity>>

    @Query("DELETE FROM images WHERE id = :imageId")
    abstract suspend fun deleteImage(imageId: String)
}
