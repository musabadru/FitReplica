package com.fitreplica.avatar.api

import com.fitreplica.core.model.BodyMeasurements
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarConfigTest {
    @Test
    fun `fromMeasurements uses defaults when measurements are missing`() {
        val config = AvatarConfig.fromMeasurements(null)

        assertEquals(AvatarConfig.DEFAULT_HEIGHT_CM, config.heightCm)
        assertNull(config.chestBustCm)
        assertNull(config.waistCm)
        assertNull(config.hipCm)
        assertEquals(SkinTone.MEDIUM, config.skinTone)
        assertTrue(config.animationEnabled)
    }

    @Test
    fun `fromMeasurements treats implausible values as unavailable`() {
        val config =
            AvatarConfig.fromMeasurements(
                BodyMeasurements(
                    heightCm = -10f,
                    chestBustCm = 0f,
                    waistCm = 5_000f,
                    hipCm = 181f,
                ),
            )

        assertEquals(AvatarConfig.DEFAULT_HEIGHT_CM, config.heightCm)
        assertNull(config.chestBustCm)
        assertNull(config.waistCm)
        assertNull(config.hipCm)
    }

    @Test
    fun `fromMeasurements preserves usable partial measurements`() {
        val config =
            AvatarConfig.fromMeasurements(
                BodyMeasurements(
                    heightCm = PARTIAL_HEIGHT_CM,
                    chestBustCm = PARTIAL_CHEST_CM,
                    waistCm = null,
                    hipCm = PARTIAL_HIP_CM,
                ),
                skinTone = SkinTone.DARK,
                animationEnabled = false,
            )

        assertEquals(PARTIAL_HEIGHT_CM, config.heightCm)
        assertEquals(PARTIAL_CHEST_CM, config.chestBustCm)
        assertNull(config.waistCm)
        assertEquals(PARTIAL_HIP_CM, config.hipCm)
        assertEquals(SkinTone.DARK, config.skinTone)
        assertFalse(config.animationEnabled)
    }
}

private const val PARTIAL_HEIGHT_CM = 182f
private const val PARTIAL_CHEST_CM = 96f
private const val PARTIAL_HIP_CM = 102f
