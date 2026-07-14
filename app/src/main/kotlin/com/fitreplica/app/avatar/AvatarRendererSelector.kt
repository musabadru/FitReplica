package com.fitreplica.app.avatar

import com.fitreplica.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AvatarRendererChoice { Disabled, TwoD }

@Singleton
class AvatarRendererSelector
    @Inject
    constructor(
        userPreferencesRepository: UserPreferencesRepository,
    ) {
        val choice: Flow<AvatarRendererChoice> =
            userPreferencesRepository.userPreferences
                .map { preferences ->
                    if (preferences.avatarModuleEnabled) {
                        AvatarRendererChoice.TwoD
                    } else {
                        AvatarRendererChoice.Disabled
                    }
                }
                .distinctUntilChanged()
    }
