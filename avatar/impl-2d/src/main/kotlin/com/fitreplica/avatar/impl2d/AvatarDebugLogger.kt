package com.fitreplica.avatar.impl2d

import android.util.Log

internal fun interface AvatarDebugLogger {
    fun log(message: String)
}

internal val AndroidAvatarDebugLogger =
    AvatarDebugLogger { message ->
        if (BuildConfig.DEBUG) {
            Log.d("TwoDAvatarRenderer", message)
        }
    }

internal val NoOpAvatarDebugLogger = AvatarDebugLogger {}
