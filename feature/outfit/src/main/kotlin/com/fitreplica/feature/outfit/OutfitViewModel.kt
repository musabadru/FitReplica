package com.fitreplica.feature.outfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitreplica.avatar.api.AvatarAnimationState
import com.fitreplica.avatar.api.AvatarConfig
import com.fitreplica.avatar.api.AvatarState
import com.fitreplica.core.domain.repository.AvatarConfigData
import com.fitreplica.core.domain.repository.UserPreferencesData
import com.fitreplica.core.domain.repository.UserPreferencesRepository
import com.fitreplica.core.model.BodyMeasurements
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class OutfitViewModel
    @Inject
    constructor(
        userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        val avatarState: StateFlow<AvatarState> =
            userPreferencesRepository.userPreferences
                .map { it.toAvatarState() }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(AVATAR_STATE_STOP_TIMEOUT_MS),
                    initialValue = AvatarState(),
                )
    }

internal fun UserPreferencesData.toAvatarState(): AvatarState =
    AvatarState(
        config =
            AvatarConfig.fromMeasurements(
                measurements = avatarConfig.toBodyMeasurements(),
                animationEnabled = animationsEnabled && avatarConfig.animationEnabled,
            ),
        outfit = emptyList(),
        animationState = AvatarAnimationState.IDLE,
    )

private fun AvatarConfigData.toBodyMeasurements(): BodyMeasurements =
    BodyMeasurements(
        heightCm = heightCm,
        chestBustCm = chestBustCm,
        waistCm = waistCm,
        hipCm = hipCm,
    )

private const val AVATAR_STATE_STOP_TIMEOUT_MS = 5_000L
