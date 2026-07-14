package com.fitreplica.metadata.caretag

import com.fitreplica.metadata.api.CareRequirement
import com.fitreplica.metadata.api.CareTagMetadataProvider
import com.fitreplica.metadata.api.CareTagScanResult
import com.fitreplica.metadata.api.CareTagSymbol
import javax.inject.Inject

private const val COLD_WASH_MAX_CELSIUS = 30
private const val WARM_WASH_MAX_CELSIUS = 40
private val TEMPERATURE_REGEX = Regex("""\b(\d{2,3})\s?(c|°c|degrees)\b""")

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
    val hasNoDryClean = normalized.hasNoDryClean
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
            if (hasNoDryClean) {
                add(CareTagSymbol.DO_NOT_DRY_CLEAN)
            } else if ("dry clean" in normalized) {
                add(CareTagSymbol.DRY_CLEAN)
            }
        }.distinct()
    val temperature = TEMPERATURE_REGEX.find(normalized)?.groupValues?.get(1)?.toIntOrNull()
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
        CareRequirement.DO_NOT_WASH.takeIf { text.hasNoWash },
        CareRequirement.DRY_CLEAN_ONLY.takeIf { text.requiresDryClean },
        CareRequirement.AVOID_DRY_CLEAN.takeIf { text.hasNoDryClean },
        CareRequirement.HAND_WASH_ONLY.takeIf { text.hasHandWash },
        temperature.washTemperatureRequirement(),
        CareRequirement.AIR_DRY.takeIf { text.requiresAirDry },
        CareRequirement.LOW_HEAT.takeIf { "low heat" in text },
        CareRequirement.AVOID_BLEACH.takeIf { text.hasNoBleach },
        CareRequirement.AVOID_IRON.takeIf { text.hasNoIron },
    ).distinct()

private fun Int?.washTemperatureRequirement(): CareRequirement? =
    when {
        this == null -> null
        this <= COLD_WASH_MAX_CELSIUS -> CareRequirement.COLD_WASH
        this <= WARM_WASH_MAX_CELSIUS -> CareRequirement.WARM_WASH
        else -> CareRequirement.HOT_WASH
    }

private val String.hasNoWash: Boolean get() = "do not wash" in this || "no wash" in this
private val String.hasHandWash: Boolean get() = "hand wash" in this
private val String.hasNoTumbleDry: Boolean get() = "do not tumble" in this || "no tumble" in this
private val String.hasNoIron: Boolean get() = "do not iron" in this || "no iron" in this
private val String.hasNoBleach: Boolean get() = "do not bleach" in this || "no bleach" in this
private val String.hasNoDryClean: Boolean get() = "do not dry clean" in this || "no dry clean" in this
private val String.requiresDryClean: Boolean get() = "dry clean" in this && !hasNoDryClean
private val String.requiresAirDry: Boolean get() = "air dry" in this || "line dry" in this || hasNoTumbleDry
