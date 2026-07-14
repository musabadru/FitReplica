package com.fitreplica.core.model

data class LaundryLoad(
    val id: LaundryLoadId,
    val startedAt: Long,
    val completedAt: Long?,
    val itemIds: List<ClothingId>,
)
