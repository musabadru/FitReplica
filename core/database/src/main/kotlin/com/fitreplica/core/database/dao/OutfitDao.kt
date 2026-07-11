package com.fitreplica.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fitreplica.core.database.entity.OutfitEntity
import com.fitreplica.core.database.entity.OutfitItemCrossRef
import com.fitreplica.core.model.OutfitId
import kotlinx.coroutines.flow.Flow

@Dao
abstract class OutfitDao {
    @Insert
    abstract suspend fun insertOutfit(outfit: OutfitEntity)

    @Insert
    abstract suspend fun insertOutfitItemCrossRef(crossRef: OutfitItemCrossRef)

    @Query("SELECT * FROM outfits ORDER BY createdAt DESC")
    abstract fun observeOutfits(): Flow<List<OutfitEntity>>

    @Query("DELETE FROM outfits WHERE id = :outfitId")
    abstract suspend fun deleteOutfit(outfitId: OutfitId)
}
