package com.fitreplica.avatar.impl2d

import com.fitreplica.avatar.api.AvatarRenderer
import com.fitreplica.avatar.api.AvatarTwoDRenderer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TwoDAvatarModule {
    @Binds
    @Singleton
    @AvatarTwoDRenderer
    abstract fun bindTwoDAvatarRenderer(impl: TwoDAvatarRenderer): AvatarRenderer
}
