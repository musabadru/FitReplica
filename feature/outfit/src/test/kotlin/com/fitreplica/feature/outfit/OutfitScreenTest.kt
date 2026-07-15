package com.fitreplica.feature.outfit

import org.junit.Assert.assertEquals
import org.junit.Test

class OutfitScreenTest {
    @Test
    fun `measurement input normalizes comma decimal separator`() {
        assertEquals("74.5", "74,5".filterMeasurementInput())
    }
}
