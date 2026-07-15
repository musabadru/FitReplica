package com.fitreplica.app.avatar

import com.fitreplica.core.domain.repository.AvatarConfigData
import com.fitreplica.core.domain.repository.ThemeMode
import com.fitreplica.core.domain.repository.UserPreferencesData
import com.fitreplica.core.domain.repository.UserPreferencesRepository
import com.fitreplica.core.model.ClothingId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarRendererSelectorTest {
    @Test
    fun `selector maps disabled preference to disabled renderer choice`() =
        runTest {
            val repository = FakeUserPreferencesRepository(UserPreferencesData(avatarModuleEnabled = false))
            val selector = AvatarRendererSelector(repository)

            assertEquals(AvatarRendererChoice.Disabled, selector.choice.first())
        }

    @Test
    fun `selector maps enabled preference to 2d renderer choice`() =
        runTest {
            val repository = FakeUserPreferencesRepository(UserPreferencesData(avatarModuleEnabled = true))
            val selector = AvatarRendererSelector(repository)

            assertEquals(AvatarRendererChoice.TwoD, selector.choice.first())
        }
}

private class FakeUserPreferencesRepository(
    initial: UserPreferencesData,
) : UserPreferencesRepository {
    private val state = MutableStateFlow(initial)

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

    override suspend fun setSelectedOutfitItemIds(itemIds: List<ClothingId>) {
        state.value = state.value.copy(selectedOutfitItemIds = itemIds)
    }
}
