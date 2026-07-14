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
    val symbols =
        buildList {
            if ("machine wash" in normalized || "wash" in normalized) add(CareTagSymbol.MACHINE_WASH)
            if ("hand wash" in normalized) add(CareTagSymbol.HAND_WASH)
            if ("do not wash" in normalized) add(CareTagSymbol.DO_NOT_WASH)
            if ("tumble dry" in normalized) add(CareTagSymbol.TUMBLE_DRY)
            if ("do not tumble" in normalized || "no tumble" in normalized) add(CareTagSymbol.DO_NOT_TUMBLE_DRY)
            if ("iron" in normalized) add(CareTagSymbol.IRON)
            if ("do not iron" in normalized || "no iron" in normalized) add(CareTagSymbol.DO_NOT_IRON)
            if ("bleach" in normalized) add(CareTagSymbol.BLEACH)
            if ("do not bleach" in normalized || "no bleach" in normalized) add(CareTagSymbol.DO_NOT_BLEACH)
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
            "dry clean" in text -> add(CareRequirement.DRY_CLEAN_ONLY)
            "hand wash" in text -> add(CareRequirement.HAND_WASH_ONLY)
            temperature != null && temperature <= 30 -> add(CareRequirement.COLD_WASH)
            temperature != null && temperature <= 40 -> add(CareRequirement.WARM_WASH)
            temperature != null -> add(CareRequirement.HOT_WASH)
        }
        if ("air dry" in text || "line dry" in text || "do not tumble" in text) add(CareRequirement.AIR_DRY)
        if ("low heat" in text) add(CareRequirement.LOW_HEAT)
        if ("do not bleach" in text || "no bleach" in text) add(CareRequirement.AVOID_BLEACH)
        if ("do not iron" in text || "no iron" in text) add(CareRequirement.AVOID_IRON)
    }.distinct()
