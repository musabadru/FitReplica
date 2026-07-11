package com.fitreplica.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fitreplica.core.common.Result
import com.fitreplica.core.database.entity.ClothingItemEntity
import com.fitreplica.core.database.repository.ImageRepositoryImpl
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
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
class ImageRepositoryImplTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: ImageRepositoryImpl
    private val itemId = ClothingId("item-1")

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database =
            Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = ImageRepositoryImpl(database.imageDao(), context)
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

    // Per §8g: an image save failure (here, an unreadable/nonexistent source URI standing
    // in for a real I/O failure like disk-full) must degrade to Result.Error rather than
    // throwing, and must not leave a partial ImageEntity row behind — the item save itself
    // is never blocked by a photo failing to save.
    @Test
    fun `addImage returns Result Error and inserts nothing when the source URI can't be read`() =
        runTest {
            val result = repository.addImage(itemId, "file:///nonexistent/path/to/photo.jpg", isPrimary = true)

            assertTrue(result is Result.Error)
            assertTrue(repository.observeImages(itemId).first().isEmpty())
        }
}
