package com.fitreplica.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fitreplica.core.database.entity.ClothingItemEntity
import com.fitreplica.core.database.entity.ImageEntity
import com.fitreplica.core.database.entity.OutfitEntity
import com.fitreplica.core.database.entity.OutfitItemCrossRef
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.OutfitId
import com.fitreplica.core.model.Status
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
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
            val item =
                ClothingItemEntity(
                    id = ClothingId("item-1"),
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
                )
            database.clothingDao().insertItem(item)
            database.outfitDao().insertOutfit(
                OutfitEntity(
                    id = OutfitId("outfit-1"),
                    name = "Work fit",
                    tags = emptyList(),
                    rating = null,
                    createdAt = 0L,
                ),
            )
            database.outfitDao().insertOutfitItemCrossRef(
                OutfitItemCrossRef(outfitId = OutfitId("outfit-1"), itemId = item.id, position = 0),
            )

            database.outfitDao().deleteOutfit(OutfitId("outfit-1"))

            val remaining =
                database.query("SELECT COUNT(*) FROM outfit_item_cross_ref", null).use { cursor ->
                    cursor.moveToFirst()
                    cursor.getInt(0)
                }
            assertTrue(remaining == 0)
        }

    @Test
    fun `deleting a clothing item cascades to its images`() =
        runTest {
            val item =
                ClothingItemEntity(
                    id = ClothingId("item-1"),
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
                )
            database.clothingDao().insertItem(item)
            database.imageDao().insertImage(
                ImageEntity(
                    id = "image-1",
                    itemId = item.id,
                    uri = "content://photo",
                    thumbnailUri = "content://thumb",
                    isPrimary = true,
                    takenAt = 0L,
                ),
            )

            database.openHelper.writableDatabase.execSQL("DELETE FROM clothing_items WHERE id = 'item-1'")

            val images = database.imageDao().observeImagesForItem(item.id).first()
            assertTrue(images.isEmpty())
        }
}
