package com.fitreplica.core.domain.fake

import com.fitreplica.core.domain.repository.LaundryRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.LaundryLoad
import com.fitreplica.core.model.LaundryLoadId
import kotlinx.coroutines.flow.MutableStateFlow

class FakeLaundryRepository : LaundryRepository {
    private val loads = MutableStateFlow<List<LaundryLoad>>(emptyList())
    val createdLoads = mutableListOf<List<ClothingId>>()
    val completedLoads = mutableListOf<LaundryLoadId>()

    override fun observeLoads() = loads

    override suspend fun createLoad(itemIds: List<ClothingId>) {
        createdLoads += itemIds
        loads.value =
            listOf(
                LaundryLoad(
                    id = LaundryLoadId("fake-load-${createdLoads.size}"),
                    startedAt = createdLoads.size.toLong(),
                    completedAt = null,
                    itemIds = itemIds,
                ),
            ) + loads.value
    }

    override suspend fun completeLoad(loadId: LaundryLoadId) {
        completedLoads += loadId
        loads.value =
            loads.value.map { load ->
                if (load.id == loadId) {
                    load.copy(completedAt = completedLoads.size.toLong())
                } else {
                    load
                }
            }
    }
}
