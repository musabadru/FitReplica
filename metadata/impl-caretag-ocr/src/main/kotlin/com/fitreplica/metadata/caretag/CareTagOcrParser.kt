package com.fitreplica.metadata.caretag

import com.fitreplica.metadata.api.CareRequirement
import com.fitreplica.metadata.api.CareTagMetadataProvider
import com.fitreplica.metadata.api.CareTagScanResult
import com.fitreplica.metadata.api.CareTagSymbol
import javax.inject.Inject

private const val COLD_WASH_MAX_CELSIUS = 30
private const val WARM_WASH_MAX_CELSIUS = 40

class CareTagOcrParser
    @Inject
    constructor() : CareTagMetadataProvider {
        override suspend fun parse(rawText: String): CareTagScanResult = rawText.toCareTagResult()
    }

internal fun String.toCareTagResult(): CareTagScanResult {
    val normalized = lowercase()
    val hasNoWash = normalized.hasNoWash
    val hasHandWash = normalized.hasHandWash
    val hasNoTumbleDry = normalized.hasNoTumbleDry
    val hasNoIron = normalized.hasNoIron
    val hasNoBleach = normalized.hasNoBleach
    val symbols =
        buildList {
            when {
                hasNoWash -> add(CareTagSymbol.DO_NOT_WASH)
                hasHandWash -> add(CareTagSymbol.HAND_WASH)
                "machine wash" in normalized || "wash" in normalized -> add(CareTagSymbol.MACHINE_WASH)
            }
            if (hasNoTumbleDry) {
                add(CareTagSymbol.DO_NOT_TUMBLE_DRY)
            } else if ("tumble dry" in normalized) {
                add(CareTagSymbol.TUMBLE_DRY)
            }
            if (hasNoIron) {
                add(CareTagSymbol.DO_NOT_IRON)
            } else if ("iron" in normalized) {
                add(CareTagSymbol.IRON)
            }
            if (hasNoBleach) {
                add(CareTagSymbol.DO_NOT_BLEACH)
            } else if ("bleach" in normalized) {
                add(CareTagSymbol.BLEACH)
            }
            if ("dry clean" in normalized) add(CareTagSymbol.DRY_CLEAN)
        }.distinct()
    val temperature = Regex("""(\d{2})\s?(c|°c|degrees)""").find(normalized)?.groupValues?.get(1)?.toIntOrNull()
    return CareTagScanResult(
        rawText = this,
        recognizedSymbols = symbols,
        suggestedWashTemperatureCelsius = temperature,
        careRequirements = buildRequirements(normalized, temperature),
    )
}

private fun buildRequirements(
    text: String,
    temperature: Int?,
): List<CareRequirement> =
    listOfNotNull(
        text.washRequirement(temperature),
        CareRequirement.AIR_DRY.takeIf { text.requiresAirDry },
        CareRequirement.LOW_HEAT.takeIf { "low heat" in text },
        CareRequirement.AVOID_BLEACH.takeIf { text.hasNoBleach },
        CareRequirement.AVOID_IRON.takeIf { text.hasNoIron },
    ).distinct()

private fun String.washRequirement(temperature: Int?): CareRequirement? =
    when {
        hasNoWash -> CareRequirement.DO_NOT_WASH
        "dry clean" in this -> CareRequirement.DRY_CLEAN_ONLY
        hasHandWash -> CareRequirement.HAND_WASH_ONLY
        temperature != null && temperature <= COLD_WASH_MAX_CELSIUS -> CareRequirement.COLD_WASH
        temperature != null && temperature <= WARM_WASH_MAX_CELSIUS -> CareRequirement.WARM_WASH
        temperature != null -> CareRequirement.HOT_WASH
        else -> null
    }

private val String.hasNoWash: Boolean get() = "do not wash" in this || "no wash" in this
private val String.hasHandWash: Boolean get() = "hand wash" in this
private val String.hasNoTumbleDry: Boolean get() = "do not tumble" in this || "no tumble" in this
private val String.hasNoIron: Boolean get() = "do not iron" in this || "no iron" in this
private val String.hasNoBleach: Boolean get() = "do not bleach" in this || "no bleach" in this
private val String.requiresAirDry: Boolean get() = "air dry" in this || "line dry" in this || hasNoTumbleDry
