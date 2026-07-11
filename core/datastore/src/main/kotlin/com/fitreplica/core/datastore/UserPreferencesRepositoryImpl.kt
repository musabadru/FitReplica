package com.fitreplica.core.datastore

import androidx.datastore.core.DataStore
import com.fitreplica.core.domain.repository.AvatarConfigData
import com.fitreplica.core.domain.repository.ThemeMode
import com.fitreplica.core.domain.repository.UserPreferencesData
import com.fitreplica.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl
    @Inject
    constructor(
        private val userPreferencesStore: DataStore<UserPreferences>,
    ) : UserPreferencesRepository {
        override val userPreferences: Flow<UserPreferencesData> =
            userPreferencesStore.data.map { proto ->
                UserPreferencesData(
                    themeMode = proto.themeMode.toDomain(),
                    avatarModuleEnabled = proto.avatarModuleEnabled,
                    metadataModuleEnabled = proto.metadataModuleEnabled,
                    animationsEnabled = proto.animationsEnabled,
                    avatarConfig = if (proto.hasAvatarConfig()) proto.avatarConfig.toDomain() else AvatarConfigData(),
                )
            }

        override suspend fun setThemeMode(mode: ThemeMode) {
            userPreferencesStore.updateData { it.toBuilder().setThemeMode(mode.toProto()).build() }
        }

        override suspend fun setAvatarModuleEnabled(enabled: Boolean) {
            userPreferencesStore.updateData { it.toBuilder().setAvatarModuleEnabled(enabled).build() }
        }

        override suspend fun setMetadataModuleEnabled(enabled: Boolean) {
            userPreferencesStore.updateData { it.toBuilder().setMetadataModuleEnabled(enabled).build() }
        }

        override suspend fun setAnimationsEnabled(enabled: Boolean) {
            userPreferencesStore.updateData { it.toBuilder().setAnimationsEnabled(enabled).build() }
        }

        override suspend fun setAvatarConfig(config: AvatarConfigData) {
            userPreferencesStore.updateData {
                it.toBuilder().setAvatarConfig(config.toProto()).build()
            }
        }
    }

private fun com.fitreplica.core.datastore.ThemeMode.toDomain(): ThemeMode =
    when (this) {
        com.fitreplica.core.datastore.ThemeMode.LIGHT -> ThemeMode.LIGHT
        com.fitreplica.core.datastore.ThemeMode.DARK -> ThemeMode.DARK
        else -> ThemeMode.SYSTEM
    }

private fun ThemeMode.toProto(): com.fitreplica.core.datastore.ThemeMode =
    when (this) {
        ThemeMode.SYSTEM -> com.fitreplica.core.datastore.ThemeMode.SYSTEM
        ThemeMode.LIGHT -> com.fitreplica.core.datastore.ThemeMode.LIGHT
        ThemeMode.DARK -> com.fitreplica.core.datastore.ThemeMode.DARK
    }

// Only call when hasAvatarConfig() is true — the default proto instance's zero-values
// (heightCm=0, animationEnabled=false) don't match AvatarConfigData's own defaults, so
// an absent sub-message must map to AvatarConfigData() instead of going through this.
private fun AvatarConfigProto.toDomain(): AvatarConfigData =
    AvatarConfigData(
        heightCm = heightCm,
        chestBustCm = chestBustCm.takeIf { it != 0f },
        waistCm = waistCm.takeIf { it != 0f },
        hipCm = hipCm.takeIf { it != 0f },
        animationEnabled = animationEnabled,
    )

private fun AvatarConfigData.toProto(): AvatarConfigProto =
    AvatarConfigProto.newBuilder()
        .setHeightCm(heightCm)
        .setChestBustCm(chestBustCm ?: 0f)
        .setWaistCm(waistCm ?: 0f)
        .setHipCm(hipCm ?: 0f)
        .setAnimationEnabled(animationEnabled)
        .build()
