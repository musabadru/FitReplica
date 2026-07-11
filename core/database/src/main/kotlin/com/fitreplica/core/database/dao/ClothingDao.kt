package com.fitreplica.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.fitreplica.core.database.entity.ClothingItemEntity
import com.fitreplica.core.database.entity.WearEventEntity
import com.fitreplica.core.model.ClothingId
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ClothingDao {
    @Insert
    abstract suspend fun insertItem(item: ClothingItemEntity)

    @Query("SELECT * FROM clothing_items WHERE id = :itemId")
    abstract fun observeItem(itemId: ClothingId): Flow<ClothingItemEntity?>

    @Query("SELECT * FROM clothing_items ORDER BY addedAt DESC")
    abstract fun observeItems(): Flow<List<ClothingItemEntity>>

    /**
     * Current-state counter and event-history insert happen in one transaction
     * so the two can never drift out of sync.
     */
    @Transaction
    open suspend fun logWear(itemId: ClothingId, event: WearEventEntity) {
        insertWearEvent(event)
        updateLastWorn(itemId, event.dateTime)
    }

    @Insert
    abstract suspend fun insertWearEvent(event: WearEventEntity)

    @Query("UPDATE clothing_items SET lastWornAt = :wornAt, timesWorn = timesWorn + 1 WHERE id = :itemId")
    abstract suspend fun updateLastWorn(itemId: ClothingId, wornAt: Long)
}
