package com.fitreplica.feature.outfit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MeasurementInputsTest {
    @Test
    fun `first invalid field is null when all measurements are supported`() {
        val inputs =
            MeasurementInputs(
                heightCm = "170",
                chestBustCm = "91",
                waistCm = "74.5",
                hipCm = "98",
            )

        assertNull(inputs.firstInvalidField)
    }

    @Test
    fun `first invalid field catches missing unparsable and unsupported measurements`() {
        assertEquals(
            MeasurementField.Height,
            validInputs.copy(heightCm = "10").firstInvalidField,
        )
        assertEquals(
            MeasurementField.Chest,
            validInputs.copy(chestBustCm = ".").firstInvalidField,
        )
        assertEquals(
            MeasurementField.Waist,
            validInputs.copy(waistCm = "").firstInvalidField,
        )
        assertEquals(
            MeasurementField.Hip,
            validInputs.copy(hipCm = "181").firstInvalidField,
        )
    }
}

private val validInputs =
    MeasurementInputs(
        heightCm = "170",
        chestBustCm = "91",
        waistCm = "74.5",
        hipCm = "98",
    )
