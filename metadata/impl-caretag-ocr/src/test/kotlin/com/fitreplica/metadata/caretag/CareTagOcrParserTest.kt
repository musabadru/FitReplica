package com.fitreplica.metadata.caretag

import com.fitreplica.metadata.api.CareRequirement
import com.fitreplica.metadata.api.CareTagSymbol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val HOT_WASH_TEMPERATURE_CELSIUS = 100

class CareTagOcrParserTest {
    @Test
    fun `parses negative dry clean before positive dry clean`() {
        val result = "Do not dry clean. Machine wash.".toCareTagResult()

        assertTrue(CareTagSymbol.DO_NOT_DRY_CLEAN in result.recognizedSymbols)
        assertTrue(CareTagSymbol.DRY_CLEAN !in result.recognizedSymbols)
        assertTrue(CareRequirement.AVOID_DRY_CLEAN in result.careRequirements)
        assertTrue(CareRequirement.DRY_CLEAN_ONLY !in result.careRequirements)
    }

    @Test
    fun `keeps independent care requirements`() {
        val result = "Do not wash. Dry clean only. Do not iron.".toCareTagResult()

        assertTrue(CareRequirement.DO_NOT_WASH in result.careRequirements)
        assertTrue(CareRequirement.DRY_CLEAN_ONLY in result.careRequirements)
        assertTrue(CareRequirement.AVOID_IRON in result.careRequirements)
    }

    @Test
    fun `parses three digit wash temperature without partial match`() {
        val result = "Wash ${HOT_WASH_TEMPERATURE_CELSIUS}°c".toCareTagResult()

        assertEquals(HOT_WASH_TEMPERATURE_CELSIUS, result.suggestedWashTemperatureCelsius)
        assertTrue(CareRequirement.HOT_WASH in result.careRequirements)
    }
}
