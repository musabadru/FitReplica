package com.fitreplica.feature.closet.list

import com.fitreplica.core.domain.usecase.LogWearEventUseCase
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClosetViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filters items by type through the repository`() =
        runTest(dispatcher) {
            val repository = FakeClothingRepository()
            repository.addItem(sampleItem(id = "top-1", type = ClothingType.TOP))
            repository.addItem(sampleItem(id = "shoe-1", type = ClothingType.SHOES))
            val viewModel = ClosetViewModel(repository, LogWearEventUseCase(repository))
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.onAction(ClosetUiAction.OnTypeFilterChanged(ClothingType.SHOES))
            advanceUntilIdle()

            assertEquals(listOf(ClothingId("shoe-1")), viewModel.uiState.value.items.map { it.id })
        }

    @Test
    fun `wear now logs against the repository`() =
        runTest(dispatcher) {
            val repository = FakeClothingRepository()
            val item = sampleItem(id = "item-1")
            repository.addItem(item)
            val viewModel = ClosetViewModel(repository, LogWearEventUseCase(repository))
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.onAction(ClosetUiAction.OnWearNowClicked(item.id))
            advanceUntilIdle()

            assertEquals(listOf(item.id), repository.wearLog)
        }

    private fun sampleItem(
        id: String,
        type: ClothingType = ClothingType.TOP,
    ) = ClothingItem(
        id = ClothingId(id),
        name = "Item $id",
        type = type,
        brand = null,
        colorPrimary = "blue",
        colorSecondary = null,
        condition = Condition.NEW,
        status = Status.CLEAN,
        size = null,
        timesWorn = 0,
        lastWornAt = null,
        addedAt = 0L,
    )
}
