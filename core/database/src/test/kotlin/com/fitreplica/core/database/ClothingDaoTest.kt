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
private const val SECOND_WEAR_EVENT_TIME_MILLIS = 2_000L
private const val THIRD_WEAR_EVENT_TIME_MILLIS = 3_000L
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
    fun `repeated logWear calls keep the counter and event count in lockstep`() =
        runTest {
            val item = clothingItem(id = "item-1")
            dao.insertItem(item)

            val eventTimes = listOf(WEAR_EVENT_TIME_MILLIS, SECOND_WEAR_EVENT_TIME_MILLIS, THIRD_WEAR_EVENT_TIME_MILLIS)
            eventTimes.forEachIndexed { index, wornAt ->
                dao.logWear(
                    item.id,
                    com.fitreplica.core.database.entity.WearEventEntity(
                        id = WearEventId("event-$index"),
                        itemId = item.id,
                        outfitId = null,
                        dateTime = wornAt,
                        context = null,
                        notes = null,
                    ),
                )
            }

            val updated = dao.observeItem(item.id).first()
            assertEquals(eventTimes.size, updated?.timesWorn)
            assertEquals(eventTimes.last(), updated?.lastWornAt)

            database.query("SELECT COUNT(*) FROM wear_events WHERE itemId = ?", arrayOf(item.id.value)).use { cursor ->
                cursor.moveToFirst()
                assertEquals(eventTimes.size, cursor.getInt(0))
            }
        }

    @Test
    fun `observeWearHistory joins item display data and sorts newest first`() =
        runTest {
            val jacket = clothingItem(id = "jacket-1", name = "Blue Jacket", type = ClothingType.OUTERWEAR)
            val sneakers = clothingItem(id = "sneaker-1", name = "White Sneakers", type = ClothingType.SHOES)
            dao.insertItem(jacket)
            dao.insertItem(sneakers)

            dao.logWear(
                jacket.id,
                com.fitreplica.core.database.entity.WearEventEntity(
                    id = WearEventId("event-older"),
                    itemId = jacket.id,
                    outfitId = null,
                    dateTime = WEAR_EVENT_TIME_MILLIS,
                    context = "work",
                    notes = "first wear",
                ),
            )
            dao.logWear(
                sneakers.id,
                com.fitreplica.core.database.entity.WearEventEntity(
                    id = WearEventId("event-newer"),
                    itemId = sneakers.id,
                    outfitId = null,
                    dateTime = SECOND_WEAR_EVENT_TIME_MILLIS,
                    context = "weekend",
                    notes = null,
                ),
            )

            val history = dao.observeWearHistory().first()

            assertEquals(listOf(WearEventId("event-newer"), WearEventId("event-older")), history.map { it.id })
            assertEquals("White Sneakers", history.first().itemName)
            assertEquals(ClothingType.SHOES, history.first().itemType)
            assertEquals("weekend", history.first().context)
            assertEquals("first wear", history.last().notes)
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
    fun `updateItem replaces the stored row`() =
        runTest {
            val item = clothingItem(id = "item-1", name = "Original")
            dao.insertItem(item)

            dao.updateItem(item.copy(name = "Updated"))

            val updated = dao.observeItem(item.id).first()
            assertEquals("Updated", updated?.name)
        }

    @Test
    fun `deleteItem removes the row`() =
        runTest {
            val item = clothingItem(id = "item-1")
            dao.insertItem(item)

            dao.deleteItem(item.id)

            assertEquals(null, dao.observeItem(item.id).first())
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
