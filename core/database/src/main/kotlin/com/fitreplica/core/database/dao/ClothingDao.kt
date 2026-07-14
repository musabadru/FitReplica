package com.fitreplica.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fitreplica.core.database.entity.ClothingItemEntity
import com.fitreplica.core.database.entity.ConditionEventEntity
import com.fitreplica.core.database.entity.WearEventEntity
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.Status
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ClothingDao {
    @Insert
    abstract suspend fun insertItem(item: ClothingItemEntity)

    @Update
    abstract suspend fun updateItem(item: ClothingItemEntity)

    // Cascades to wear_events/images/outfit cross-refs via their existing FKs.
    @Query("DELETE FROM clothing_items WHERE id = :itemId")
    abstract suspend fun deleteItem(itemId: ClothingId)

    @Query("SELECT * FROM clothing_items WHERE id = :itemId")
    abstract fun observeItem(itemId: ClothingId): Flow<ClothingItemEntity?>

    @Query("SELECT * FROM clothing_items ORDER BY addedAt DESC")
    abstract fun observeItems(): Flow<List<ClothingItemEntity>>

    @Query(
        """
        SELECT
            wear_events.id AS id,
            wear_events.itemId AS itemId,
            wear_events.itemName AS itemName,
            wear_events.itemType AS itemType,
            wear_events.colorPrimary AS colorPrimary,
            wear_events.outfitId AS outfitId,
            wear_events.dateTime AS wornAt,
            wear_events.context AS context,
            wear_events.notes AS notes
        FROM wear_events
        ORDER BY wear_events.dateTime DESC
        """,
    )
    abstract fun observeWearHistory(): Flow<List<WearHistoryRow>>

    // Structured filters only — combined with FTS search in searchItems below.
    // Nullable-bind pattern (`:x IS NULL OR column = :x`) lets one query serve every
    // filter combination without building a query at runtime.
    @Query(
        """
        SELECT * FROM clothing_items
        WHERE (:type IS NULL OR type = :type)
        AND (:status IS NULL OR status = :status)
        AND (:condition IS NULL OR condition = :condition)
        AND (:brand IS NULL OR brand = :brand)
        AND (:colorPrimary IS NULL OR colorPrimary = :colorPrimary)
        ORDER BY addedAt DESC
        """,
    )
    abstract fun observeItemsFiltered(
        type: ClothingType?,
        status: Status?,
        condition: Condition?,
        brand: String?,
        colorPrimary: String?,
    ): Flow<List<ClothingItemEntity>>

    // `ftsQuery` is pre-formatted FTS4 MATCH syntax (e.g. "blue* nike*") — see
    // ClosetFilter/ClothingRepositoryImpl for how free text becomes prefix terms.
    // Room DAO methods bind each SQL parameter as its own function parameter, so
    // this can't be collapsed into a filter object the way a regular function could.
    @Suppress("LongParameterList")
    @Query(
        """
        SELECT clothing_items.* FROM clothing_items
        JOIN clothing_items_fts ON clothing_items.rowid = clothing_items_fts.rowid
        WHERE clothing_items_fts MATCH :ftsQuery
        AND (:type IS NULL OR clothing_items.type = :type)
        AND (:status IS NULL OR clothing_items.status = :status)
        AND (:condition IS NULL OR clothing_items.condition = :condition)
        AND (:brand IS NULL OR clothing_items.brand = :brand)
        AND (:colorPrimary IS NULL OR clothing_items.colorPrimary = :colorPrimary)
        ORDER BY clothing_items.addedAt DESC
        """,
    )
    abstract fun searchItems(
        ftsQuery: String,
        type: ClothingType?,
        status: Status?,
        condition: Condition?,
        brand: String?,
        colorPrimary: String?,
    ): Flow<List<ClothingItemEntity>>

    @Query("UPDATE clothing_items SET condition = :condition WHERE id = :itemId")
    abstract suspend fun updateConditionOnly(
        itemId: ClothingId,
        condition: Condition,
    )

    @Query("SELECT condition FROM clothing_items WHERE id = :itemId")
    abstract suspend fun getCondition(itemId: ClothingId): Condition?

    @Insert
    abstract suspend fun insertConditionEvent(event: ConditionEventEntity)

    @Transaction
    open suspend fun updateCondition(
        itemId: ClothingId,
        event: ConditionEventEntity,
    ): Boolean {
        val previous = getCondition(itemId) ?: return false
        if (previous == event.newCondition) return false
        insertConditionEvent(event.copy(previousCondition = previous))
        updateConditionOnly(itemId, event.newCondition)
        return true
    }

    @Query("SELECT * FROM condition_events WHERE itemId = :itemId ORDER BY changedAt DESC, id DESC")
    abstract fun observeConditionEvents(itemId: ClothingId): Flow<List<ConditionEventEntity>>

    @Query("SELECT * FROM condition_events ORDER BY changedAt ASC, id ASC")
    abstract fun observeConditionEvents(): Flow<List<ConditionEventEntity>>

    /**
     * Current-state counter and event-history insert happen in one transaction
     * so the two can never drift out of sync.
     */
    @Transaction
    open suspend fun logWear(
        itemId: ClothingId,
        event: WearEventEntity,
    ) {
        val snapshot = requireNotNull(wearItemSnapshot(itemId))
        insertWearEvent(
            event.copy(
                itemId = itemId,
                itemName = snapshot.itemName,
                itemType = snapshot.itemType,
                colorPrimary = snapshot.colorPrimary,
            ),
        )
        updateLastWorn(itemId, event.dateTime)
    }

    @Query(
        """
        SELECT name AS itemName, type AS itemType, colorPrimary AS colorPrimary
        FROM clothing_items
        WHERE id = :itemId
        """,
    )
    internal abstract suspend fun wearItemSnapshot(itemId: ClothingId): WearItemSnapshot?

    @Insert
    abstract suspend fun insertWearEvent(event: WearEventEntity)

    @Query("SELECT * FROM wear_events ORDER BY dateTime ASC, id ASC")
    abstract fun observeWearEvents(): Flow<List<WearEventEntity>>

    @Query(
        """
        UPDATE clothing_items
        SET lastWornAt = :wornAt, timesWorn = timesWorn + 1, status = 'DIRTY'
        WHERE id = :itemId
        """,
    )
    abstract suspend fun updateLastWorn(
        itemId: ClothingId,
        wornAt: Long,
    )
}

internal data class WearItemSnapshot(
    val itemName: String,
    val itemType: ClothingType,
    val colorPrimary: String,
)
