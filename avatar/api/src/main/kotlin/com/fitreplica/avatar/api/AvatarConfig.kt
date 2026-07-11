package com.fitreplica.avatar.api

/**
 * Derived from UserProfileEntity (height, waist/chest/hip) once that entity lands
 * in :core:database (Phase 1, issue #11) — a fromUserProfile(...) factory arrives then.
 * No separate BodyType enum: the renderer infers silhouette from these ratios directly.
 */
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

        fun default(skinTone: SkinTone = SkinTone.MEDIUM): AvatarConfig =
            AvatarConfig(
                heightCm = DEFAULT_HEIGHT_CM,
                chestBustCm = null,
                waistCm = null,
                hipCm = null,
                skinTone = skinTone,
                animationEnabled = true,
            )
    }
}
