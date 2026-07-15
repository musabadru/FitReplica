package com.fitreplica.avatar.api

import com.fitreplica.core.model.BodyMeasurements

data class AvatarConfig(
    val heightCm: Float,
    val chestBustCm: Float?,
    val waistCm: Float?,
    val hipCm: Float?,
    val skinTone: SkinTone,
    val animationEnabled: Boolean,
) {
    companion object {
        const val DEFAULT_HEIGHT_CM = 170f
        private const val MIN_HEIGHT_CM = 120f
        private const val MAX_HEIGHT_CM = 230f
        private const val MIN_CIRCUMFERENCE_CM = 40f
        private const val MAX_CIRCUMFERENCE_CM = 180f

        fun default(skinTone: SkinTone = SkinTone.MEDIUM): AvatarConfig =
            AvatarConfig(
                heightCm = DEFAULT_HEIGHT_CM,
                chestBustCm = null,
                waistCm = null,
                hipCm = null,
                skinTone = skinTone,
                animationEnabled = true,
            )

        fun fromMeasurements(
            measurements: BodyMeasurements?,
            skinTone: SkinTone = SkinTone.MEDIUM,
            animationEnabled: Boolean = true,
        ): AvatarConfig =
            AvatarConfig(
                heightCm = measurements?.heightCm.usableHeightCm() ?: DEFAULT_HEIGHT_CM,
                chestBustCm = measurements?.chestBustCm.usableCircumferenceCm(),
                waistCm = measurements?.waistCm.usableCircumferenceCm(),
                hipCm = measurements?.hipCm.usableCircumferenceCm(),
                skinTone = skinTone,
                animationEnabled = animationEnabled,
            )

        fun isSupportedHeightCm(value: Float): Boolean = value in MIN_HEIGHT_CM..MAX_HEIGHT_CM

        fun isSupportedCircumferenceCm(value: Float): Boolean = value in MIN_CIRCUMFERENCE_CM..MAX_CIRCUMFERENCE_CM

        private fun Float?.usableHeightCm(): Float? {
            return this?.takeIf { isSupportedHeightCm(it) }
        }

        private fun Float?.usableCircumferenceCm(): Float? {
            return this?.takeIf { isSupportedCircumferenceCm(it) }
        }
    }
}
