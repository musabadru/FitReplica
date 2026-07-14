package com.fitreplica.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fitreplica.core.database.dao.ClothingDao
import com.fitreplica.core.database.dao.ImageDao
import com.fitreplica.core.database.dao.LaundryDao
import com.fitreplica.core.database.dao.OutfitDao
import com.fitreplica.core.database.entity.ClothingItemEntity
import com.fitreplica.core.database.entity.ClothingItemFts
import com.fitreplica.core.database.entity.ConditionEventEntity
import com.fitreplica.core.database.entity.ImageEntity
import com.fitreplica.core.database.entity.LaundryLoadEntity
import com.fitreplica.core.database.entity.LaundryLoadItemCrossRef
import com.fitreplica.core.database.entity.OutfitEntity
import com.fitreplica.core.database.entity.OutfitItemCrossRef
import com.fitreplica.core.database.entity.WearEventEntity

@Database(
    entities = [
        ClothingItemEntity::class,
        WearEventEntity::class,
        ConditionEventEntity::class,
        OutfitEntity::class,
        OutfitItemCrossRef::class,
        LaundryLoadEntity::class,
        LaundryLoadItemCrossRef::class,
        ImageEntity::class,
        ClothingItemFts::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class, ListConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clothingDao(): ClothingDao

    abstract fun outfitDao(): OutfitDao

    abstract fun laundryDao(): LaundryDao

    abstract fun imageDao(): ImageDao
}
