package com.fitreplica.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TEST_DB = "migration-test"

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
                .getDatabasePath(TEST_DB)
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
}
