package com.fitreplica.feature.history

import com.fitreplica.core.domain.repository.ClosetFilter
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.OutfitId
import com.fitreplica.core.model.WearEventId
import com.fitreplica.core.model.WearHistoryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
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
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
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
    fun `groups timeline entries by local day`() =
        runTest(dispatcher) {
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val repository =
                FakeHistoryRepository(
                    listOf(
                        historyEntry("event-1", "Blue Jacket", atStartOfDay(today)),
                        historyEntry("event-2", "White Sneakers", atStartOfDay(yesterday)),
                    ),
                )
            val viewModel = HistoryViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertEquals(
                listOf(today, yesterday),
                viewModel.uiState.value.timelineGroups.map { it.date },
            )
            assertEquals(
                listOf("Blue Jacket"),
                viewModel.uiState.value.timelineGroups.first().entries.map { it.itemName },
            )
        }

    @Test
    fun `calendar shows month counts and selected day details`() =
        runTest(dispatcher) {
            val today = LocalDate.now()
            val repository =
                FakeHistoryRepository(
                    listOf(
                        historyEntry("event-1", "Blue Jacket", atStartOfDay(today)),
                        historyEntry("event-2", "White Sneakers", atStartOfDay(today)),
                    ),
                )
            val viewModel = HistoryViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            val selectedDay = viewModel.uiState.value.calendarDays.single { it.date == today }

            assertEquals(2, selectedDay.wearCount)
            assertEquals(
                listOf("Blue Jacket", "White Sneakers"),
                viewModel.uiState.value.selectedDayEntries.map { it.itemName },
            )
        }

    @Test
    fun `month navigation updates the visible calendar month`() =
        runTest(dispatcher) {
            val viewModel = HistoryViewModel(FakeHistoryRepository(emptyList()))
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            val initialMonth = viewModel.uiState.value.visibleMonth

            viewModel.onAction(HistoryUiAction.OnPreviousMonthClicked)
            advanceUntilIdle()

            assertEquals(initialMonth.minusMonths(1), viewModel.uiState.value.visibleMonth)
            assertEquals(initialMonth.minusMonths(1), java.time.YearMonth.from(viewModel.uiState.value.selectedDate))

            viewModel.onAction(HistoryUiAction.OnNextMonthClicked)
            advanceUntilIdle()

            assertEquals(initialMonth, viewModel.uiState.value.visibleMonth)
            assertEquals(initialMonth, java.time.YearMonth.from(viewModel.uiState.value.selectedDate))
        }

    @Test
    fun `mode changed action switches from timeline to calendar`() =
        runTest(dispatcher) {
            val viewModel = HistoryViewModel(FakeHistoryRepository(emptyList()))
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertEquals(HistoryMode.TIMELINE, viewModel.uiState.value.mode)

            viewModel.onAction(HistoryUiAction.OnModeChanged(HistoryMode.CALENDAR))
            advanceUntilIdle()

            assertEquals(HistoryMode.CALENDAR, viewModel.uiState.value.mode)
        }

    @Test
    fun `date selected action updates selected date and visible month`() =
        runTest(dispatcher) {
            val viewModel = HistoryViewModel(FakeHistoryRepository(emptyList()))
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            val selectedDate = LocalDate.now().minusMonths(2).withDayOfMonth(1)

            viewModel.onAction(HistoryUiAction.OnDateSelected(selectedDate))
            advanceUntilIdle()

            assertEquals(selectedDate, viewModel.uiState.value.selectedDate)
            assertEquals(java.time.YearMonth.from(selectedDate), viewModel.uiState.value.visibleMonth)
        }

    @Test
    fun `repository failure emits recoverable error state`() =
        runTest(dispatcher) {
            val viewModel =
                HistoryViewModel(
                    FakeHistoryRepository(
                        entries = emptyList(),
                        failuresBeforeSuccess = 1,
                    ),
                )
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertEquals(false, viewModel.uiState.value.isLoading)
            assertEquals("Unable to load wear history. Please try again.", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `retry action resubscribes after repository failure`() =
        runTest(dispatcher) {
            val today = LocalDate.now()
            val viewModel =
                HistoryViewModel(
                    FakeHistoryRepository(
                        entries = listOf(historyEntry("event-1", "Blue Jacket", atStartOfDay(today))),
                        failuresBeforeSuccess = 1,
                    ),
                )
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertEquals("Unable to load wear history. Please try again.", viewModel.uiState.value.errorMessage)

            viewModel.onAction(HistoryUiAction.OnRetryClicked)
            advanceUntilIdle()

            assertEquals(null, viewModel.uiState.value.errorMessage)
            assertEquals(listOf("Blue Jacket"), viewModel.uiState.value.entries.map { it.itemName })
        }

    private fun atStartOfDay(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private class FakeHistoryRepository(
    entries: List<WearHistoryEntry>,
    failuresBeforeSuccess: Int = 0,
) : ClothingRepository {
    private val history = MutableStateFlow(entries)
    private var remainingFailures = failuresBeforeSuccess

    override fun observeItems(filter: ClosetFilter): Flow<List<ClothingItem>> = emptyFlow()

    override fun observeItem(itemId: ClothingId): Flow<ClothingItem?> = emptyFlow()

    override fun observeWearHistory(): Flow<List<WearHistoryEntry>> =
        flow {
            if (remainingFailures > 0) {
                remainingFailures -= 1
                error("boom")
            }
            emitAll(history)
        }

    override suspend fun addItem(item: ClothingItem) = Unit

    override suspend fun updateItem(item: ClothingItem) = Unit

    override suspend fun deleteItem(itemId: ClothingId) = Unit

    override suspend fun logWear(
        itemId: ClothingId,
        outfitId: OutfitId?,
        context: String?,
    ) = Unit

    override suspend fun updateCondition(
        itemId: ClothingId,
        condition: Condition,
    ) = Unit
}

private fun historyEntry(
    id: String,
    itemName: String,
    wornAt: Long,
) = WearHistoryEntry(
    id = WearEventId(id),
    itemId = ClothingId("item-$id"),
    itemName = itemName,
    itemType = ClothingType.TOP,
    colorPrimary = "blue",
    outfitId = null,
    wornAt = wornAt,
    context = null,
    notes = null,
)
