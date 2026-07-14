@file:Suppress("MatchingDeclarationName")

package com.fitreplica.avatar.impl2d

import com.fitreplica.avatar.api.AvatarAnimationState
import kotlin.math.PI
import kotlin.math.sin

internal data class AvatarPoseFrame(
    val shoulderTiltDegrees: Float = 0f,
    val hipShiftFraction: Float = 0f,
    val armSwingDegrees: Float = 0f,
    val legSwingDegrees: Float = 0f,
)

internal fun isAvatarMotionEnabled(
    configAnimationEnabled: Boolean,
    systemAnimatorScale: Float,
): Boolean = configAnimationEnabled && systemAnimatorScale != 0f

internal fun poseFrameFor(
    state: AvatarAnimationState,
    progress: Float,
): AvatarPoseFrame =
    when (state) {
        AvatarAnimationState.WALK -> walkPose(progress)
        AvatarAnimationState.IDLE,
        AvatarAnimationState.TURN,
        AvatarAnimationState.SHOWCASE,
        -> idlePose(progress)
    }

private fun idlePose(progress: Float): AvatarPoseFrame {
    val wave = sin(progress.coerceIn(0f, 1f) * PI.toFloat() * FULL_CYCLE_MULTIPLIER)
    return AvatarPoseFrame(
        shoulderTiltDegrees = wave * IDLE_SHOULDER_TILT_DEGREES,
        hipShiftFraction = wave * IDLE_HIP_SHIFT_FRACTION,
    )
}

private fun walkPose(progress: Float): AvatarPoseFrame {
    val frame = ((progress.coerceIn(0f, 1f) * WALK_FRAME_COUNT).toInt() % WALK_FRAME_COUNT)
    val direction = if (frame % DIRECTION_ALTERNATION_FRAME_COUNT == 0) 1f else -1f
    val stride =
        if (frame in FULL_STRIDE_FRAME_START..FULL_STRIDE_FRAME_END) {
            FULL_STRIDE_SCALE
        } else {
            RECOVERY_STRIDE_SCALE
        }
    return AvatarPoseFrame(
        shoulderTiltDegrees = direction * WALK_SHOULDER_TILT_DEGREES,
        hipShiftFraction = direction * WALK_HIP_SHIFT_FRACTION,
        armSwingDegrees = -direction * WALK_ARM_SWING_DEGREES * stride,
        legSwingDegrees = direction * WALK_LEG_SWING_DEGREES * stride,
    )
}

private const val WALK_FRAME_COUNT = 8
private const val FULL_CYCLE_MULTIPLIER = 2f
private const val DIRECTION_ALTERNATION_FRAME_COUNT = 2
private const val FULL_STRIDE_FRAME_START = 2
private const val FULL_STRIDE_FRAME_END = 5
private const val FULL_STRIDE_SCALE = 1f
private const val RECOVERY_STRIDE_SCALE = 0.55f
private const val IDLE_SHOULDER_TILT_DEGREES = 1.2f
private const val IDLE_HIP_SHIFT_FRACTION = 0.006f
private const val WALK_SHOULDER_TILT_DEGREES = 2f
private const val WALK_HIP_SHIFT_FRACTION = 0.012f
private const val WALK_ARM_SWING_DEGREES = 8f
private const val WALK_LEG_SWING_DEGREES = 7f
