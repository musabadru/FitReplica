package com.fitreplica.core.model

data class ConditionEvent(
    val id: ConditionEventId,
    val itemId: ClothingId,
    val previousCondition: Condition,
    val newCondition: Condition,
    val changedAt: Long,
    val notes: String?,
)
