@file:Suppress("MatchingDeclarationName")

package com.fitreplica.avatar.impl2d

import com.fitreplica.avatar.api.AvatarConfig

internal enum class SilhouetteVariant(
    val shoulderScale: Float,
    val waistScale: Float,
    val hipScale: Float,
    val heightScale: Float,
) {
    Neutral(shoulderScale = 1f, waistScale = 1f, hipScale = 1f, heightScale = 1f),
    Compact(shoulderScale = 0.98f, waistScale = 1.02f, hipScale = 1.02f, heightScale = 0.94f),
    TallLean(shoulderScale = 0.95f, waistScale = 0.92f, hipScale = 0.94f, heightScale = 1.07f),
    TopFuller(shoulderScale = 1.1f, waistScale = 0.97f, hipScale = 0.96f, heightScale = 1f),
    HipFuller(shoulderScale = 0.96f, waistScale = 0.98f, hipScale = 1.12f, heightScale = 1f),
    CenterFuller(shoulderScale = 1f, waistScale = 1.12f, hipScale = 1.04f, heightScale = 0.99f),
}

internal fun selectSilhouette(config: AvatarConfig): SilhouetteVariant {
    val height = config.heightCm.takeIf { it != AvatarConfig.DEFAULT_HEIGHT_CM }
    val chest = config.chestBustCm
    val waist = config.waistCm
    val hip = config.hipCm
    val hasCircumference = chest != null || waist != null || hip != null

    if (height == null && !hasCircumference) {
        return SilhouetteVariant.Neutral
    }

    // Decision flow: body dominance from available ratios wins first; height-only partial
    // profiles then fall back to compact/tall-lean before the neutral silhouette.
    val dominantSilhouette = selectDominantSilhouette(chest = chest, waist = waist, hip = hip)

    return when {
        dominantSilhouette != null -> dominantSilhouette
        height != null && height < COMPACT_HEIGHT_CM -> SilhouetteVariant.Compact
        height != null && height > TALL_HEIGHT_CM -> SilhouetteVariant.TallLean
        else -> SilhouetteVariant.Neutral
    }
}

private fun selectDominantSilhouette(
    chest: Float?,
    waist: Float?,
    hip: Float?,
): SilhouetteVariant? =
    when {
        ratioAtLeast(chest, hip, TOP_TO_HIP_DOMINANCE_RATIO) ||
            ratioAtLeast(chest, waist, TOP_TO_WAIST_DOMINANCE_RATIO) -> SilhouetteVariant.TopFuller
        ratioAtLeast(hip, chest, HIP_TO_CHEST_DOMINANCE_RATIO) ||
            ratioAtLeast(hip, waist, HIP_TO_WAIST_DOMINANCE_RATIO) -> SilhouetteVariant.HipFuller
        ratioAtLeast(waist, chest, CENTER_TO_CHEST_DOMINANCE_RATIO) ||
            ratioAtLeast(waist, hip, CENTER_TO_HIP_DOMINANCE_RATIO) -> SilhouetteVariant.CenterFuller
        else -> null
    }

private fun ratioAtLeast(
    numerator: Float?,
    denominator: Float?,
    threshold: Float,
): Boolean {
    return numerator != null &&
        denominator != null &&
        denominator > 0f &&
        numerator / denominator >= threshold
}

private const val COMPACT_HEIGHT_CM = 160f
private const val TALL_HEIGHT_CM = 180f
private const val TOP_TO_HIP_DOMINANCE_RATIO = 1.08f
private const val TOP_TO_WAIST_DOMINANCE_RATIO = 1.25f
private const val HIP_TO_CHEST_DOMINANCE_RATIO = 1.08f
private const val HIP_TO_WAIST_DOMINANCE_RATIO = 1.3f
private const val CENTER_TO_CHEST_DOMINANCE_RATIO = 0.88f
private const val CENTER_TO_HIP_DOMINANCE_RATIO = 0.86f
