package com.fitreplica.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private const val DATABASE_VERSION_2 = 2
private const val DATABASE_VERSION_3 = 3

// Adds the rest of the v1 schema (outfits, laundry loads, images, FTS4 search) on top
// of the Phase 0 subset (clothing_items, wear_events). Purely additive — no existing
// column changes — but hand-written rather than @AutoMigration because the FTS4 table
// needs its content-sync triggers created explicitly: Room only auto-generates those
// via RoomOpenHelper.onCreate for a fresh install, never for a version-to-version
// Migration path, so an upgrading install would otherwise get a search table that
// silently stops tracking new/edited items.
val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            createOutfitTables(db)
            createLaundryTables(db)
            createImagesTable(db)
            addWearEventOutfitForeignKey(db)
            createFtsSearch(db)
        }
    }

val MIGRATION_2_3 =
    object : Migration(DATABASE_VERSION_2, DATABASE_VERSION_3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            addWearEventSnapshotFields(db)
        }
    }

private fun createOutfitTables(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `outfits` (
            `id` TEXT NOT NULL,
            `name` TEXT NOT NULL,
            `tags` TEXT NOT NULL,
            `rating` INTEGER,
            `createdAt` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
        )
        """.trimIndent(),
    )

    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `outfit_item_cross_ref` (
            `outfitId` TEXT NOT NULL,
            `itemId` TEXT NOT NULL,
            `position` INTEGER NOT NULL,
            PRIMARY KEY(`outfitId`, `itemId`),
            FOREIGN KEY(`outfitId`) REFERENCES `outfits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
            FOREIGN KEY(`itemId`) REFERENCES `clothing_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent(),
    )
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS `index_outfit_item_cross_ref_itemId` ON `outfit_item_cross_ref` (`itemId`)",
    )
}

private fun createLaundryTables(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `laundry_loads` (
            `id` TEXT NOT NULL,
            `startedAt` INTEGER NOT NULL,
            `completedAt` INTEGER,
            PRIMARY KEY(`id`)
        )
        """.trimIndent(),
    )

    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `laundry_load_item_cross_ref` (
            `loadId` TEXT NOT NULL,
            `itemId` TEXT NOT NULL,
            PRIMARY KEY(`loadId`, `itemId`),
            FOREIGN KEY(`loadId`) REFERENCES `laundry_loads`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
            FOREIGN KEY(`itemId`) REFERENCES `clothing_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent(),
    )
    db.execSQL(
        """
        CREATE INDEX IF NOT EXISTS `index_laundry_load_item_cross_ref_itemId`
        ON `laundry_load_item_cross_ref` (`itemId`)
        """.trimIndent(),
    )
}

private fun createImagesTable(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `images` (
            `id` TEXT NOT NULL,
            `itemId` TEXT NOT NULL,
            `uri` TEXT NOT NULL,
            `thumbnailUri` TEXT NOT NULL,
            `isPrimary` INTEGER NOT NULL,
            `takenAt` INTEGER NOT NULL,
            PRIMARY KEY(`id`),
            FOREIGN KEY(`itemId`) REFERENCES `clothing_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent(),
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_images_itemId` ON `images` (`itemId`)")
}

// SQLite has no ALTER TABLE ... ADD CONSTRAINT, so adding the wear_events -> outfits
// foreign key (needed now that `outfits` exists) requires the standard SQLite table-
// recreation dance: create the new shape, copy rows across, drop the old table, rename.
// Must run after createOutfitTables() so the referenced `outfits` table already exists.
private fun addWearEventOutfitForeignKey(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE `wear_events_new` (
            `id` TEXT NOT NULL,
            `itemId` TEXT NOT NULL,
            `outfitId` TEXT,
            `dateTime` INTEGER NOT NULL,
            `context` TEXT,
            `notes` TEXT,
            PRIMARY KEY(`id`),
            FOREIGN KEY(`itemId`) REFERENCES `clothing_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
            FOREIGN KEY(`outfitId`) REFERENCES `outfits`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
        )
        """.trimIndent(),
    )
    // outfitId is copied as NULL, not carried over verbatim: v1 had no `outfits` table at
    // all, so any non-null value stored there could never reference a real outfit row —
    // copying it as-is would either violate the new FK (if enforcement is on during the
    // migration) or silently leave a dangling reference (if it's off).
    db.execSQL(
        """
        INSERT INTO `wear_events_new` (`id`, `itemId`, `outfitId`, `dateTime`, `context`, `notes`)
        SELECT `id`, `itemId`, NULL, `dateTime`, `context`, `notes` FROM `wear_events`
        """.trimIndent(),
    )
    db.execSQL("DROP TABLE `wear_events`")
    db.execSQL("ALTER TABLE `wear_events_new` RENAME TO `wear_events`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_wear_events_itemId` ON `wear_events` (`itemId`)")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_wear_events_outfitId` ON `wear_events` (`outfitId`)")
}

private fun addWearEventSnapshotFields(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE `wear_events` ADD COLUMN `itemName` TEXT NOT NULL DEFAULT ''")
    db.execSQL("ALTER TABLE `wear_events` ADD COLUMN `itemType` TEXT NOT NULL DEFAULT 'OTHER'")
    db.execSQL("ALTER TABLE `wear_events` ADD COLUMN `colorPrimary` TEXT NOT NULL DEFAULT ''")
    db.execSQL(
        """
        UPDATE `wear_events`
        SET
            `itemName` = COALESCE(
                (SELECT `name` FROM `clothing_items` WHERE `clothing_items`.`id` = `wear_events`.`itemId`),
                ''
            ),
            `itemType` = COALESCE(
                (SELECT `type` FROM `clothing_items` WHERE `clothing_items`.`id` = `wear_events`.`itemId`),
                'OTHER'
            ),
            `colorPrimary` = COALESCE(
                (SELECT `colorPrimary` FROM `clothing_items` WHERE `clothing_items`.`id` = `wear_events`.`itemId`),
                ''
            )
        """.trimIndent(),
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_wear_events_dateTime` ON `wear_events` (`dateTime`)")
}

// Room only auto-creates the FTS4 content-sync triggers via onCreate on a fresh
// install; a Migration must recreate the same trigger set by hand (see MIGRATION_1_2
// doc comment above) plus a one-time backfill for rows that predate this table.
private fun createFtsSearch(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE VIRTUAL TABLE IF NOT EXISTS `clothing_items_fts` USING FTS4(
            `name`, `brand`, `type`, `colorPrimary`, content=`clothing_items`
        )
        """.trimIndent(),
    )
    db.execSQL(
        """
        INSERT INTO `clothing_items_fts`(`docid`, `name`, `brand`, `type`, `colorPrimary`)
        SELECT `rowid`, `name`, `brand`, `type`, `colorPrimary` FROM `clothing_items`
        """.trimIndent(),
    )
    db.execSQL(
        """
        CREATE TRIGGER IF NOT EXISTS `room_fts_content_sync_clothing_items_fts_BEFORE_UPDATE`
        BEFORE UPDATE ON `clothing_items` BEGIN
            DELETE FROM `clothing_items_fts` WHERE `docid`=OLD.`rowid`;
        END
        """.trimIndent(),
    )
    db.execSQL(
        """
        CREATE TRIGGER IF NOT EXISTS `room_fts_content_sync_clothing_items_fts_BEFORE_DELETE`
        BEFORE DELETE ON `clothing_items` BEGIN
            DELETE FROM `clothing_items_fts` WHERE `docid`=OLD.`rowid`;
        END
        """.trimIndent(),
    )
    db.execSQL(
        """
        CREATE TRIGGER IF NOT EXISTS `room_fts_content_sync_clothing_items_fts_AFTER_UPDATE`
        AFTER UPDATE ON `clothing_items` BEGIN
            INSERT INTO `clothing_items_fts`(`docid`, `name`, `brand`, `type`, `colorPrimary`)
            VALUES (NEW.`rowid`, NEW.`name`, NEW.`brand`, NEW.`type`, NEW.`colorPrimary`);
        END
        """.trimIndent(),
    )
    db.execSQL(
        """
        CREATE TRIGGER IF NOT EXISTS `room_fts_content_sync_clothing_items_fts_AFTER_INSERT`
        AFTER INSERT ON `clothing_items` BEGIN
            INSERT INTO `clothing_items_fts`(`docid`, `name`, `brand`, `type`, `colorPrimary`)
            VALUES (NEW.`rowid`, NEW.`name`, NEW.`brand`, NEW.`type`, NEW.`colorPrimary`);
        END
        """.trimIndent(),
    )
}
