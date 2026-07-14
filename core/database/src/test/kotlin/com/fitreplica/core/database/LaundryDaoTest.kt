package com.fitreplica.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fitreplica.core.database.entity.ClothingItemEntity
import com.fitreplica.core.database.entity.LaundryLoadEntity
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.LaundryLoadId
import com.fitreplica.core.model.Status
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LaundryDaoTest {
    private lateinit var database: AppDatabase

    @Before
    fun createDb() {
        database =
            Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun `createLoad inserts crossrefs and moves items into laundry`() =
        runTest {
            val itemIds = listOf(ClothingId("item-1"), ClothingId("item-2"))
            itemIds.forEach { insertItem(it, Status.DIRTY) }

            database.laundryDao().createLoad(
                load = LaundryLoadEntity(LaundryLoadId("load-1"), startedAt = 1L, completedAt = null),
                itemIds = itemIds,
            )

            val load = database.laundryDao().observeLoadsWithItems().first().single()
            assertEquals(itemIds, load.items.map { it.id })
            itemIds.forEach { itemId ->
                assertEquals(Status.IN_LAUNDRY, database.clothingDao().observeItem(itemId).first()?.status)
            }
        }

    @Test
    fun `completeLoad sets completedAt and moves items clean`() =
        runTest {
            val itemId = ClothingId("item-1")
            insertItem(itemId, Status.DIRTY)
            database.laundryDao().createLoad(
                load = LaundryLoadEntity(LaundryLoadId("load-1"), startedAt = 1L, completedAt = null),
                itemIds = listOf(itemId),
            )

            database.laundryDao().completeLoad(LaundryLoadId("load-1"), completedAt = 2L)

            val load = database.laundryDao().observeLoadsWithItems().first().single()
            assertEquals(2L, load.load.completedAt)
            assertEquals(Status.CLEAN, database.clothingDao().observeItem(itemId).first()?.status)
        }

    @Test
    fun `completeLoad leaves items dirty if they changed after load started`() =
        runTest {
            val itemId = ClothingId("item-1")
            insertItem(itemId, Status.DIRTY)
            database.laundryDao().createLoad(
                load = LaundryLoadEntity(LaundryLoadId("load-1"), startedAt = 1L, completedAt = null),
                itemIds = listOf(itemId),
            )
            database.laundryDao().updateItemStatus(listOf(itemId), Status.DIRTY)

            database.laundryDao().completeLoad(LaundryLoadId("load-1"), completedAt = 2L)

            assertEquals(Status.DIRTY, database.clothingDao().observeItem(itemId).first()?.status)
        }

    private suspend fun insertItem(
        itemId: ClothingId,
        status: Status,
    ) {
        database.clothingDao().insertItem(
            ClothingItemEntity(
                id = itemId,
                name = itemId.value,
                type = ClothingType.TOP,
                brand = null,
                colorPrimary = "blue",
                colorSecondary = null,
                condition = Condition.NEW,
                status = status,
                timesWorn = 0,
                lastWornAt = null,
                addedAt = 0L,
                size = null,
            ),
        )
    }
}
