package com.fitreplica.avatar.impl2d

import com.fitreplica.avatar.api.AvatarAnimationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarMotionTest {
    @Test
    fun `animation is enabled only when config and system scale both allow it`() {
        assertTrue(isAvatarMotionEnabled(configAnimationEnabled = true, systemAnimatorScale = 1f))
        assertFalse(isAvatarMotionEnabled(configAnimationEnabled = false, systemAnimatorScale = 1f))
        assertFalse(isAvatarMotionEnabled(configAnimationEnabled = true, systemAnimatorScale = 0f))
        assertFalse(isAvatarMotionEnabled(configAnimationEnabled = false, systemAnimatorScale = 0f))
    }

    @Test
    fun `unsupported animation states fall back to idle pose`() {
        val idle = poseFrameFor(AvatarAnimationState.IDLE, progress = 0.25f)

        assertEquals(idle, poseFrameFor(AvatarAnimationState.TURN, progress = 0.25f))
        assertEquals(idle, poseFrameFor(AvatarAnimationState.SHOWCASE, progress = 0.25f))
    }

    @Test
    fun `walk pose uses procedural keyframes`() {
        val first = poseFrameFor(AvatarAnimationState.WALK, progress = 0f)
        val next = poseFrameFor(AvatarAnimationState.WALK, progress = 0.2f)

        assertNotEquals(first, next)
    }

    @Test
    fun `shoe position follows scaled leg endpoint`() {
        assertEquals(
            COMPACT_SHOE_Y,
            shoeTopY(avatarHeight = AVATAR_HEIGHT, silhouette = SilhouetteVariant.Compact),
            DELTA,
        )
        assertEquals(
            TALL_LEAN_SHOE_Y,
            shoeTopY(avatarHeight = AVATAR_HEIGHT, silhouette = SilhouetteVariant.TallLean),
            DELTA,
        )
    }
}

private const val AVATAR_HEIGHT = 1_000f
private const val COMPACT_SHOE_Y = 860.2f
private const val TALL_LEAN_SHOE_Y = 903.1f
private const val DELTA = 0.01f
