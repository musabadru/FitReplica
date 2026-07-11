package com.fitreplica.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.fitreplica.core.datastore.UserPreferences
import com.fitreplica.core.datastore.UserPreferencesRepositoryImpl
import com.fitreplica.core.datastore.UserPreferencesSerializer
import com.fitreplica.core.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<UserPreferences> =
        DataStoreFactory.create(
            serializer = UserPreferencesSerializer,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            produceFile = { context.dataStoreFile("user_preferences.pb") },
        )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreBindsModule {
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
