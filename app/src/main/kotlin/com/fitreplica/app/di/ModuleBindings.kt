package com.fitreplica.app.di

import com.fitreplica.avatar.api.AvatarRenderer
import com.fitreplica.avatar.api.NoOpAvatarRenderer
import com.fitreplica.core.domain.repository.NoOpWeatherProvider
import com.fitreplica.core.domain.repository.SuggestionEngine
import com.fitreplica.core.domain.repository.WeatherProvider
import com.fitreplica.core.domain.usecase.RuleEngine
import com.fitreplica.metadata.api.MetadataProvider
import com.fitreplica.metadata.noop.NoOpMetadataProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * NoOp is the only implementation until :avatar:impl-2d (Phase 3) and
 * :metadata:impl-barcode (Phase 6) exist — swap these bindings for a
 * preference-driven choice (see architecture doc §9) once they land.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AvatarModule {
    @Binds
    @Singleton
    abstract fun bindAvatarRenderer(impl: NoOpAvatarRenderer): AvatarRenderer
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MetadataModule {
    @Binds
    @Singleton
    abstract fun bindMetadataProvider(impl: NoOpMetadataProvider): MetadataProvider
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SuggestionModule {
    @Binds
    @Singleton
    abstract fun bindWeatherProvider(impl: NoOpWeatherProvider): WeatherProvider

    @Binds
    @Singleton
    abstract fun bindSuggestionEngine(impl: RuleEngine): SuggestionEngine
}
