package com.fitreplica.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fitreplica.core.database.dao.ClothingDao
import com.fitreplica.core.database.entity.ClothingItemEntity
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.Status
import com.fitreplica.core.model.WearEventId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val WEAR_EVENT_TIME_MILLIS = 1_000L
private const val COLUMN_ID = 0
private const val COLUMN_ITEM_ID = 1
private const val COLUMN_DATE_TIME = 2
private const val COLUMN_CONTEXT = 3

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ClothingDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ClothingDao

    @Before
    fun createDb() {
        database =
            Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = database.clothingDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun `logWear updates counter and inserts event atomically`() =
        runTest {
            val item = clothingItem(id = "item-1")
            dao.insertItem(item)

            dao.logWear(
                item.id,
                com.fitreplica.core.database.entity.WearEventEntity(
                    id = WearEventId("event-1"),
                    itemId = item.id,
                    outfitId = null,
                    dateTime = WEAR_EVENT_TIME_MILLIS,
                    context = "work",
                    notes = null,
                ),
            )

            val updated = dao.observeItem(item.id).first()
            assertEquals(1, updated?.timesWorn)
            assertEquals(WEAR_EVENT_TIME_MILLIS, updated?.lastWornAt)

            database.query("SELECT id, itemId, dateTime, context FROM wear_events", null).use { cursor ->
                assertEquals(1, cursor.count)
                cursor.moveToFirst()
                assertEquals("event-1", cursor.getString(COLUMN_ID))
                assertEquals(item.id.value, cursor.getString(COLUMN_ITEM_ID))
                assertEquals(WEAR_EVENT_TIME_MILLIS, cursor.getLong(COLUMN_DATE_TIME))
                assertEquals("work", cursor.getString(COLUMN_CONTEXT))
            }
        }

    @Test
    fun `updateCondition changes only the condition column`() =
        runTest {
            val item = clothingItem(id = "item-1", condition = Condition.NEW)
            dao.insertItem(item)

            dao.updateCondition(item.id, Condition.NEEDS_REPAIR)

            val updated = dao.observeItem(item.id).first()
            assertEquals(Condition.NEEDS_REPAIR, updated?.condition)
            assertEquals(item.name, updated?.name)
        }

    @Test
    fun `observeItemsFiltered narrows by type and status`() =
        runTest {
            dao.insertItem(clothingItem(id = "top-1", type = ClothingType.TOP, status = Status.CLEAN))
            dao.insertItem(clothingItem(id = "shoe-1", type = ClothingType.SHOES, status = Status.CLEAN))
            dao.insertItem(clothingItem(id = "top-2", type = ClothingType.TOP, status = Status.DIRTY))

            val results =
                dao.observeItemsFiltered(
                    type = ClothingType.TOP,
                    status = Status.CLEAN,
                    condition = null,
                    brand = null,
                    colorPrimary = null,
                ).first()

            assertEquals(listOf(ClothingId("top-1")), results.map { it.id })
        }

    @Test
    fun `searchItems finds items via FTS prefix match`() =
        runTest {
            dao.insertItem(clothingItem(id = "item-1", name = "Blue Nike Jacket"))
            dao.insertItem(clothingItem(id = "item-2", name = "Red Adidas Shorts"))

            val results =
                dao.searchItems(
                    ftsQuery = "blue* nike*",
                    type = null,
                    status = null,
                    condition = null,
                    brand = null,
                    colorPrimary = null,
                ).first()

            assertEquals(listOf(ClothingId("item-1")), results.map { it.id })
        }

    private fun clothingItem(
        id: String,
        name: String = "Item $id",
        type: ClothingType = ClothingType.TOP,
        status: Status = Status.CLEAN,
        condition: Condition = Condition.NEW,
    ) = ClothingItemEntity(
        id = ClothingId(id),
        name = name,
        type = type,
        brand = null,
        colorPrimary = "blue",
        colorSecondary = null,
        condition = condition,
        status = status,
        timesWorn = 0,
        lastWornAt = null,
        addedAt = 0L,
        size = null,
    )
}
