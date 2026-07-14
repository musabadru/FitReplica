package com.fitreplica.avatar.api

enum class AvatarLayer(val zIndex: Int) {
    Body(0),
    Base(BASE_LAYER_Z_INDEX),
    Mid(MID_LAYER_Z_INDEX),
    Outer(OUTER_LAYER_Z_INDEX),
    Shoes(SHOES_LAYER_Z_INDEX),
}

private const val BASE_LAYER_Z_INDEX = 10
private const val MID_LAYER_Z_INDEX = 20
private const val OUTER_LAYER_Z_INDEX = 30
private const val SHOES_LAYER_Z_INDEX = 40
