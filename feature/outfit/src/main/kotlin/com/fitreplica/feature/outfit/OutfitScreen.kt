package com.fitreplica.feature.outfit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitreplica.avatar.api.AvatarRenderer
import com.fitreplica.core.designsystem.component.WardrobeCard
import com.fitreplica.core.designsystem.component.WardrobeEmptyState
import com.fitreplica.core.designsystem.component.WardrobeStatusBadge
import com.fitreplica.core.model.ClothingItem
import kotlinx.coroutines.launch

@Composable
fun OutfitScreen(
    avatarRenderer: AvatarRenderer,
    modifier: Modifier = Modifier,
    viewModel: OutfitViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OutfitContent(
        uiState = uiState,
        avatarRenderer = avatarRenderer,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
private fun OutfitContent(
    uiState: OutfitUiState,
    avatarRenderer: AvatarRenderer,
    onAction: (OutfitUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val measurementFocusRequesters =
        remember {
            MeasurementField.entries.associateWith { FocusRequester() }
        }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        item {
            AvatarPreview(
                uiState = uiState,
                avatarRenderer = avatarRenderer,
                onMeasurementsClick = {
                    val field = uiState.measurements.firstMissingField ?: MeasurementField.Height
                    coroutineScope.launch {
                        listState.animateScrollToItem(AVATAR_CONFIGURATION_ITEM_INDEX)
                        measurementFocusRequesters[field]?.requestFocus()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            AvatarConfiguration(
                uiState = uiState,
                onAction = onAction,
                measurementFocusRequesters = measurementFocusRequesters,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            Text("Outfit", style = MaterialTheme.typography.titleMedium)
        }

        if (uiState.availableItems.isEmpty()) {
            item {
                WardrobeEmptyState(
                    message = "Add closet items before building an outfit.",
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                )
            }
        } else {
            items(uiState.availableItems, key = { it.id.value }) { item ->
                OutfitItemRow(
                    item = item,
                    selected = item.id in uiState.selectedItemIds,
                    onSelectionChanged = {
                        onAction(OutfitUiAction.OnItemSelectionToggled(item.id))
                    },
                )
            }
        }
    }
}

@Composable
private fun AvatarPreview(
    uiState: OutfitUiState,
    avatarRenderer: AvatarRenderer,
    onMeasurementsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WardrobeCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Avatar", style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier.fillMaxWidth().height(320.dp),
                contentAlignment = Alignment.Center,
            ) {
                avatarRenderer.Render(
                    config = uiState.avatarState.config,
                    outfit = uiState.avatarState.outfit,
                    animationState = uiState.avatarState.animationState,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            Text(
                text = "${uiState.avatarState.outfit.size} item(s) selected",
                style = MaterialTheme.typography.bodySmall,
            )
            if (uiState.measurements.firstMissingField != null) {
                Text(
                    text = "Complete body measurements to personalize the avatar preview.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onMeasurementsClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Add body measurements")
                }
            }
        }
    }
}

@Composable
private fun AvatarConfiguration(
    uiState: OutfitUiState,
    onAction: (OutfitUiAction) -> Unit,
    measurementFocusRequesters: Map<MeasurementField, FocusRequester>,
    modifier: Modifier = Modifier,
) {
    WardrobeCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Configuration", style = MaterialTheme.typography.titleMedium)
            SwitchRow(
                label = "2D avatar",
                checked = uiState.avatarModuleEnabled,
                onCheckedChange = { onAction(OutfitUiAction.OnAvatarModuleEnabledChanged(it)) },
            )
            SwitchRow(
                label = "Avatar animation",
                checked = uiState.avatarAnimationEnabled,
                enabled = uiState.globalAnimationsEnabled,
                onCheckedChange = { onAction(OutfitUiAction.OnAvatarAnimationEnabledChanged(it)) },
            )
            HorizontalDivider()
            MeasurementRow(
                label = "Height",
                value = uiState.measurements.textFor(MeasurementField.Height),
                onValueChange = { onAction(OutfitUiAction.OnMeasurementChanged(MeasurementField.Height, it)) },
                modifier = Modifier.focusRequester(measurementFocusRequesters.getValue(MeasurementField.Height)),
            )
            MeasurementRow(
                label = "Chest",
                value = uiState.measurements.textFor(MeasurementField.Chest),
                onValueChange = { onAction(OutfitUiAction.OnMeasurementChanged(MeasurementField.Chest, it)) },
                modifier = Modifier.focusRequester(measurementFocusRequesters.getValue(MeasurementField.Chest)),
            )
            MeasurementRow(
                label = "Waist",
                value = uiState.measurements.textFor(MeasurementField.Waist),
                onValueChange = { onAction(OutfitUiAction.OnMeasurementChanged(MeasurementField.Waist, it)) },
                modifier = Modifier.focusRequester(measurementFocusRequesters.getValue(MeasurementField.Waist)),
            )
            MeasurementRow(
                label = "Hip",
                value = uiState.measurements.textFor(MeasurementField.Hip),
                onValueChange = { onAction(OutfitUiAction.OnMeasurementChanged(MeasurementField.Hip, it)) },
                modifier = Modifier.focusRequester(measurementFocusRequesters.getValue(MeasurementField.Hip)),
            )
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Composable
private fun MeasurementRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filterMeasurementInput()) },
        label = { Text("$label (cm)") },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun OutfitItemRow(
    item: ClothingItem,
    selected: Boolean,
    onSelectionChanged: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WardrobeCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onSelectionChanged() },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall)
                item.brand?.let { brand ->
                    Text(brand, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WardrobeStatusBadge(label = item.type.name)
                    WardrobeStatusBadge(label = item.status.name)
                }
            }
        }
    }
}

internal fun String.filterMeasurementInput(): String {
    val normalized = replace(',', '.')
    return normalized.filterIndexed { index, char ->
        char.isDigit() || char == '.' && normalized.indexOf('.') == index
    }
}

private const val AVATAR_CONFIGURATION_ITEM_INDEX = 1
