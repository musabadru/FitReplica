package com.fitreplica.core.database.di

import android.content.Context
import androidx.room.Room
import com.fitreplica.core.database.AppDatabase
import com.fitreplica.core.database.dao.ClothingDao
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
    ): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "fitreplica.db").build()

    @Provides
    fun provideClothingDao(database: AppDatabase): ClothingDao = database.clothingDao()
}
