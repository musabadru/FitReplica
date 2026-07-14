package com.fitreplica.feature.outfit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.fitreplica.core.designsystem.component.WardrobeAvatarPlaceholder

@Composable
fun OutfitScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        WardrobeAvatarPlaceholder(
            modifier = Modifier.align(Alignment.Center),
            message = "Add measurements to preview fit.",
        )
    }
}
