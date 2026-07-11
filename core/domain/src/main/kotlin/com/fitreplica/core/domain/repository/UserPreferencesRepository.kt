package com.fitreplica.core.domain.repository

import kotlinx.coroutines.flow.Flow

data class UserPreferencesData(
    val avatarModuleEnabled: Boolean = false,
    val metadataModuleEnabled: Boolean = false,
    val animationsEnabled: Boolean = true,
)

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferencesData>

    suspend fun setAvatarModuleEnabled(enabled: Boolean)

    suspend fun setMetadataModuleEnabled(enabled: Boolean)
}
