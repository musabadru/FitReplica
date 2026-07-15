@file:Suppress("LongParameterList", "MagicNumber")

package com.fitreplica.avatar.impl2d

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.fitreplica.avatar.api.AvatarAnimationState
import com.fitreplica.avatar.api.AvatarConfig
import com.fitreplica.avatar.api.AvatarLayer
import com.fitreplica.avatar.api.AvatarRenderer
import com.fitreplica.core.model.ClothingItem
import javax.inject.Inject

internal class TwoDAvatarRenderer
    @Inject
    constructor() : AvatarRenderer {
        @Composable
        override fun Render(
            config: AvatarConfig,
            outfit: List<ClothingItem>,
            animationState: AvatarAnimationState,
            modifier: Modifier,
        ) {
            val context = LocalContext.current
            val systemAnimatorScale = remember(context) { context.systemAnimatorScale() }
            val motionEnabled = isAvatarMotionEnabled(config.animationEnabled, systemAnimatorScale)
            val silhouette = remember(config) { selectSilhouette(config) }
            val layers = remember(outfit) { resolveAvatarLayers(outfit, AndroidAvatarDebugLogger) }
            val layerColors = remember(layers) { layers.map { it.toDrawableAvatarLayer() } }
            val poseState = rememberAvatarPoseFrame(animationState, motionEnabled)
            val bodyPath = remember { Path() }
            val garmentPath = remember { Path() }

            Canvas(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .aspectRatio(0.56f)
                        .semantics { contentDescription = "2D avatar preview" },
            ) {
                if (size.minDimension <= 0f) return@Canvas

                val pose = poseState.value
                val centerX = size.width / 2f + size.width * pose.hipShiftFraction
                drawBody(
                    centerX = centerX,
                    silhouette = silhouette,
                    skinTone = config.skinTone.toColor(),
                    pose = pose,
                    path = bodyPath,
                )
                var layerIndex = 0
                while (layerIndex < layerColors.size) {
                    val layer = layerColors[layerIndex]
                    drawGarmentLayer(
                        layer = layer.layer,
                        color = layer.color,
                        centerX = centerX,
                        silhouette = silhouette,
                        pose = pose,
                        path = garmentPath,
                    )
                    layerIndex++
                }
            }
        }
    }

@Composable
private fun rememberAvatarPoseFrame(
    animationState: AvatarAnimationState,
    motionEnabled: Boolean,
): State<AvatarPoseFrame> {
    if (!motionEnabled) {
        return remember {
            object : State<AvatarPoseFrame> {
                override val value: AvatarPoseFrame = AvatarPoseFrame()
            }
        }
    }

    val transition = rememberInfiniteTransition(label = "avatarPose")
    val progress =
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 900, easing = LinearEasing),
                ),
            label = "avatarPoseProgress",
        )
    val latestAnimationState = rememberUpdatedState(animationState)

    return remember(progress) {
        object : State<AvatarPoseFrame> {
            override val value: AvatarPoseFrame
                get() = poseFrameFor(latestAnimationState.value, progress.value)
        }
    }
}

private fun DrawScope.drawBody(
    centerX: Float,
    silhouette: SilhouetteVariant,
    skinTone: Color,
    pose: AvatarPoseFrame,
    path: Path,
) {
    val headRadius = size.width * 0.09f
    drawCircle(
        color = skinTone,
        radius = headRadius,
        center = Offset(centerX, size.height * 0.12f),
    )

    val shoulderWidth = size.width * 0.38f * silhouette.shoulderScale
    val waistWidth = size.width * 0.24f * silhouette.waistScale
    val hipWidth = size.width * 0.31f * silhouette.hipScale
    val torsoTop = size.height * 0.23f
    val torsoBottom = size.height * 0.55f

    rotate(degrees = pose.shoulderTiltDegrees, pivot = Offset(centerX, torsoTop)) {
        path.reset()
        path.moveTo(centerX - shoulderWidth / 2f, torsoTop)
        path.lineTo(centerX + shoulderWidth / 2f, torsoTop)
        path.lineTo(centerX + waistWidth / 2f, size.height * 0.42f)
        path.lineTo(centerX + hipWidth / 2f, torsoBottom)
        path.lineTo(centerX - hipWidth / 2f, torsoBottom)
        path.lineTo(centerX - waistWidth / 2f, size.height * 0.42f)
        path.close()
        drawPath(
            path = path,
            color = skinTone,
        )
    }

    val legTop = size.height * 0.55f
    val legHeight = size.height * 0.33f * silhouette.heightScale
    drawLimb(centerX - size.width * 0.065f, legTop, legHeight, pose.legSwingDegrees, skinTone)
    drawLimb(centerX + size.width * 0.065f, legTop, legHeight, -pose.legSwingDegrees, skinTone)
    drawLimb(centerX - shoulderWidth / 2f, torsoTop, size.height * 0.27f, pose.armSwingDegrees, skinTone)
    drawLimb(centerX + shoulderWidth / 2f, torsoTop, size.height * 0.27f, -pose.armSwingDegrees, skinTone)
}

private fun DrawScope.drawLimb(
    x: Float,
    y: Float,
    height: Float,
    rotationDegrees: Float,
    color: Color,
) {
    rotate(degrees = rotationDegrees, pivot = Offset(x, y)) {
        drawRoundRect(
            color = color,
            topLeft = Offset(x - size.width * 0.025f, y),
            size = Size(width = size.width * 0.05f, height = height),
        )
    }
}

private fun DrawScope.drawGarmentLayer(
    layer: AvatarLayer,
    color: Color,
    centerX: Float,
    silhouette: SilhouetteVariant,
    pose: AvatarPoseFrame,
    path: Path,
) {
    when (layer) {
        AvatarLayer.Body -> Unit
        AvatarLayer.Base ->
            drawTorsoGarment(
                centerX = centerX,
                color = color,
                top = size.height * 0.23f,
                bottom = size.height * 0.5f,
                shoulderScale = silhouette.shoulderScale,
                hipScale = silhouette.waistScale,
                pose = pose,
                path = path,
            )
        AvatarLayer.Mid ->
            drawTorsoGarment(
                centerX = centerX,
                color = color,
                top = size.height * 0.35f,
                bottom = size.height * 0.66f,
                shoulderScale = silhouette.waistScale,
                hipScale = silhouette.hipScale,
                pose = pose,
                path = path,
            )
        AvatarLayer.Outer ->
            drawTorsoGarment(
                centerX = centerX,
                color = color.copy(alpha = 0.9f),
                top = size.height * 0.21f,
                bottom = size.height * 0.6f,
                shoulderScale = silhouette.shoulderScale * 1.08f,
                hipScale = silhouette.hipScale * 1.08f,
                pose = pose,
                path = path,
            )
        AvatarLayer.Shoes -> {
            val shoeY = shoeTopY(size.height, silhouette)
            drawOval(
                color = color,
                topLeft = Offset(centerX - size.width * 0.15f, shoeY),
                size = Size(size.width * 0.13f, size.height * 0.035f),
            )
            drawOval(
                color = color,
                topLeft = Offset(centerX + size.width * 0.02f, shoeY),
                size = Size(size.width * 0.13f, size.height * 0.035f),
            )
        }
    }
}

private fun DrawScope.drawTorsoGarment(
    centerX: Float,
    color: Color,
    top: Float,
    bottom: Float,
    shoulderScale: Float,
    hipScale: Float,
    pose: AvatarPoseFrame,
    path: Path,
) {
    val shoulderWidth = size.width * 0.42f * shoulderScale
    val hipWidth = size.width * 0.32f * hipScale
    rotate(degrees = pose.shoulderTiltDegrees, pivot = Offset(centerX, top)) {
        path.reset()
        path.moveTo(centerX - shoulderWidth / 2f, top)
        path.lineTo(centerX + shoulderWidth / 2f, top)
        path.lineTo(centerX + hipWidth / 2f, bottom)
        path.lineTo(centerX - hipWidth / 2f, bottom)
        path.close()
        drawPath(
            path = path,
            color = color,
        )
    }
}

internal fun shoeTopY(
    avatarHeight: Float,
    silhouette: SilhouetteVariant,
): Float = avatarHeight * 0.55f + avatarHeight * 0.33f * silhouette.heightScale

private data class DrawableAvatarLayer(
    val layer: AvatarLayer,
    val color: Color,
)

private fun ResolvedAvatarLayer.toDrawableAvatarLayer(): DrawableAvatarLayer =
    DrawableAvatarLayer(
        layer = layer,
        color = item.colorPrimary.toAvatarColor(),
    )

private fun android.content.Context.systemAnimatorScale(): Float =
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

private fun com.fitreplica.avatar.api.SkinTone.toColor(): Color =
    when (this) {
        com.fitreplica.avatar.api.SkinTone.LIGHT -> Color(0xFFE6B98F)
        com.fitreplica.avatar.api.SkinTone.MEDIUM_LIGHT -> Color(0xFFD7A06F)
        com.fitreplica.avatar.api.SkinTone.MEDIUM -> Color(0xFFB87945)
        com.fitreplica.avatar.api.SkinTone.MEDIUM_DARK -> Color(0xFF8F5632)
        com.fitreplica.avatar.api.SkinTone.DARK -> Color(0xFF5B3626)
    }

private fun String.toAvatarColor(): Color =
    when (lowercase()) {
        "black" -> Color(0xFF1F1F1F)
        "white" -> Color(0xFFF2F2F2)
        "gray", "grey" -> Color(0xFF808080)
        "navy" -> Color(0xFF1E2A44)
        "blue" -> Color(0xFF2F6FDB)
        "red" -> Color(0xFFC3423F)
        "green" -> Color(0xFF3D7A4B)
        "yellow" -> Color(0xFFE0B83F)
        "brown" -> Color(0xFF7A5637)
        "purple" -> Color(0xFF7651A6)
        else -> Color(0xFF5E6C84)
    }
