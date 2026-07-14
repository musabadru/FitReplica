package com.fitreplica.feature.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitreplica.core.model.ClosetAnalytics
import com.fitreplica.core.model.ContextBreakdown
import com.fitreplica.core.model.OutfitSuggestion
import com.fitreplica.core.model.RepairTime
import com.fitreplica.core.model.WearStreak
import java.util.concurrent.TimeUnit

private const val TOP_ANALYTICS_ITEMS = 5

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp),
            )
        }
        uiState.errorMessage?.let { message ->
            item {
                ErrorSection(
                    message = message,
                    onRetryClick = viewModel::onRetryClicked,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        } ?: run {
            uiState.analytics?.let { analytics ->
                item { ClosetAnalyticsSection(analytics) }
            }
            item { HorizontalDivider() }
            item { WearStreakSection(uiState.wearStreaks) }
            item { RepairSection(uiState.repairTimes) }
            item { ContextSection(uiState.contextBreakdown) }
            item { SuggestionSection(uiState.suggestions) }
        }
    }
}

@Composable
private fun ClosetAnalyticsSection(analytics: ClosetAnalytics) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Closet", style = MaterialTheme.typography.titleLarge)
        MetricRow("Never worn", analytics.neverWorn.size.toString())
        MetricRow("Over-rotated", analytics.overRotated.size.toString())
        MetricRow("Colors", analytics.colorDistribution.entries.joinToString { "${it.key}: ${it.value}" })
        MetricRow("Types", analytics.typeDistribution.entries.joinToString { "${it.key}: ${it.value}" })
        if (analytics.costPerWear.isNotEmpty()) {
            Text("Cost per wear", style = MaterialTheme.typography.titleMedium)
            analytics.costPerWear.take(TOP_ANALYTICS_ITEMS).forEach {
                MetricRow(it.itemName, "%.2f".format(it.costPerWear))
            }
        }
    }
}

@Composable
private fun WearStreakSection(streaks: List<WearStreak>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Wear", style = MaterialTheme.typography.titleLarge)
        if (streaks.isEmpty()) {
            Text("No wear history yet.")
        } else {
            streaks.take(TOP_ANALYTICS_ITEMS).forEach {
                MetricRow(it.itemName, "${it.streakLength} ${it.interval.name.lowercase()}s")
            }
        }
    }
}

@Composable
private fun ErrorSection(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetryClick) {
            Text("Retry")
        }
    }
}

@Composable
private fun RepairSection(repairs: List<RepairTime>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Repair", style = MaterialTheme.typography.titleLarge)
        if (repairs.isEmpty()) {
            Text("No repair cycles yet.")
        } else {
            repairs.take(TOP_ANALYTICS_ITEMS).forEach { repair ->
                MetricRow(repair.itemName.orEmpty(), repair.durationLabel())
            }
        }
    }
}

private fun RepairTime.durationLabel(): String {
    val days = TimeUnit.MILLISECONDS.toDays(durationMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis).coerceAtLeast(1)
    return when {
        days > 0 -> "$days days"
        hours > 0 -> "$hours hours"
        else -> "$minutes minutes"
    }
}

@Composable
private fun ContextSection(contexts: List<ContextBreakdown>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Context", style = MaterialTheme.typography.titleLarge)
        if (contexts.isEmpty()) {
            Text("No contexts yet.")
        } else {
            contexts.forEach { MetricRow(it.context, it.wearCount.toString()) }
        }
    }
}

@Composable
private fun SuggestionSection(suggestions: List<OutfitSuggestion>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Suggestions", style = MaterialTheme.typography.titleLarge)
        if (suggestions.isEmpty()) {
            Text("No suggestions yet.")
        } else {
            suggestions.forEach { suggestion ->
                Column {
                    Text(suggestion.title, style = MaterialTheme.typography.titleMedium)
                    Text(suggestion.reason, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
