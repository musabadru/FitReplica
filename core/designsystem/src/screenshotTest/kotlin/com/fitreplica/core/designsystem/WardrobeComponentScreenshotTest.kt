package com.fitreplica.core.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.fitreplica.core.designsystem.component.WardrobeAvatarPlaceholder
import com.fitreplica.core.designsystem.component.WardrobeCard
import com.fitreplica.core.designsystem.component.WardrobeEmptyState
import com.fitreplica.core.designsystem.component.WardrobeFilterChip
import com.fitreplica.core.designsystem.component.WardrobeStatusBadge
import com.fitreplica.core.designsystem.theme.FitReplicaTheme

@PreviewTest
@Preview(showBackground = true, widthDp = 360)
@Composable
fun WardrobeCardScreenshot() {
    DesignSystemScreenshotSurface {
        WardrobeCard {
            Column {
                Text("Blue jacket")
                Text("Clean and ready")
            }
        }
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360)
@Composable
fun WardrobeFilterChipScreenshot() {
    DesignSystemScreenshotSurface {
        WardrobeFilterChip(label = "Outerwear", selected = true, onClick = {})
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360)
@Composable
fun WardrobeStatusBadgeScreenshot() {
    DesignSystemScreenshotSurface {
        WardrobeStatusBadge(label = "CLEAN")
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360)
@Composable
fun WardrobeEmptyStateScreenshot() {
    DesignSystemScreenshotSurface {
        WardrobeEmptyState(message = "No items match these filters.")
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360)
@Composable
fun WardrobeAvatarPlaceholderScreenshot() {
    DesignSystemScreenshotSurface {
        WardrobeAvatarPlaceholder(message = "Add measurements to preview fit.")
    }
}

@Composable
private fun DesignSystemScreenshotSurface(content: @Composable () -> Unit) {
    FitReplicaTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(24.dp)) {
            content()
        }
    }
}
