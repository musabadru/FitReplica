package com.fitreplica.feature.outfit

import com.fitreplica.core.domain.repository.AvatarConfigData
import com.fitreplica.core.domain.repository.ClosetFilter
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.domain.repository.ThemeMode
import com.fitreplica.core.domain.repository.UserPreferencesData
import com.fitreplica.core.domain.repository.UserPreferencesRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.ConditionEvent
import com.fitreplica.core.model.OutfitId
import com.fitreplica.core.model.Status
import com.fitreplica.core.model.WearHistoryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
class OutfitViewModelTest {
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
    fun `measurement edits persist to avatar config and update text input state`() =
        runTest(dispatcher) {
            val preferencesRepository = FakeUserPreferencesRepository()
            val viewModel = OutfitViewModel(preferencesRepository, FakeClothingRepository())
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.onAction(OutfitUiAction.OnMeasurementChanged(MeasurementField.Waist, WAIST_INPUT))
            advanceUntilIdle()

            assertEquals(WAIST_INPUT, viewModel.uiState.value.measurements.textFor(MeasurementField.Waist))
            assertEquals(WAIST_CM, preferencesRepository.state.value.avatarConfig.waistCm)
        }

    @Test
    fun `avatar module toggle persists to preferences`() =
        runTest(dispatcher) {
            val preferencesRepository = FakeUserPreferencesRepository()
            val viewModel = OutfitViewModel(preferencesRepository, FakeClothingRepository())
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.onAction(OutfitUiAction.OnAvatarModuleEnabledChanged(true))
            advanceUntilIdle()

            assertEquals(true, preferencesRepository.state.value.avatarModuleEnabled)
            assertEquals(true, viewModel.uiState.value.avatarModuleEnabled)
        }

    @Test
    fun `selected closet items feed the avatar preview outfit in selection order`() =
        runTest(dispatcher) {
            val shirt = sampleItem(id = "shirt-1", type = ClothingType.TOP)
            val shoes = sampleItem(id = "shoe-1", type = ClothingType.SHOES)
            val clothingRepository = FakeClothingRepository(listOf(shirt, shoes))
            val viewModel = OutfitViewModel(FakeUserPreferencesRepository(), clothingRepository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.onAction(OutfitUiAction.OnItemSelectionToggled(shoes.id))
            viewModel.onAction(OutfitUiAction.OnItemSelectionToggled(shirt.id))
            advanceUntilIdle()

            assertEquals(listOf(shoes.id, shirt.id), viewModel.uiState.value.avatarState.outfit.map { it.id })
        }
}

private class FakeUserPreferencesRepository(
    initial: UserPreferencesData = UserPreferencesData(),
) : UserPreferencesRepository {
    val state = MutableStateFlow(initial)

    override val userPreferences: Flow<UserPreferencesData> = state

    override suspend fun setThemeMode(mode: ThemeMode) {
        state.value = state.value.copy(themeMode = mode)
    }

    override suspend fun setAvatarModuleEnabled(enabled: Boolean) {
        state.value = state.value.copy(avatarModuleEnabled = enabled)
    }

    override suspend fun setMetadataModuleEnabled(enabled: Boolean) {
        state.value = state.value.copy(metadataModuleEnabled = enabled)
    }

    override suspend fun setAnimationsEnabled(enabled: Boolean) {
        state.value = state.value.copy(animationsEnabled = enabled)
    }

    override suspend fun setAvatarConfig(config: AvatarConfigData) {
        state.value = state.value.copy(avatarConfig = config)
    }
}

private class FakeClothingRepository(
    items: List<ClothingItem> = emptyList(),
) : ClothingRepository {
    private val itemState = MutableStateFlow(items)

    override fun observeItems(filter: ClosetFilter): Flow<List<ClothingItem>> = itemState

    override fun observeItem(itemId: ClothingId): Flow<ClothingItem?> = emptyFlow()

    override fun observeWearHistory(): Flow<List<WearHistoryEntry>> = emptyFlow()

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
        notes: String?,
    ) = Unit

    override fun observeConditionEvents(itemId: ClothingId): Flow<List<ConditionEvent>> = emptyFlow()
}

private fun sampleItem(
    id: String,
    type: ClothingType,
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

private const val WAIST_INPUT = "74.5"
private const val WAIST_CM = 74.5f
