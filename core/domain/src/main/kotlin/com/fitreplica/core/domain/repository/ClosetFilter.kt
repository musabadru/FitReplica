package com.fitreplica.core.domain.repository

import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.Status

data class ClosetFilter(
    val searchQuery: String? = null,
    val type: ClothingType? = null,
    val status: Status? = null,
    val condition: Condition? = null,
    val brand: String? = null,
    val colorPrimary: String? = null,
)
