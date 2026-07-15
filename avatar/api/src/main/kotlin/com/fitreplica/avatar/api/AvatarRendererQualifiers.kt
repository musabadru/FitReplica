package com.fitreplica.avatar.api

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AvatarFallbackRenderer

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AvatarTwoDRenderer
