package com.fitreplica.feature.laundry

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitreplica.metadata.api.CareTagScanResult

@Composable
fun CareTagConfirmationScreen(
    result: CareTagScanResult,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Care tag", style = MaterialTheme.typography.headlineSmall)
        Text(result.rawText, style = MaterialTheme.typography.bodyMedium)
        if (result.suggestedWashTemperatureCelsius != null) {
            Text("Wash ${result.suggestedWashTemperatureCelsius}C", style = MaterialTheme.typography.bodyLarge)
        }
        result.careRequirements.forEach { requirement ->
            Text(requirement.name, style = MaterialTheme.typography.bodyMedium)
        }
        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
            Text("Confirm")
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Dismiss")
        }
    }
}
