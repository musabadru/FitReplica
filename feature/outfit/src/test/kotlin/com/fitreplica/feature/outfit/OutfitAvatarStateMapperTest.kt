package com.fitreplica.feature.outfit

import com.fitreplica.avatar.api.AvatarConfig
import com.fitreplica.core.domain.repository.AvatarConfigData
import com.fitreplica.core.domain.repository.UserPreferencesData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OutfitAvatarStateMapperTest {
    @Test
    fun `avatar state maps persisted measurements to avatar config`() {
        val avatarState =
            UserPreferencesData(
                avatarConfig =
                    AvatarConfigData(
                        heightCm = STORED_HEIGHT_CM,
                        chestBustCm = STORED_CHEST_CM,
                        waistCm = STORED_WAIST_CM,
                        hipCm = STORED_HIP_CM,
                    ),
            ).toAvatarState()

        assertEquals(STORED_HEIGHT_CM, avatarState.config.heightCm)
        assertEquals(STORED_CHEST_CM, avatarState.config.chestBustCm)
        assertEquals(STORED_WAIST_CM, avatarState.config.waistCm)
        assertEquals(STORED_HIP_CM, avatarState.config.hipCm)
    }

    @Test
    fun `invalid persisted measurements fall back through avatar config sanitizing`() {
        val avatarState =
            UserPreferencesData(
                avatarConfig =
                    AvatarConfigData(
                        heightCm = -1f,
                        chestBustCm = 0f,
                        waistCm = 5_000f,
                        hipCm = 181f,
                    ),
            ).toAvatarState()

        assertEquals(AvatarConfig.DEFAULT_HEIGHT_CM, avatarState.config.heightCm)
        assertNull(avatarState.config.chestBustCm)
        assertNull(avatarState.config.waistCm)
        assertNull(avatarState.config.hipCm)
    }

    @Test
    fun `avatar animation uses global and avatar config preferences as an AND gate`() {
        assertEquals(true, preferences(global = true, config = true).toAvatarState().config.animationEnabled)
        assertEquals(false, preferences(global = true, config = false).toAvatarState().config.animationEnabled)
        assertEquals(false, preferences(global = false, config = true).toAvatarState().config.animationEnabled)
        assertEquals(false, preferences(global = false, config = false).toAvatarState().config.animationEnabled)
    }
}

private fun preferences(
    global: Boolean,
    config: Boolean,
): UserPreferencesData =
    UserPreferencesData(
        animationsEnabled = global,
        avatarConfig = AvatarConfigData(animationEnabled = config),
    )

private const val STORED_HEIGHT_CM = 166f
private const val STORED_CHEST_CM = 92f
private const val STORED_WAIST_CM = 74f
private const val STORED_HIP_CM = 98f
