package com.fitreplica.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.fitreplica.core.database.entity.LaundryLoadEntity
import com.fitreplica.core.database.entity.LaundryLoadItemCrossRef
import com.fitreplica.core.database.entity.LaundryLoadWithItems
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.LaundryLoadId
import com.fitreplica.core.model.Status
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LaundryDao {
    @Insert
    abstract suspend fun insertLoad(load: LaundryLoadEntity)

    @Insert
    abstract suspend fun insertLoadItemCrossRef(crossRef: LaundryLoadItemCrossRef)

    @Query("SELECT * FROM laundry_loads ORDER BY startedAt DESC")
    abstract fun observeLoads(): Flow<List<LaundryLoadEntity>>

    @Transaction
    @Query("SELECT * FROM laundry_loads ORDER BY startedAt DESC")
    abstract fun observeLoadsWithItems(): Flow<List<LaundryLoadWithItems>>

    @Query("DELETE FROM laundry_loads WHERE id = :loadId")
    abstract suspend fun deleteLoad(loadId: LaundryLoadId)

    @Transaction
    open suspend fun createLoad(
        load: LaundryLoadEntity,
        itemIds: List<ClothingId>,
    ) {
        insertLoad(load)
        itemIds.forEach { itemId -> insertLoadItemCrossRef(LaundryLoadItemCrossRef(load.id, itemId)) }
        updateItemStatus(itemIds, Status.IN_LAUNDRY)
    }

    @Transaction
    open suspend fun completeLoad(
        loadId: LaundryLoadId,
        completedAt: Long,
    ) {
        completeLoadOnly(loadId, completedAt)
        updateItemStatus(getLoadItemIds(loadId), Status.CLEAN)
    }

    @Query("UPDATE laundry_loads SET completedAt = :completedAt WHERE id = :loadId")
    abstract suspend fun completeLoadOnly(
        loadId: LaundryLoadId,
        completedAt: Long,
    )

    @Query("SELECT itemId FROM laundry_load_item_cross_ref WHERE loadId = :loadId")
    abstract suspend fun getLoadItemIds(loadId: LaundryLoadId): List<ClothingId>

    @Query("UPDATE clothing_items SET status = :status WHERE id IN (:itemIds)")
    abstract suspend fun updateItemStatus(
        itemIds: List<ClothingId>,
        status: Status,
    )
}
