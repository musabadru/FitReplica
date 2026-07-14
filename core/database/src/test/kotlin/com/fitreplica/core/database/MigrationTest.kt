package com.fitreplica.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MIGRATION_1_2_TEST_DB = "migration-1-2-test"
private const val MIGRATION_2_3_TEST_DB = "migration-2-3-test"
private const val VERSION_TWO = 2
private const val VERSION_THREE = 3

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MigrationTest {
    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java,
            emptyList(),
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    fun `migrate 1 to 2 preserves existing rows and adds FTS search`() {
        val testDbPath =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .getDatabasePath(MIGRATION_1_2_TEST_DB)
                .absolutePath

        helper.createDatabase(testDbPath, 1).apply {
            execSQL(
                """
                INSERT INTO clothing_items
                (id, name, type, brand, colorPrimary, colorSecondary, condition, status,
                 timesWorn, lastWornAt, addedAt, sku, avatarSlot, purchasePrice, purchaseDate,
                 purchaseLocation, notes, size_label, size_system, size_category,
                 size_measuredChestCm, size_measuredWaistCm, size_measuredLengthCm)
                VALUES
                ('item-1', 'Blue Nike Jacket', 'OUTERWEAR', 'Nike', 'blue', NULL, 'NEW', 'CLEAN',
                 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
                """.trimIndent(),
            )
            // v1 has no `outfits` table, so this outfitId was never backed by a real
            // outfit row — it must survive the migration with outfitId cleared, not
            // throw a foreign-key violation or leave a dangling reference.
            execSQL(
                """
                INSERT INTO wear_events (id, itemId, outfitId, dateTime, context, notes)
                VALUES ('event-1', 'item-1', 'orphaned-outfit-id', 0, NULL, NULL)
                """.trimIndent(),
            )
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(testDbPath, 2, true, MIGRATION_1_2)

        val itemCount =
            migratedDb.query("SELECT COUNT(*) FROM clothing_items").use { cursor ->
                cursor.moveToFirst()
                cursor.getInt(0)
            }
        check(itemCount == 1) { "Expected the pre-migration row to survive, found $itemCount" }

        val searchHits =
            migratedDb.query(
                "SELECT COUNT(*) FROM clothing_items_fts WHERE clothing_items_fts MATCH 'nike*'",
            ).use { cursor ->
                cursor.moveToFirst()
                cursor.getInt(0)
            }
        check(searchHits == 1) { "Expected FTS backfill to find the migrated row, found $searchHits" }

        migratedDb.query("SELECT outfitId FROM wear_events WHERE id = 'event-1'").use { cursor ->
            cursor.moveToFirst()
            assertTrue("Expected the orphaned pre-v1 outfitId to be cleared", cursor.isNull(0))
        }
    }

    @Test
    fun `migrate 2 to 3 snapshots wear event display fields and adds date index`() {
        val testDbPath =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .getDatabasePath(MIGRATION_2_3_TEST_DB)
                .absolutePath

        helper.createDatabase(testDbPath, VERSION_TWO).apply {
            execSQL(
                """
                INSERT INTO clothing_items
                (id, name, type, brand, colorPrimary, colorSecondary, condition, status,
                 timesWorn, lastWornAt, addedAt, sku, avatarSlot, purchasePrice, purchaseDate,
                 purchaseLocation, notes, size_label, size_system, size_category,
                 size_measuredChestCm, size_measuredWaistCm, size_measuredLengthCm)
                VALUES
                ('item-1', 'Blue Nike Jacket', 'OUTERWEAR', 'Nike', 'blue', NULL, 'NEW', 'CLEAN',
                 1, 1000, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO wear_events (id, itemId, outfitId, dateTime, context, notes)
                VALUES ('event-1', 'item-1', NULL, 1000, 'work', NULL)
                """.trimIndent(),
            )
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(testDbPath, VERSION_THREE, true, MIGRATION_2_3)

        migratedDb.query(
            "SELECT itemName, itemType, colorPrimary FROM wear_events WHERE id = 'event-1'",
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals("Blue Nike Jacket", cursor.getString(0))
            assertEquals("OUTERWEAR", cursor.getString(1))
            assertEquals("blue", cursor.getString(2))
        }
        migratedDb.query("PRAGMA index_list('wear_events')").use { cursor ->
            var hasDateTimeIndex = false
            while (cursor.moveToNext()) {
                hasDateTimeIndex = hasDateTimeIndex || cursor.getString(1) == "index_wear_events_dateTime"
            }
            assertTrue("Expected wear_events dateTime index after migration", hasDateTimeIndex)
        }
    }
}
