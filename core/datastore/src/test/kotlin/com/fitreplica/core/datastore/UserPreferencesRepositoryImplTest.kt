package com.fitreplica.core.datastore

import androidx.datastore.core.DataStore
import com.fitreplica.core.domain.repository.AvatarConfigData
import com.fitreplica.core.model.ClothingId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeDataStore(initial: UserPreferences) : DataStore<UserPreferences> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<UserPreferences> = state

    override suspend fun updateData(transform: suspend (UserPreferences) -> UserPreferences): UserPreferences {
        state.value = transform(state.value)
        return state.value
    }
}

class UserPreferencesRepositoryImplTest {
    @Test
    fun `never-written preferences report AvatarConfigData defaults, not proto zero-values`() =
        runTest {
            val repository = UserPreferencesRepositoryImpl(FakeDataStore(UserPreferences.getDefaultInstance()))

            val avatarConfig = repository.userPreferences.first().avatarConfig

            assertEquals(AvatarConfigData(), avatarConfig)
            assertEquals(AvatarConfigData.DEFAULT_HEIGHT_CM, avatarConfig.heightCm)
            assertEquals(true, avatarConfig.animationEnabled)
        }

    @Test
    fun `explicitly set avatar config round-trips through the proto`() =
        runTest {
            val repository = UserPreferencesRepositoryImpl(FakeDataStore(UserPreferences.getDefaultInstance()))
            val config =
                AvatarConfigData(
                    heightCm = 165f,
                    chestBustCm = 90f,
                    waistCm = 70f,
                    hipCm = 95f,
                    animationEnabled = false,
                )

            repository.setAvatarConfig(config)

            assertEquals(config, repository.userPreferences.first().avatarConfig)
        }

    @Test
    fun `selected outfit item ids round-trip through the proto`() =
        runTest {
            val repository = UserPreferencesRepositoryImpl(FakeDataStore(UserPreferences.getDefaultInstance()))
            val itemIds = listOf(ClothingId("shoe-1"), ClothingId("shirt-1"))

            repository.setSelectedOutfitItemIds(itemIds)

            assertEquals(itemIds, repository.userPreferences.first().selectedOutfitItemIds)
        }
}
