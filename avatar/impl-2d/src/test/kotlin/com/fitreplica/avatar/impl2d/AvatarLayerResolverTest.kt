package com.fitreplica.avatar.impl2d

import com.fitreplica.avatar.api.AvatarLayer
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.Status
import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarLayerResolverTest {
    @Test
    fun `resolves explicit slots before clothing type fallback and sorts by z order`() {
        val outfit =
            listOf(
                item("coat", ClothingType.TOP, avatarSlot = "outer"),
                item("shoes", ClothingType.SHOES),
                item("shirt", ClothingType.TOP),
            )

        val resolved = resolveAvatarLayers(outfit)

        assertEquals(
            listOf(AvatarLayer.Base, AvatarLayer.Outer, AvatarLayer.Shoes),
            resolved.map { it.layer },
        )
        assertEquals(listOf("shirt", "coat", "shoes"), resolved.map { it.item.id.value })
    }

    @Test
    fun `last item wins when multiple items target one layer`() {
        val messages = mutableListOf<String>()
        val resolved =
            resolveAvatarLayers(
                outfit =
                    listOf(
                        item("first-shirt", ClothingType.TOP),
                        item("second-shirt", ClothingType.TOP),
                    ),
                logger = messages::add,
            )

        assertEquals(listOf("second-shirt"), resolved.map { it.item.id.value })
        assertEquals(
            listOf("Replacing first-shirt with second-shirt in avatar layer Base"),
            messages,
        )
    }

    @Test
    fun `unsupported explicit slots are logged and ignored`() {
        val messages = mutableListOf<String>()
        val resolved =
            resolveAvatarLayers(
                outfit = listOf(item("watch", ClothingType.ACCESSORY, avatarSlot = "wrist")),
                logger = messages::add,
            )

        assertEquals(emptyList<ResolvedAvatarLayer>(), resolved)
        assertEquals(listOf("Unsupported avatar slot for watch: wrist"), messages)
    }
}

private fun item(
    id: String,
    type: ClothingType,
    avatarSlot: String? = null,
): ClothingItem =
    ClothingItem(
        id = ClothingId(id),
        name = id,
        type = type,
        brand = null,
        colorPrimary = "blue",
        colorSecondary = null,
        condition = Condition.GOOD,
        status = Status.CLEAN,
        size = null,
        lastWornAt = null,
        addedAt = 0L,
        avatarSlot = avatarSlot,
    )
