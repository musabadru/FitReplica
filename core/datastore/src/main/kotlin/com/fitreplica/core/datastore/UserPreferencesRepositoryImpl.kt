package com.fitreplica.core.datastore

import androidx.datastore.core.DataStore
import com.fitreplica.core.domain.repository.UserPreferencesData
import com.fitreplica.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesStore: DataStore<UserPreferences>,
) : UserPreferencesRepository {
    override val userPreferences: Flow<UserPreferencesData> =
        userPreferencesStore.data.map { proto ->
            UserPreferencesData(
                avatarModuleEnabled = proto.avatarModuleEnabled,
                metadataModuleEnabled = proto.metadataModuleEnabled,
                animationsEnabled = proto.animationsEnabled,
            )
        }

    override suspend fun setAvatarModuleEnabled(enabled: Boolean) {
        userPreferencesStore.updateData { it.toBuilder().setAvatarModuleEnabled(enabled).build() }
    }

    override suspend fun setMetadataModuleEnabled(enabled: Boolean) {
        userPreferencesStore.updateData { it.toBuilder().setMetadataModuleEnabled(enabled).build() }
    }
}
