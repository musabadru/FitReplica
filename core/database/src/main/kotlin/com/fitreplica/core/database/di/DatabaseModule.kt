package com.fitreplica.core.database.di

import android.content.Context
import androidx.room.Room
import com.fitreplica.core.database.AppDatabase
import com.fitreplica.core.database.MIGRATION_1_2
import com.fitreplica.core.database.dao.ClothingDao
import com.fitreplica.core.database.dao.ImageDao
import com.fitreplica.core.database.dao.LaundryDao
import com.fitreplica.core.database.dao.OutfitDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "fitreplica.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideClothingDao(database: AppDatabase): ClothingDao = database.clothingDao()

    @Provides
    fun provideOutfitDao(database: AppDatabase): OutfitDao = database.outfitDao()

    @Provides
    fun provideLaundryDao(database: AppDatabase): LaundryDao = database.laundryDao()

    @Provides
    fun provideImageDao(database: AppDatabase): ImageDao = database.imageDao()
}
