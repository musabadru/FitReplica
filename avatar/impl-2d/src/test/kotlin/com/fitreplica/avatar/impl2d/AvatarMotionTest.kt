package com.fitreplica.avatar.impl2d

import com.fitreplica.avatar.api.AvatarAnimationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AvatarMotionTest {
    @Test
    fun `animation is enabled only when config and system scale both allow it`() {
        assertEquals(true, isAvatarMotionEnabled(configAnimationEnabled = true, systemAnimatorScale = 1f))
        assertEquals(false, isAvatarMotionEnabled(configAnimationEnabled = false, systemAnimatorScale = 1f))
        assertEquals(false, isAvatarMotionEnabled(configAnimationEnabled = true, systemAnimatorScale = 0f))
        assertEquals(false, isAvatarMotionEnabled(configAnimationEnabled = false, systemAnimatorScale = 0f))
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
}
