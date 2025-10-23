package com.mitteloupe.sketch

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val TrackWidth = 52.0.dp
private val TrackHeight = 32.0.dp
private val TrackOutlineWidth = 2.0.dp
private val StateLayerSize = 40.0.dp
private val UnselectedHandleWidth = 16.0.dp
private val SelectedHandleWidth = 24.0.dp
private val PressedHandleWidth = 28.0.dp

private val SwitchWidth = TrackWidth
private val SwitchHeight = TrackHeight
private val UncheckedThumbDiameter = UnselectedHandleWidth
private val ThumbDiameter = SelectedHandleWidth
private val ThumbPadding = (SwitchHeight - ThumbDiameter) / 2

private val SnapSpec = SnapSpec<Float>()
private val AnimationSpec = TweenSpec<Float>(durationMillis = 100)

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
    randomSeed: Int = 0
) {
    val thumbShape by remember(randomSeed) {
        mutableStateOf(SketchCircleShape(randomSeed))
    }

    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }

    val toggleableModifier =
        if (onCheckedChange != null) {
            Modifier
                .minimumInteractiveComponentSize()
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    enabled = enabled,
                    role = Role.Switch,
                    interactionSource = interactionSource,
                    indication = null
                )
        } else {
            Modifier
        }

    SwitchImpl(
        modifier = modifier
            .then(toggleableModifier)
            .wrapContentSize(Alignment.Center)
            .requiredSize(SwitchWidth, SwitchHeight),
        checked = checked,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        thumbShape = thumbShape,
        thumbContent = thumbContent,
        randomSeed = randomSeed
    )
}

@Composable
@Suppress("ComposableLambdaParameterNaming", "ComposableLambdaParameterPosition")
private fun SwitchImpl(
    modifier: Modifier,
    checked: Boolean,
    enabled: Boolean,
    colors: SwitchColors,
    thumbContent: (@Composable () -> Unit)?,
    interactionSource: InteractionSource,
    thumbShape: Shape,
    randomSeed: Int
) {
    val trackColor = colors.trackColor(enabled, checked)
    val resolvedThumbColor = colors.thumbColor(enabled, checked)
    val trackShape by remember(randomSeed) {
        mutableStateOf(SketchCapsuleShape(randomSeed))
    }

    Box(
        modifier
            .border(TrackOutlineWidth, colors.borderColor(enabled, checked), trackShape)
            .background(trackColor, trackShape)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .then(ThumbElement(interactionSource, checked))
                .indication(
                    interactionSource = interactionSource,
                    indication = ripple(
                        bounded = false,
                        radius = StateLayerSize / 2
                    )
                )
                .background(resolvedThumbColor, thumbShape),
            contentAlignment = Alignment.Center
        ) {
            if (thumbContent != null) {
                val iconColor = colors.iconColor(enabled, checked)
                CompositionLocalProvider(
                    LocalContentColor provides iconColor,
                    content = thumbContent
                )
            }
        }
    }
}

@Stable
private fun SwitchColors.trackColor(enabled: Boolean, checked: Boolean): Color = if (enabled) {
    if (checked) checkedTrackColor else uncheckedTrackColor
} else {
    if (checked) disabledCheckedTrackColor else disabledUncheckedTrackColor
}

@Stable
private fun SwitchColors.borderColor(enabled: Boolean, checked: Boolean): Color = if (enabled) {
    if (checked) checkedBorderColor else uncheckedBorderColor
} else {
    if (checked) disabledCheckedBorderColor else disabledUncheckedBorderColor
}

@Stable
private fun SwitchColors.thumbColor(enabled: Boolean, checked: Boolean): Color = if (enabled) {
    if (checked) checkedThumbColor else uncheckedThumbColor
} else {
    if (checked) disabledCheckedThumbColor else disabledUncheckedThumbColor
}

@Stable
private fun SwitchColors.iconColor(enabled: Boolean, checked: Boolean): Color = if (enabled) {
    if (checked) checkedIconColor else uncheckedIconColor
} else {
    if (checked) disabledCheckedIconColor else disabledUncheckedIconColor
}

private data class ThumbElement(val interactionSource: InteractionSource, val checked: Boolean) :
    ModifierNodeElement<ThumbNode>() {
    override fun create() = ThumbNode(interactionSource, checked)

    override fun update(node: ThumbNode) {
        node.interactionSource = interactionSource
        if (node.checked != checked) {
            node.invalidateMeasurement()
        }
        node.checked = checked
        node.update()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "switchThumb"
        properties["interactionSource"] = interactionSource
        properties["checked"] = checked
    }
}

private class ThumbNode(var interactionSource: InteractionSource, var checked: Boolean) :
    Modifier.Node(),
    LayoutModifierNode {

    override val shouldAutoInvalidate: Boolean
        get() = false

    private var isPressed = false
    private var offsetAnim: Animatable<Float, AnimationVector1D>? = null
    private var sizeAnim: Animatable<Float, AnimationVector1D>? = null
    private var initialOffset: Float = Float.NaN
    private var initialSize: Float = Float.NaN

    override fun onAttach() {
        coroutineScope.launch {
            var pressCount = 0
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> pressCount++
                    is PressInteraction.Release -> pressCount--
                    is PressInteraction.Cancel -> pressCount--
                }
                val pressed = pressCount > 0
                if (isPressed != pressed) {
                    isPressed = pressed
                    invalidateMeasurement()
                }
            }
        }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val hasContent = measurable.maxIntrinsicHeight(constraints.maxWidth) != 0 &&
            measurable.maxIntrinsicWidth(constraints.maxHeight) != 0
        val size =
            when {
                isPressed -> PressedHandleWidth
                hasContent || checked -> ThumbDiameter
                else -> UncheckedThumbDiameter
            }.toPx()

        val actualSize = (sizeAnim?.value ?: size).toInt()
        val placeable = measurable.measure(Constraints.fixed(actualSize, actualSize))
        val thumbPaddingStart = (SwitchHeight - size.toDp()) / 2f
        val minBound = thumbPaddingStart.toPx()
        val thumbPathLength = (SwitchWidth - ThumbDiameter) - ThumbPadding
        val maxBound = thumbPathLength.toPx()
        val offset =
            when {
                isPressed && checked -> maxBound - TrackOutlineWidth.toPx()
                isPressed && !checked -> TrackOutlineWidth.toPx()
                checked -> maxBound
                else -> minBound
            }

        if (sizeAnim?.targetValue != size) {
            coroutineScope.launch {
                sizeAnim?.animateTo(size, if (isPressed) SnapSpec else AnimationSpec)
            }
        }

        if (offsetAnim?.targetValue != offset) {
            coroutineScope.launch {
                offsetAnim?.animateTo(offset, if (isPressed) SnapSpec else AnimationSpec)
            }
        }

        if (initialSize.isNaN() && initialOffset.isNaN()) {
            initialSize = size
            initialOffset = offset
        }

        return layout(actualSize, actualSize) {
            placeable.placeRelative(offsetAnim?.value?.toInt() ?: offset.toInt(), 0)
        }
    }

    fun update() {
        if (sizeAnim == null && !initialSize.isNaN()) {
            sizeAnim = Animatable(initialSize)
        }

        if (offsetAnim == null && !initialOffset.isNaN()) offsetAnim = Animatable(initialOffset)
    }
}
