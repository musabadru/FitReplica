package com.fitreplica.core.domain.repository

import com.fitreplica.core.model.ClothingId
import kotlinx.coroutines.flow.Flow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

// Mirrors AvatarConfig from architecture doc §3.1a/§7.1 (the persisted subset — skin
// tone and other Phase 3 rendering fields join once :avatar:impl-2d exists). Default
// height matches avatar.api.AvatarConfig.DEFAULT_HEIGHT_CM — :core:domain can't depend
// on :avatar:api (wrong direction per the module graph), so keep these two in sync by
// hand if either changes.
data class AvatarConfigData(
    val heightCm: Float = DEFAULT_HEIGHT_CM,
    val chestBustCm: Float? = null,
    val waistCm: Float? = null,
    val hipCm: Float? = null,
    val animationEnabled: Boolean = true,
) {
    companion object {
        const val DEFAULT_HEIGHT_CM = 170f
    }
}

data class UserPreferencesData(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val avatarModuleEnabled: Boolean = false,
    val metadataModuleEnabled: Boolean = false,
    val animationsEnabled: Boolean = true,
    val avatarConfig: AvatarConfigData = AvatarConfigData(),
    val selectedOutfitItemIds: List<ClothingId> = emptyList(),
)

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferencesData>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setAvatarModuleEnabled(enabled: Boolean)

    suspend fun setMetadataModuleEnabled(enabled: Boolean)

    suspend fun setAnimationsEnabled(enabled: Boolean)

    suspend fun setAvatarConfig(config: AvatarConfigData)

    suspend fun setSelectedOutfitItemIds(itemIds: List<ClothingId>)
}
