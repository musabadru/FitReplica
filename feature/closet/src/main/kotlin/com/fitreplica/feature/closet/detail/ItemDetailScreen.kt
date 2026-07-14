package com.fitreplica.feature.closet.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fitreplica.core.designsystem.component.WardrobeStatusBadge
import com.fitreplica.core.designsystem.theme.WardrobeExpressiveCtaShape
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.Image
import com.fitreplica.feature.closet.photo.rememberPhotoLaunchers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    onEditClick: () -> Unit,
    onBackClick: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRetireConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val photoLaunchers =
        rememberPhotoLaunchers(onPhotoReady = { viewModel.onAction(ItemDetailUiAction.OnPhotoAdded(it)) })

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDeleted()
    }

    LaunchedEffect(uiState.photoWarning) {
        uiState.photoWarning?.let { warning -> snackbarHostState.showSnackbar(warning) }
    }

    LaunchedEffect(uiState.deleteError) {
        uiState.deleteError?.let { error -> snackbarHostState.showSnackbar(error) }
    }

    LaunchedEffect(uiState.conditionError) {
        uiState.conditionError?.let { error -> snackbarHostState.showSnackbar(error) }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.item?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
            )
        },
    ) { padding ->
        val item = uiState.item
        if (item == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
            ) {
                PhotoCarousel(
                    images = uiState.images,
                    onSetPrimary = { viewModel.onAction(ItemDetailUiAction.OnSetPrimaryImage(it)) },
                    onDelete = { viewModel.onAction(ItemDetailUiAction.OnDeleteImage(it)) },
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    TextButton(onClick = photoLaunchers.capture) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null)
                        Text("Camera")
                    }
                    TextButton(onClick = photoLaunchers.pick) { Text("Gallery") }
                }
                ItemDetails(item)
                ConditionActions(
                    currentCondition = item.condition,
                    onConditionSelected = { condition ->
                        if (condition == Condition.RETIRED) {
                            showRetireConfirm = true
                        } else {
                            viewModel.onAction(ItemDetailUiAction.OnConditionSelected(condition))
                        }
                    },
                )
                Button(
                    onClick = { viewModel.onAction(ItemDetailUiAction.OnWearNowClicked) },
                    enabled = item.condition != Condition.RETIRED,
                    shape = WardrobeExpressiveCtaShape,
                    colors = ButtonDefaults.buttonColors(),
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                ) {
                    Text("Wear now")
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete item?") },
            text = { Text("This removes the item and its photos. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.onAction(ItemDetailUiAction.OnDeleteConfirmed)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }

    if (showRetireConfirm) {
        AlertDialog(
            onDismissRequest = { showRetireConfirm = false },
            title = { Text("Retire item?") },
            text = { Text("Retired items stay in history but are removed from active outfit and laundry flows.") },
            confirmButton = {
                TextButton(onClick = {
                    showRetireConfirm = false
                    viewModel.onAction(ItemDetailUiAction.OnConditionSelected(Condition.RETIRED))
                }) { Text("Retire") }
            },
            dismissButton = {
                TextButton(onClick = { showRetireConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun PhotoCarousel(
    images: List<Image>,
    onSetPrimary: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    if (images.isEmpty()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        return
    }

    val pagerState = rememberPagerState(pageCount = { images.size })
    Column {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(240.dp)) { page ->
            AsyncImage(
                model = images[page].uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        LazyRow(
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(images, key = { it.id }) { image ->
                Column {
                    val thumbnailBackground =
                        if (image.isPrimary) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    AsyncImage(
                        model = image.thumbnailUri,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(64.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(thumbnailBackground),
                    )
                    Row {
                        TextButton(
                            onClick = { onSetPrimary(image.id) },
                        ) { Text(if (image.isPrimary) "Primary" else "Set primary") }
                        IconButton(onClick = { onDelete(image.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete photo")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemDetails(item: ClothingItem) {
    Column(modifier = Modifier.padding(16.dp)) {
        item.brand?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            WardrobeStatusBadge(label = item.status.name)
            WardrobeStatusBadge(label = item.condition.name)
        }
        Text("Color: ${item.colorPrimary}", style = MaterialTheme.typography.bodyMedium)
        item.size?.let { size ->
            Text(
                "Size: ${size.label} (${size.system.name}, ${size.category.name})",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text("Worn ${item.timesWorn} times", style = MaterialTheme.typography.bodyMedium)
        item.notes?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ConditionActions(
    currentCondition: Condition,
    onConditionSelected: (Condition) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Condition", style = MaterialTheme.typography.titleMedium)
        LazyRow(
            contentPadding = PaddingValues(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(Condition.entries) { condition ->
                val selected = condition == currentCondition
                TextButton(
                    onClick = { onConditionSelected(condition) },
                    enabled = !selected,
                ) {
                    Text(if (selected) "${condition.name} selected" else condition.name)
                }
            }
        }
    }
}
