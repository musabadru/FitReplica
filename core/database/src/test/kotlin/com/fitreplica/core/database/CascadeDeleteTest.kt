package com.fitreplica.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fitreplica.core.database.entity.ClothingItemEntity
import com.fitreplica.core.database.entity.ImageEntity
import com.fitreplica.core.database.entity.LaundryLoadEntity
import com.fitreplica.core.database.entity.LaundryLoadItemCrossRef
import com.fitreplica.core.database.entity.OutfitEntity
import com.fitreplica.core.database.entity.OutfitItemCrossRef
import com.fitreplica.core.database.entity.WearEventEntity
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.LaundryLoadId
import com.fitreplica.core.model.OutfitId
import com.fitreplica.core.model.Status
import com.fitreplica.core.model.WearEventId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CascadeDeleteTest {
    private lateinit var database: AppDatabase

    private val itemId = ClothingId("item-1")

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
    fun `deleting an outfit removes its cross-ref rows`() =
        runTest {
            insertItem()
            insertOutfit()
            database.outfitDao().insertOutfitItemCrossRef(
                OutfitItemCrossRef(outfitId = OutfitId("outfit-1"), itemId = itemId, position = 0),
            )

            database.outfitDao().deleteOutfit(OutfitId("outfit-1"))

            assertEquals(0, rowCount("outfit_item_cross_ref"))
        }

    @Test
    fun `deleting a clothing item cascades to its images`() =
        runTest {
            insertItem()
            database.imageDao().insertImage(
                ImageEntity(
                    id = "image-1",
                    itemId = itemId,
                    uri = "content://photo",
                    thumbnailUri = "content://thumb",
                    isPrimary = true,
                    takenAt = 0L,
                ),
            )

            deleteItem()

            val images = database.imageDao().observeImagesForItem(itemId).first()
            assertTrue(images.isEmpty())
        }

    @Test
    fun `deleting a clothing item cascades to its wear events`() =
        runTest {
            insertItem()
            database.clothingDao().logWear(
                itemId,
                WearEventEntity(
                    id = WearEventId("event-1"),
                    itemId = itemId,
                    outfitId = null,
                    dateTime = 0L,
                    context = null,
                    notes = null,
                ),
            )

            deleteItem()

            assertEquals(0, rowCount("wear_events"))
        }

    @Test
    fun `deleting a clothing item cascades to its outfit cross-ref rows`() =
        runTest {
            insertItem()
            insertOutfit()
            database.outfitDao().insertOutfitItemCrossRef(
                OutfitItemCrossRef(outfitId = OutfitId("outfit-1"), itemId = itemId, position = 0),
            )

            deleteItem()

            assertEquals(0, rowCount("outfit_item_cross_ref"))
        }

    @Test
    fun `deleting a clothing item cascades to its laundry cross-ref rows`() =
        runTest {
            insertItem()
            insertLaundryLoad()
            database.laundryDao().insertLoadItemCrossRef(
                LaundryLoadItemCrossRef(loadId = LaundryLoadId("load-1"), itemId = itemId),
            )

            deleteItem()

            assertEquals(0, rowCount("laundry_load_item_cross_ref"))
        }

    @Test
    fun `deleting a laundry load cascades to its cross-ref rows`() =
        runTest {
            insertItem()
            insertLaundryLoad()
            database.laundryDao().insertLoadItemCrossRef(
                LaundryLoadItemCrossRef(loadId = LaundryLoadId("load-1"), itemId = itemId),
            )

            database.laundryDao().deleteLoad(LaundryLoadId("load-1"))

            assertEquals(0, rowCount("laundry_load_item_cross_ref"))
        }

    @Test
    fun `deleting an outfit clears outfitId on its wear events instead of deleting them`() =
        runTest {
            insertItem()
            insertOutfit()
            database.clothingDao().logWear(
                itemId,
                WearEventEntity(
                    id = WearEventId("event-1"),
                    itemId = itemId,
                    outfitId = OutfitId("outfit-1"),
                    dateTime = 0L,
                    context = null,
                    notes = null,
                ),
            )

            database.outfitDao().deleteOutfit(OutfitId("outfit-1"))

            assertEquals(1, rowCount("wear_events"))
            val outfitId =
                database.query("SELECT outfitId FROM wear_events WHERE id = 'event-1'", null).use { cursor ->
                    cursor.moveToFirst()
                    if (cursor.isNull(0)) null else cursor.getString(0)
                }
            assertNull(outfitId)
        }

    private suspend fun insertItem() {
        database.clothingDao().insertItem(
            ClothingItemEntity(
                id = itemId,
                name = "Jacket",
                type = ClothingType.OUTERWEAR,
                brand = null,
                colorPrimary = "blue",
                colorSecondary = null,
                condition = Condition.NEW,
                status = Status.CLEAN,
                timesWorn = 0,
                lastWornAt = null,
                addedAt = 0L,
                size = null,
            ),
        )
    }

    private fun deleteItem() {
        database.openHelper.writableDatabase.execSQL("DELETE FROM clothing_items WHERE id = 'item-1'")
    }

    private suspend fun insertOutfit() {
        database.outfitDao().insertOutfit(
            OutfitEntity(
                id = OutfitId("outfit-1"),
                name = "Work fit",
                tags = emptyList(),
                rating = null,
                createdAt = 0L,
            ),
        )
    }

    private suspend fun insertLaundryLoad() {
        database.laundryDao().insertLoad(
            LaundryLoadEntity(id = LaundryLoadId("load-1"), startedAt = 0L, completedAt = null),
        )
    }

    private fun rowCount(table: String): Int =
        database.query("SELECT COUNT(*) FROM $table", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
}
