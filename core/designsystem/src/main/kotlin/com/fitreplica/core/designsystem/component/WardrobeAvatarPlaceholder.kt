package com.fitreplica.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val SILHOUETTE_ALPHA = 0.18f
private const val HEAD_RADIUS_FRACTION = 0.18f
private const val HEAD_CENTER_Y_FRACTION = 0.24f
private const val BODY_LEFT_FRACTION = 0.24f
private const val BODY_TOP_FRACTION = 0.42f
private const val BODY_WIDTH_FRACTION = 0.52f
private const val BODY_HEIGHT_FRACTION = 0.44f
private const val OUTLINE_RADIUS_FRACTION = 0.48f

@Composable
fun WardrobeAvatarPlaceholder(
    modifier: Modifier = Modifier,
    message: String = "Add measurements to preview fit.",
) {
    val silhouetteColor = MaterialTheme.colorScheme.primary.copy(alpha = SILHOUETTE_ALPHA)
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier.widthIn(max = 320.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Canvas(
            modifier =
                Modifier
                    .size(128.dp)
                    .semantics { contentDescription = "Avatar preview placeholder" },
        ) {
            drawCircle(
                color = silhouetteColor,
                radius = size.minDimension * HEAD_RADIUS_FRACTION,
                center = Offset(size.width / 2f, size.height * HEAD_CENTER_Y_FRACTION),
            )
            drawOval(
                color = silhouetteColor,
                topLeft = Offset(size.width * BODY_LEFT_FRACTION, size.height * BODY_TOP_FRACTION),
                size = Size(width = size.width * BODY_WIDTH_FRACTION, height = size.height * BODY_HEIGHT_FRACTION),
            )
            drawCircle(
                color = outlineColor,
                radius = size.minDimension * OUTLINE_RADIUS_FRACTION,
                center = Offset(size.width / 2f, size.height / 2f),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
        Text(
            text = "Avatar preview",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
