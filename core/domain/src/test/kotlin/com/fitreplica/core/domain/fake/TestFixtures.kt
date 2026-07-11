package com.fitreplica.core.domain.fake

import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.Status

fun sampleClothingItem(
    id: String = "item-1",
    name: String = "Blue Nike Jacket",
    condition: Condition = Condition.NEW,
): ClothingItem =
    ClothingItem(
        id = ClothingId(id),
        name = name,
        type = ClothingType.OUTERWEAR,
        brand = "Nike",
        colorPrimary = "blue",
        colorSecondary = null,
        condition = condition,
        status = Status.CLEAN,
        size = null,
        timesWorn = 0,
        lastWornAt = null,
        addedAt = 0L,
    )
