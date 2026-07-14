@file:Suppress("MatchingDeclarationName")

package com.fitreplica.avatar.impl2d

import com.fitreplica.avatar.api.AvatarLayer
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.ClothingType

internal data class ResolvedAvatarLayer(
    val layer: AvatarLayer,
    val item: ClothingItem,
)

internal fun resolveAvatarLayers(
    outfit: List<ClothingItem>,
    logger: AvatarDebugLogger = NoOpAvatarDebugLogger,
): List<ResolvedAvatarLayer> {
    val resolved = linkedMapOf<AvatarLayer, ClothingItem>()

    outfit.forEach { item ->
        val layer =
            item.avatarSlot?.toAvatarLayer()
                ?: item.type.toAvatarLayer()

        if (layer == null || layer == AvatarLayer.Body) {
            logger.log("Unsupported avatar slot for ${item.id.value}: ${item.avatarSlot ?: item.type.name}")
            return@forEach
        }

        resolved[layer]?.let { previous ->
            logger.log(
                "Replacing ${previous.id.value} with ${item.id.value} in avatar layer ${layer.name}",
            )
        }
        resolved[layer] = item
    }

    return resolved
        .map { (layer, item) -> ResolvedAvatarLayer(layer = layer, item = item) }
        .sortedBy { it.layer.zIndex }
}

private fun String.toAvatarLayer(): AvatarLayer? =
    when (lowercase().replace("-", "_").replace(" ", "_")) {
        "base", "base_layer", "under", "under_layer", "top" -> AvatarLayer.Base
        "mid", "mid_layer", "bottom", "dress" -> AvatarLayer.Mid
        "outer", "outer_layer", "outerwear" -> AvatarLayer.Outer
        "shoes", "shoe", "footwear" -> AvatarLayer.Shoes
        else -> null
    }

private fun ClothingType.toAvatarLayer(): AvatarLayer? =
    when (this) {
        ClothingType.TOP,
        ClothingType.UNDERGARMENT,
        -> AvatarLayer.Base
        ClothingType.BOTTOM,
        ClothingType.DRESS,
        -> AvatarLayer.Mid
        ClothingType.OUTERWEAR -> AvatarLayer.Outer
        ClothingType.SHOES -> AvatarLayer.Shoes
        ClothingType.ACCESSORY,
        ClothingType.OTHER,
        -> null
    }
