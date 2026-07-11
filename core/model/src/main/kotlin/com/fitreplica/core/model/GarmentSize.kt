package com.fitreplica.core.model

data class GarmentSize(
    val label: String,
    val system: SizeSystem = SizeSystem.UNKNOWN,
    val category: SizeCategory,
    val measuredChestCm: Float? = null,
    val measuredWaistCm: Float? = null,
    val measuredLengthCm: Float? = null,
)
