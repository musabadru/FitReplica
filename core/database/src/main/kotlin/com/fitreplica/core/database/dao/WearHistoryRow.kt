package com.fitreplica.core.database.dao

import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.OutfitId
import com.fitreplica.core.model.WearEventId

data class WearHistoryRow(
    val id: WearEventId,
    val itemId: ClothingId,
    val itemName: String,
    val itemType: ClothingType,
    val colorPrimary: String,
    val outfitId: OutfitId?,
    val wornAt: Long,
    val context: String?,
    val notes: String?,
)
