package com.fitreplica.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WardrobeCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
