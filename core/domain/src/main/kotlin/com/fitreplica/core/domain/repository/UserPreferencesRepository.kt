package com.fitreplica.core.domain.repository

import kotlinx.coroutines.flow.Flow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

// Mirrors AvatarConfig from architecture doc §3.1a/§7.1 (the persisted subset — skin
// tone and other Phase 3 rendering fields join once :avatar:impl-2d exists).
data class AvatarConfigData(
    val heightCm: Float = 0f,
    val chestBustCm: Float? = null,
    val waistCm: Float? = null,
    val hipCm: Float? = null,
    val animationEnabled: Boolean = true,
)

data class UserPreferencesData(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val avatarModuleEnabled: Boolean = false,
    val metadataModuleEnabled: Boolean = false,
    val animationsEnabled: Boolean = true,
    val avatarConfig: AvatarConfigData = AvatarConfigData(),
)

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferencesData>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setAvatarModuleEnabled(enabled: Boolean)

    suspend fun setMetadataModuleEnabled(enabled: Boolean)

    suspend fun setAnimationsEnabled(enabled: Boolean)

    suspend fun setAvatarConfig(config: AvatarConfigData)
}
