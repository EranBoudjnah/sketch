package com.mitteloupe.sketch

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private val StateLayerSize = 40.0.dp
private val IconSize = 20.0.dp
private val RadioButtonPadding = 2.dp
private val RadioButtonDotSize = 12.dp
private val RadioStrokeWidth = 2.dp

private const val RADIO_ANIMATION_DURATION = 100

@Composable
fun RadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: RadioButtonColors = RadioButtonDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
    seed: Int = 0
) {
    val random = Random(seed)

    val dotRadius =
        animateDpAsState(
            targetValue = if (selected) RadioButtonDotSize / 2 else 0.dp,
            animationSpec = tween(durationMillis = RADIO_ANIMATION_DURATION)
        )
    val radioColor = colors.radioColor(enabled, selected)
    val selectableModifier =
        if (onClick != null) {
            Modifier.selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = false,
                    radius = StateLayerSize / 2
                )
            )
        } else {
            Modifier
        }
    val localDensity = LocalDensity.current
    val maximumJitter = with(localDensity) {
        MAXIMUM_LATERAL_OFFSET.toPx()
    }
    val outerCircleSize = with(localDensity) {
        Size(IconSize.toPx(), IconSize.toPx())
    }
    val dotCircleSize by remember(dotRadius.value) {
        with(localDensity) {
            mutableStateOf(Size(dotRadius.value.toPx(), dotRadius.value.toPx()))
        }
    }

    val iconPath = remember(outerCircleSize, seed) {
        val strokeWidth = with(localDensity) { RadioStrokeWidth.toPx() }
        val halfStrokeWidth = strokeWidth / 2f
        val pointA = Offset(halfStrokeWidth, halfStrokeWidth)
        val pointB = Offset(
            outerCircleSize.width - halfStrokeWidth,
            outerCircleSize.height - halfStrokeWidth
        )
        Path().sketchedCircle(random, pointA, pointB, maximumJitter)
    }

    val dotPath by remember(outerCircleSize, dotCircleSize, seed) {
        val strokeWidth = with(localDensity) { RadioStrokeWidth.toPx() }
        val dotSize = dotCircleSize.width * 2f - strokeWidth
        val offsetX = (outerCircleSize.width - dotSize) / 2f
        val offsetY = (outerCircleSize.height - dotSize) / 2f
        val pointA = Offset(offsetX, offsetY)
        val pointB = Offset(offsetX + dotSize, offsetY + dotSize)
        mutableStateOf(
            Path().sketchedCircle(random, pointA, pointB, maximumJitter)
        )
    }

    Canvas(
        modifier
            .then(
                if (onClick != null) {
                    Modifier.minimumInteractiveComponentSize()
                } else {
                    Modifier
                }
            )
            .then(selectableModifier)
            .wrapContentSize(Alignment.Center)
            .padding(RadioButtonPadding)
            .requiredSize(IconSize)
    ) {
        val strokeWidth = RadioStrokeWidth.toPx()
        drawPath(path = iconPath, color = radioColor.value, style = Stroke(strokeWidth))
        if (dotRadius.value > 0.dp) {
            drawPath(path = dotPath, color = radioColor.value, style = Fill)
        }
    }
}

@Composable
private fun RadioButtonColors.radioColor(enabled: Boolean, selected: Boolean): State<Color> {
    val target =
        when {
            enabled && selected -> selectedColor
            enabled && !selected -> unselectedColor
            !enabled && selected -> disabledSelectedColor
            else -> disabledUnselectedColor
        }

    // If not enabled 'snap' to the disabled state, as there should be no animations between
    // enabled / disabled.
    return if (enabled) {
        animateColorAsState(target, tween(durationMillis = RADIO_ANIMATION_DURATION))
    } else {
        rememberUpdatedState(target)
    }
}
