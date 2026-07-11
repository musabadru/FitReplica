package com.fitreplica.core.database.di

import com.fitreplica.core.database.repository.ClothingRepositoryImpl
import com.fitreplica.core.database.repository.ImageRepositoryImpl
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.domain.repository.ImageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindClothingRepository(impl: ClothingRepositoryImpl): ClothingRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository
}
