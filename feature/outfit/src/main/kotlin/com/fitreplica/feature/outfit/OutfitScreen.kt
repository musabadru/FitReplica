package com.fitreplica.feature.outfit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitreplica.avatar.api.AvatarRenderer

@Composable
fun OutfitScreen(
    avatarRenderer: AvatarRenderer,
    modifier: Modifier = Modifier,
    viewModel: OutfitViewModel = hiltViewModel(),
) {
    val avatarState by viewModel.avatarState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        avatarRenderer.Render(
            config = avatarState.config,
            outfit = avatarState.outfit,
            animationState = avatarState.animationState,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
