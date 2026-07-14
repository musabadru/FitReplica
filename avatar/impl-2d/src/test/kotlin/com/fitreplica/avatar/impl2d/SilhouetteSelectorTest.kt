package com.fitreplica.avatar.impl2d

import com.fitreplica.avatar.api.AvatarConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class SilhouetteSelectorTest {
    @Test
    fun `missing measurements select neutral`() {
        assertEquals(SilhouetteVariant.Neutral, selectSilhouette(AvatarConfig.default()))
    }

    @Test
    fun `height-only partial measurements select compact or tall lean`() {
        assertEquals(
            SilhouetteVariant.Compact,
            selectSilhouette(config(heightCm = 155f)),
        )
        assertEquals(
            SilhouetteVariant.TallLean,
            selectSilhouette(config(heightCm = 184f)),
        )
    }

    @Test
    fun `top dominant ratios select top fuller`() {
        assertEquals(
            SilhouetteVariant.TopFuller,
            selectSilhouette(config(chestBustCm = 104f, waistCm = 78f, hipCm = 92f)),
        )
    }

    @Test
    fun `hip dominant ratios select hip fuller`() {
        assertEquals(
            SilhouetteVariant.HipFuller,
            selectSilhouette(config(chestBustCm = 88f, waistCm = 72f, hipCm = 104f)),
        )
    }

    @Test
    fun `waist dominant partial ratios select center fuller`() {
        assertEquals(
            SilhouetteVariant.CenterFuller,
            selectSilhouette(config(chestBustCm = 98f, waistCm = 90f)),
        )
    }

    @Test
    fun `average partial measurements without dominance select neutral`() {
        assertEquals(
            SilhouetteVariant.Neutral,
            selectSilhouette(config(chestBustCm = 96f, hipCm = 98f)),
        )
    }
}

private fun config(
    heightCm: Float = AvatarConfig.DEFAULT_HEIGHT_CM,
    chestBustCm: Float? = null,
    waistCm: Float? = null,
    hipCm: Float? = null,
): AvatarConfig =
    AvatarConfig.default().copy(
        heightCm = heightCm,
        chestBustCm = chestBustCm,
        waistCm = waistCm,
        hipCm = hipCm,
    )
