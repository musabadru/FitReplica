package com.fitreplica.feature.outfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitreplica.avatar.api.AvatarAnimationState
import com.fitreplica.avatar.api.AvatarConfig
import com.fitreplica.avatar.api.AvatarState
import com.fitreplica.core.domain.repository.AvatarConfigData
import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.domain.repository.UserPreferencesData
import com.fitreplica.core.domain.repository.UserPreferencesRepository
import com.fitreplica.core.model.BodyMeasurements
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OutfitViewModel
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
        clothingRepository: ClothingRepository,
    ) : ViewModel() {
        private val measurementInputs = MutableStateFlow(MeasurementInputs())
        private val selectedItemIds = MutableStateFlow<List<ClothingId>>(emptyList())
        private val preferences =
            userPreferencesRepository.userPreferences.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(OUTFIT_STATE_STOP_TIMEOUT_MS),
                initialValue = UserPreferencesData(),
            )

        internal val uiState: StateFlow<OutfitUiState> =
            combine(
                preferences,
                clothingRepository.observeItems(),
                measurementInputs,
                selectedItemIds,
            ) { preferences, availableItems, inputs, selectedIds ->
                val selectedOutfit = selectedIds.toSelectedItems(availableItems)
                val measurements = inputs.withFallback(preferences.avatarConfig)

                OutfitUiState(
                    avatarModuleEnabled = preferences.avatarModuleEnabled,
                    globalAnimationsEnabled = preferences.animationsEnabled,
                    avatarAnimationEnabled = preferences.avatarConfig.animationEnabled,
                    measurements = measurements,
                    availableItems = availableItems,
                    selectedItemIds = selectedIds.toSet(),
                    avatarState = preferences.toAvatarState(selectedOutfit),
                )
            }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(OUTFIT_STATE_STOP_TIMEOUT_MS),
                    initialValue = OutfitUiState(),
                )

        internal fun onAction(action: OutfitUiAction) {
            when (action) {
                is OutfitUiAction.OnAvatarModuleEnabledChanged ->
                    viewModelScope.launch {
                        userPreferencesRepository.setAvatarModuleEnabled(action.enabled)
                    }

                is OutfitUiAction.OnAvatarAnimationEnabledChanged ->
                    updateAvatarConfig { it.copy(animationEnabled = action.enabled) }

                is OutfitUiAction.OnMeasurementChanged ->
                    onMeasurementChanged(action.field, action.value)

                is OutfitUiAction.OnItemSelectionToggled ->
                    selectedItemIds.update { current -> current.toggled(action.itemId) }
            }
        }

        private fun onMeasurementChanged(
            field: MeasurementField,
            value: String,
        ) {
            measurementInputs.update { it.updated(field, value) }
            updateAvatarConfig { config ->
                when (field) {
                    MeasurementField.Height ->
                        config.copy(heightCm = value.toFloatOrNull() ?: AvatarConfigData.DEFAULT_HEIGHT_CM)
                    MeasurementField.Chest ->
                        config.copy(chestBustCm = value.toNullableFloat())
                    MeasurementField.Waist ->
                        config.copy(waistCm = value.toNullableFloat())
                    MeasurementField.Hip ->
                        config.copy(hipCm = value.toNullableFloat())
                }
            }
        }

        private fun updateAvatarConfig(transform: (AvatarConfigData) -> AvatarConfigData) {
            viewModelScope.launch {
                userPreferencesRepository.setAvatarConfig(transform(preferences.value.avatarConfig))
            }
        }
    }

internal data class OutfitUiState(
    val avatarModuleEnabled: Boolean = false,
    val globalAnimationsEnabled: Boolean = true,
    val avatarAnimationEnabled: Boolean = true,
    val measurements: MeasurementInputs = MeasurementInputs(),
    val availableItems: List<ClothingItem> = emptyList(),
    val selectedItemIds: Set<ClothingId> = emptySet(),
    val avatarState: AvatarState = AvatarState(),
)

internal data class MeasurementInputs(
    val heightCm: String? = null,
    val chestBustCm: String? = null,
    val waistCm: String? = null,
    val hipCm: String? = null,
) {
    fun textFor(field: MeasurementField): String =
        when (field) {
            MeasurementField.Height -> heightCm
            MeasurementField.Chest -> chestBustCm
            MeasurementField.Waist -> waistCm
            MeasurementField.Hip -> hipCm
        }.orEmpty()
}

internal enum class MeasurementField { Height, Chest, Waist, Hip }

internal sealed interface OutfitUiAction {
    data class OnAvatarModuleEnabledChanged(val enabled: Boolean) : OutfitUiAction

    data class OnAvatarAnimationEnabledChanged(val enabled: Boolean) : OutfitUiAction

    data class OnMeasurementChanged(
        val field: MeasurementField,
        val value: String,
    ) : OutfitUiAction

    data class OnItemSelectionToggled(val itemId: ClothingId) : OutfitUiAction
}

internal fun UserPreferencesData.toAvatarState(outfit: List<ClothingItem> = emptyList()): AvatarState =
    AvatarState(
        config =
            AvatarConfig.fromMeasurements(
                measurements = avatarConfig.toBodyMeasurements(),
                animationEnabled = animationsEnabled && avatarConfig.animationEnabled,
            ),
        outfit = outfit,
        animationState = AvatarAnimationState.IDLE,
    )

private fun MeasurementInputs.withFallback(config: AvatarConfigData): MeasurementInputs =
    MeasurementInputs(
        heightCm = heightCm ?: config.heightCm.toMeasurementText(),
        chestBustCm = chestBustCm ?: config.chestBustCm.toMeasurementText(),
        waistCm = waistCm ?: config.waistCm.toMeasurementText(),
        hipCm = hipCm ?: config.hipCm.toMeasurementText(),
    )

private fun MeasurementInputs.updated(
    field: MeasurementField,
    value: String,
): MeasurementInputs =
    when (field) {
        MeasurementField.Height -> copy(heightCm = value)
        MeasurementField.Chest -> copy(chestBustCm = value)
        MeasurementField.Waist -> copy(waistCm = value)
        MeasurementField.Hip -> copy(hipCm = value)
    }

private fun List<ClothingId>.toggled(itemId: ClothingId): List<ClothingId> =
    if (itemId in this) {
        filterNot { it == itemId }
    } else {
        this + itemId
    }

private fun List<ClothingId>.toSelectedItems(items: List<ClothingItem>): List<ClothingItem> {
    val itemsById = items.associateBy { it.id }
    return mapNotNull { itemsById[it] }
}

private fun AvatarConfigData.toBodyMeasurements(): BodyMeasurements =
    BodyMeasurements(
        heightCm = heightCm,
        chestBustCm = chestBustCm,
        waistCm = waistCm,
        hipCm = hipCm,
    )

private fun String.toNullableFloat(): Float? = takeIf { it.isNotBlank() }?.toFloatOrNull()

private fun Float?.toMeasurementText(): String = this?.toMeasurementText().orEmpty()

private fun Float.toMeasurementText(): String =
    if (this % 1f == 0f) {
        toInt().toString()
    } else {
        toString()
    }

private const val OUTFIT_STATE_STOP_TIMEOUT_MS = 5_000L
