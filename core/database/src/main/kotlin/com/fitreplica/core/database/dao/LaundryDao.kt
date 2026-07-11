package com.fitreplica.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fitreplica.core.database.entity.LaundryLoadEntity
import com.fitreplica.core.database.entity.LaundryLoadItemCrossRef
import com.fitreplica.core.model.LaundryLoadId
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LaundryDao {
    @Insert
    abstract suspend fun insertLoad(load: LaundryLoadEntity)

    @Insert
    abstract suspend fun insertLoadItemCrossRef(crossRef: LaundryLoadItemCrossRef)

    @Query("SELECT * FROM laundry_loads ORDER BY startedAt DESC")
    abstract fun observeLoads(): Flow<List<LaundryLoadEntity>>

    @Query("DELETE FROM laundry_loads WHERE id = :loadId")
    abstract suspend fun deleteLoad(loadId: LaundryLoadId)
}
