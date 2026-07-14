package com.fitreplica.metadata.api

interface CareTagMetadataProvider {
    suspend fun parse(rawText: String): CareTagScanResult
}

data class CareTagScanResult(
    val rawText: String,
    val recognizedSymbols: List<CareTagSymbol>,
    val suggestedWashTemperatureCelsius: Int?,
    val careRequirements: List<CareRequirement>,
)

enum class CareTagSymbol {
    MACHINE_WASH,
    HAND_WASH,
    DO_NOT_WASH,
    TUMBLE_DRY,
    DO_NOT_TUMBLE_DRY,
    IRON,
    DO_NOT_IRON,
    BLEACH,
    DO_NOT_BLEACH,
    DRY_CLEAN,
    DO_NOT_DRY_CLEAN,
}

enum class CareRequirement {
    DO_NOT_WASH,
    COLD_WASH,
    WARM_WASH,
    HOT_WASH,
    HAND_WASH_ONLY,
    AIR_DRY,
    LOW_HEAT,
    AVOID_BLEACH,
    AVOID_IRON,
    DRY_CLEAN_ONLY,
    AVOID_DRY_CLEAN,
}
