package com.fitreplica.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitreplica.core.designsystem.component.WardrobeCard
import com.fitreplica.core.designsystem.component.WardrobeEmptyState
import com.fitreplica.core.designsystem.component.WardrobeFilterChip
import com.fitreplica.core.designsystem.component.WardrobeStatusBadge
import com.fitreplica.core.model.WearHistoryEntry
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DayHeaderFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
private val MonthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
private val TimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
private const val CALENDAR_COLUMN_COUNT = 7

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Wear history") }) },
    ) { padding ->
        HistoryContent(
            uiState = uiState,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(padding).fillMaxSize(),
        )
    }
}

@Composable
private fun HistoryContent(
    uiState: HistoryUiState,
    onAction: (HistoryUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HistoryModePicker(
            selectedMode = uiState.mode,
            onModeSelected = { onAction(HistoryUiAction.OnModeChanged(it)) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        when {
            uiState.isLoading ->
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            uiState.errorMessage != null ->
                HistoryErrorState(
                    message = uiState.errorMessage,
                    onRetry = { onAction(HistoryUiAction.OnRetryClicked) },
                )
            uiState.entries.isEmpty() ->
                WardrobeEmptyState(
                    message = "No wear history yet — tap “Wear now” on an item to start building your timeline.",
                    modifier = Modifier.fillMaxSize(),
                )
            uiState.mode == HistoryMode.TIMELINE ->
                TimelineContent(groups = uiState.timelineGroups)
            else ->
                CalendarContent(uiState = uiState, onAction = onAction)
        }
    }
}

@Composable
private fun HistoryErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        WardrobeEmptyState(message = message, modifier = Modifier.fillMaxWidth())
        TextButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun HistoryModePicker(
    selectedMode: HistoryMode,
    onModeSelected: (HistoryMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WardrobeFilterChip(
            label = "Timeline",
            selected = selectedMode == HistoryMode.TIMELINE,
            onClick = { onModeSelected(HistoryMode.TIMELINE) },
        )
        WardrobeFilterChip(
            label = "Calendar",
            selected = selectedMode == HistoryMode.CALENDAR,
            onClick = { onModeSelected(HistoryMode.CALENDAR) },
        )
    }
}

@Composable
private fun TimelineContent(groups: List<HistoryDayGroup>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        groups.forEach { group ->
            item(key = "header-${group.date}") {
                Text(
                    text = group.date.format(DayHeaderFormatter),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(group.entries, key = { it.id.value }) { entry ->
                WearHistoryCard(entry = entry)
            }
        }
    }
}

@Composable
private fun CalendarContent(
    uiState: HistoryUiState,
    onAction: (HistoryUiAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { onAction(HistoryUiAction.OnPreviousMonthClicked) }) {
                Text("Previous")
            }
            Text(
                text = uiState.visibleMonth.atDay(1).format(MonthFormatter),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = { onAction(HistoryUiAction.OnNextMonthClicked) }) {
                Text("Next")
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(CALENDAR_COLUMN_COUNT),
            modifier = Modifier.height(280.dp).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(uiState.calendarLeadingBlankCount, key = { "blank-$it" }) {
                Box(modifier = Modifier.fillMaxWidth())
            }
            items(uiState.calendarDays, key = { it.date.toString() }) { day ->
                CalendarDayCell(
                    day = day,
                    onClick = { onAction(HistoryUiAction.OnDateSelected(day.date)) },
                )
            }
        }

        Text(
            text = uiState.selectedDate.format(DayHeaderFormatter),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        if (uiState.selectedDayEntries.isEmpty()) {
            WardrobeEmptyState(message = "Nothing logged on this day.", modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.selectedDayEntries, key = { it.id.value }) { entry ->
                    WearHistoryCard(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: HistoryCalendarDay,
    onClick: () -> Unit,
) {
    WardrobeCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelLarge,
                color =
                    if (day.isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
            Text(
                text = if (day.wearCount == 0) "—" else day.wearCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WearHistoryCard(entry: WearHistoryEntry) {
    WardrobeCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(entry.itemName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = entry.wornAt.formatTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                WardrobeStatusBadge(label = entry.itemType.name)
            }
            Text(
                text = "Color: ${entry.colorPrimary}",
                style = MaterialTheme.typography.bodyMedium,
            )
            entry.context?.let {
                Text(text = "Context: $it", style = MaterialTheme.typography.bodyMedium)
            }
            entry.notes?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun Long.formatTime(): String = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(TimeFormatter)
