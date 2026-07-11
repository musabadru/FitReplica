package com.fitreplica.feature.closet.addedit

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fitreplica.core.designsystem.component.WardrobeFilterChip
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.Image
import com.fitreplica.core.model.SizeCategory
import com.fitreplica.core.model.SizeSystem
import com.fitreplica.core.model.Status
import com.fitreplica.feature.closet.photo.rememberPhotoLaunchers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditItemViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val photoLaunchers =
        rememberPhotoLaunchers(onPhotoReady = { viewModel.onAction(AddEditItemUiAction.OnPhotoAdded(it)) })

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }
    LaunchedEffect(uiState.saveWarning) {
        uiState.saveWarning?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit item" else "Add item") },
                navigationIcon = {
                    IconButton(
                        onClick = onCancel,
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel") }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            return@Scaffold
        }

        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PhotoSection(
                existingImages = uiState.existingImages,
                stagedPhotos = uiState.stagedPhotos,
                onAddCamera = photoLaunchers.capture,
                onAddGallery = photoLaunchers.pick,
                onSetPrimary = { viewModel.onAction(AddEditItemUiAction.OnSetPrimaryPhoto(it)) },
                onRemove = { viewModel.onAction(AddEditItemUiAction.OnPhotoRemoved(it)) },
            )

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onAction(AddEditItemUiAction.OnNameChanged(it)) },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.brand,
                onValueChange = { viewModel.onAction(AddEditItemUiAction.OnBrandChanged(it)) },
                label = { Text("Brand") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.colorPrimary,
                onValueChange = { viewModel.onAction(AddEditItemUiAction.OnColorPrimaryChanged(it)) },
                label = { Text("Color") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.colorSecondary,
                onValueChange = { viewModel.onAction(AddEditItemUiAction.OnColorSecondaryChanged(it)) },
                label = { Text("Secondary color (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            EnumChipRow("Type", ClothingType.entries, uiState.type) {
                viewModel.onAction(AddEditItemUiAction.OnTypeChanged(it))
            }
            EnumChipRow("Condition", Condition.entries, uiState.condition) {
                viewModel.onAction(AddEditItemUiAction.OnConditionChanged(it))
            }
            EnumChipRow("Status", Status.entries, uiState.status) {
                viewModel.onAction(AddEditItemUiAction.OnStatusChanged(it))
            }

            Text("Size", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(
                value = uiState.sizeLabel,
                onValueChange = { viewModel.onAction(AddEditItemUiAction.OnSizeLabelChanged(it)) },
                label = { Text("Size label (e.g. \"M\", \"32\")") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            // SizeSystem.UNKNOWN is a normal, selectable option here — not an error state —
            // since a garment's size system often can't be confidently identified.
            EnumChipRow("Size system", SizeSystem.entries, uiState.sizeSystem) {
                viewModel.onAction(AddEditItemUiAction.OnSizeSystemChanged(it))
            }
            EnumChipRow("Size category", SizeCategory.entries, uiState.sizeCategory) {
                viewModel.onAction(AddEditItemUiAction.OnSizeCategoryChanged(it))
            }

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onAction(AddEditItemUiAction.OnNotesChanged(it)) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = { viewModel.onAction(AddEditItemUiAction.OnSaveClicked) },
                enabled = uiState.name.isNotBlank() && uiState.colorPrimary.isNotBlank() && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isSaving) "Saving..." else "Save")
            }
        }
    }
}

private data class PhotoTile(val identifier: String, val previewUri: String, val isPrimary: Boolean)

@Composable
private fun PhotoSection(
    existingImages: List<Image>,
    stagedPhotos: List<StagedPhoto>,
    onAddCamera: () -> Unit,
    onAddGallery: () -> Unit,
    onSetPrimary: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    val tiles =
        existingImages.map { PhotoTile(it.id, it.thumbnailUri, it.isPrimary) } +
            stagedPhotos.map { PhotoTile(it.sourceUri, it.sourceUri, it.isPrimary) }

    Column {
        Text("Photos", style = MaterialTheme.typography.labelMedium)
        LazyRow(
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(tiles, key = { it.identifier }) { tile ->
                Box {
                    AsyncImage(
                        model = tile.previewUri,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(72.dp)
                                .clip(MaterialTheme.shapes.small),
                    )
                    IconButton(onClick = { onRemove(tile.identifier) }, modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(Icons.Default.Close, contentDescription = "Remove photo")
                    }
                    if (!tile.isPrimary) {
                        TextButton(
                            onClick = { onSetPrimary(tile.identifier) },
                            modifier = Modifier.align(Alignment.BottomCenter),
                        ) { Text("Primary") }
                    }
                }
            }
            item {
                Row {
                    IconButton(
                        onClick = onAddCamera,
                    ) { Icon(Icons.Default.AddAPhoto, contentDescription = "Take photo") }
                    IconButton(
                        onClick = onAddGallery,
                    ) { Icon(Icons.Default.AddAPhoto, contentDescription = "Choose photo") }
                }
            }
        }
    }
}

@Composable
private fun <T : Enum<T>> EnumChipRow(
    label: String,
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            options.forEach { option ->
                WardrobeFilterChip(label = option.name, selected = selected == option, onClick = { onSelected(option) })
            }
        }
    }
}
