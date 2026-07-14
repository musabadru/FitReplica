package com.fitreplica.metadata.caretag

import com.fitreplica.metadata.api.CareRequirement
import com.fitreplica.metadata.api.CareTagMetadataProvider
import com.fitreplica.metadata.api.CareTagScanResult
import com.fitreplica.metadata.api.CareTagSymbol
import javax.inject.Inject

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
    buildList {
        when {
            text.hasNoWash -> add(CareRequirement.DO_NOT_WASH)
            "dry clean" in text -> add(CareRequirement.DRY_CLEAN_ONLY)
            text.hasHandWash -> add(CareRequirement.HAND_WASH_ONLY)
            temperature != null && temperature <= 30 -> add(CareRequirement.COLD_WASH)
            temperature != null && temperature <= 40 -> add(CareRequirement.WARM_WASH)
            temperature != null -> add(CareRequirement.HOT_WASH)
        }
        if ("air dry" in text || "line dry" in text || text.hasNoTumbleDry) add(CareRequirement.AIR_DRY)
        if ("low heat" in text) add(CareRequirement.LOW_HEAT)
        if (text.hasNoBleach) add(CareRequirement.AVOID_BLEACH)
        if (text.hasNoIron) add(CareRequirement.AVOID_IRON)
    }.distinct()

private val String.hasNoWash: Boolean get() = "do not wash" in this || "no wash" in this
private val String.hasHandWash: Boolean get() = "hand wash" in this
private val String.hasNoTumbleDry: Boolean get() = "do not tumble" in this || "no tumble" in this
private val String.hasNoIron: Boolean get() = "do not iron" in this || "no iron" in this
private val String.hasNoBleach: Boolean get() = "do not bleach" in this || "no bleach" in this
