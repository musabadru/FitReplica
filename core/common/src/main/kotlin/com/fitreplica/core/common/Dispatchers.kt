package com.fitreplica.core.common

import javax.inject.Qualifier

enum class FitReplicaDispatchers {
    DEFAULT,
    IO,
    MAIN,
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val fitReplicaDispatcher: FitReplicaDispatchers)
