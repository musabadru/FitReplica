package com.fitreplica.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fitreplica.core.database.dao.ImageDao
import com.fitreplica.core.database.entity.ClothingItemEntity
import com.fitreplica.core.database.entity.ImageEntity
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
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
class ImageDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ImageDao

    private val itemId = ClothingId("item-1")

    @Before
    fun createDb() {
        database =
            Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = database.imageDao()
        runTest {
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
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun `inserting a second primary image demotes the first`() =
        runTest {
            dao.insertImage(image(id = "image-1", isPrimary = true))
            dao.insertImage(image(id = "image-2", isPrimary = true))

            val primaryImages = dao.observeImagesForItem(itemId).first().filter { it.isPrimary }
            assertEquals(listOf("image-2"), primaryImages.map { it.id })
        }

    @Test
    fun `setPrimaryImage moves the primary flag`() =
        runTest {
            dao.insertImage(image(id = "image-1", isPrimary = true))
            dao.insertImage(image(id = "image-2", isPrimary = false))

            dao.setPrimaryImage(itemId, "image-2")

            val primaryImages = dao.observeImagesForItem(itemId).first().filter { it.isPrimary }
            assertEquals(listOf("image-2"), primaryImages.map { it.id })
        }

    private fun image(
        id: String,
        isPrimary: Boolean,
    ) = ImageEntity(
        id = id,
        itemId = itemId,
        uri = "content://photo/$id",
        thumbnailUri = "content://thumb/$id",
        isPrimary = isPrimary,
        takenAt = 0L,
    )
}
