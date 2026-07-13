package com.fitreplica.core.database.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fitreplica.core.database.AppDatabase
import com.fitreplica.core.database.entity.WearEventEntity
import com.fitreplica.core.domain.repository.ClosetFilter
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ClothingRepositoryImplTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: ClothingRepositoryImpl

    @Before
    fun createDb() {
        database =
            Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = ClothingRepositoryImpl(database.clothingDao())
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun `search queries containing FTS4 special characters don't throw`() =
        runTest {
            repository.addItem(sampleItem())

            // Each of these would previously produce a malformed FTS4 MATCH expression.
            for (query in listOf("nike*", "\"blue jacket\"", "(blue)", "blue:jacket", "blue-jacket", "***")) {
                repository.observeItems(ClosetFilter(searchQuery = query)).first()
            }
        }

    @Test
    fun `search still matches after stripping special characters`() =
        runTest {
            repository.addItem(sampleItem())

            val results = repository.observeItems(ClosetFilter(searchQuery = "nike*")).first()

            assertEquals(listOf(ClothingId("item-1")), results.map { it.id })
        }

    @Test
    fun `hyphenated search terms stay separate tokens instead of merging`() =
        runTest {
            repository.addItem(sampleItem())

            // "nike-jacket" must become "nike* jacket*", not "nikejacket*" — the FTS4
            // tokenizer already indexed "Nike" and "Jacket" as separate tokens, so a
            // merged term would never match either one.
            val results = repository.observeItems(ClosetFilter(searchQuery = "nike-jacket")).first()

            assertEquals(listOf(ClothingId("item-1")), results.map { it.id })
        }

    @Test
    fun `observeWearHistory maps joined wear events to domain entries`() =
        runTest {
            val item = sampleItem()
            repository.addItem(item)
            database.clothingDao().logWear(
                item.id,
                WearEventEntity(
                    id = WearEventId("event-1"),
                    itemId = item.id,
                    outfitId = null,
                    dateTime = 1_000L,
                    context = "date night",
                    notes = "felt good",
                ),
            )

            val history = repository.observeWearHistory().first()

            assertEquals(listOf(WearEventId("event-1")), history.map { it.id })
            assertEquals("Blue Nike Jacket", history.single().itemName)
            assertEquals(ClothingType.OUTERWEAR, history.single().itemType)
            assertEquals("date night", history.single().context)
            assertEquals("felt good", history.single().notes)
        }

    private fun sampleItem() =
        ClothingItem(
            id = ClothingId("item-1"),
            name = "Blue Nike Jacket",
            type = ClothingType.OUTERWEAR,
            brand = "Nike",
            colorPrimary = "blue",
            colorSecondary = null,
            condition = Condition.NEW,
            status = Status.CLEAN,
            size = null,
            timesWorn = 0,
            lastWornAt = null,
            addedAt = 0L,
        )
}
