package com.fitreplica.core.database.entity

import androidx.room.Entity
import androidx.room.Fts4

// External-content FTS4 table over clothing_items. Room generates triggers that keep
// this in sync with the content entity automatically on fresh installs; the same
// triggers must be created by hand in any Migration that adds this table (see
// MIGRATION_1_2), since Room only auto-creates them via onCreate, not via migrations.
@Fts4(contentEntity = ClothingItemEntity::class)
@Entity(tableName = "clothing_items_fts")
data class ClothingItemFts(
    val name: String,
    val brand: String?,
    val type: String,
    val colorPrimary: String,
)
