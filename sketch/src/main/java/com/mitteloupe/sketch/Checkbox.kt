package com.mitteloupe.sketch

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.floor
import kotlin.random.Random

private val StateLayerSize = 40.0.dp
private val CheckboxDefaultPadding = 2.dp
private val CheckboxSize = 20.dp
private val StrokeWidth = 2.dp
private const val CHECK_ANIMATION_DURATION = 100
private const val BOX_IN_DURATION = 50
private const val BOX_OUT_DURATION = 100

@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
    seed: Int = 0
) {
    TriStateCheckbox(
        state = ToggleableState(checked),
        onClick = if (onCheckedChange != null) {
            { onCheckedChange(!checked) }
        } else {
            null
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        seed = seed
    )
}

@Composable
fun TriStateCheckbox(
    state: ToggleableState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
    seed: Int = 0
) {
    val toggleableModifier =
        if (onClick != null) {
            Modifier.triStateToggleable(
                state = state,
                onClick = onClick,
                enabled = enabled,
                role = Role.Checkbox,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = false,
                    radius = StateLayerSize / 2
                )
            )
        } else {
            Modifier
        }
    CheckboxImpl(
        enabled = enabled,
        value = state,
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.minimumInteractiveComponentSize()
                } else {
                    Modifier
                }
            )
            .then(toggleableModifier)
            .padding(CheckboxDefaultPadding),
        colors = colors,
        seed = seed
    )
}

@Composable
private fun CheckboxImpl(
    enabled: Boolean,
    value: ToggleableState,
    modifier: Modifier,
    colors: CheckboxColors,
    seed: Int = 0
) {
    val transition = updateTransition(value)
    val checkDrawFraction =
        transition.animateFloat(
            transitionSpec = {
                when {
                    initialState == ToggleableState.Off -> tween(CHECK_ANIMATION_DURATION)
                    targetState == ToggleableState.Off -> snap(BOX_OUT_DURATION)
                    else -> spring()
                }
            }
        ) {
            when (it) {
                ToggleableState.On -> 1f
                ToggleableState.Off -> 0f
                ToggleableState.Indeterminate -> 1f
            }
        }

    val checkCenterGravitationShiftFraction =
        transition.animateFloat(
            transitionSpec = {
                when {
                    initialState == ToggleableState.Off -> snap()
                    targetState == ToggleableState.Off -> snap(BOX_OUT_DURATION)
                    else -> tween(durationMillis = CHECK_ANIMATION_DURATION)
                }
            }
        ) {
            when (it) {
                ToggleableState.On -> 0f
                ToggleableState.Off -> 0f
                ToggleableState.Indeterminate -> 1f
            }
        }
    val checkCache = remember { CheckDrawingCache() }
    val checkColor = colors.checkmarkColor(value)
    val boxColor = colors.boxColor(enabled, value)
    val borderColor = colors.borderColor(enabled, value)
    val maximumJitter = with(LocalDensity.current) {
        MAXIMUM_LATERAL_OFFSET.toPx()
    }
    val stepSizePixel = with(LocalDensity.current) {
        STEP_SIZE.toPx()
    }

    val canvasSize = with(LocalDensity.current) {
        Size(CheckboxSize.toPx(), CheckboxSize.toPx())
    }
    val path = remember(canvasSize, seed) {
        val relativePointA = RelativePoint(0f, 0f)
        val relativePointB = RelativePoint(1f, 1f)
        val pointA =
            Offset(relativePointA.x * canvasSize.width, relativePointA.y * canvasSize.height)
        val pointB =
            Offset(relativePointB.x * canvasSize.width, relativePointB.y * canvasSize.height)
        Path().sketchedRectangle(
            random = Random(seed),
            pointA = pointA,
            pointB = pointB,
            maximumLateralOffsetPixel = maximumJitter,
            stepSizePixel = stepSizePixel
        )
    }

    Canvas(
        modifier
            .wrapContentSize(Alignment.Center)
            .requiredSize(CheckboxSize)
    ) {
        val strokeWidthPx = floor(StrokeWidth.toPx())
        drawBox(
            boxColor = boxColor.value,
            borderColor = borderColor.value,
            strokeWidth = strokeWidthPx,
            path = path
        )
        drawCheck(
            checkColor = checkColor.value,
            checkFraction = checkDrawFraction.value,
            crossCenterGravitation = checkCenterGravitationShiftFraction.value,
            strokeWidthPx = strokeWidthPx,
            drawingCache = checkCache
        )
    }
}

@Composable
private fun CheckboxColors.checkmarkColor(state: ToggleableState): State<Color> {
    val target =
        if (state == ToggleableState.Off) {
            uncheckedCheckmarkColor
        } else {
            checkedCheckmarkColor
        }

    val duration = if (state == ToggleableState.Off) BOX_OUT_DURATION else BOX_IN_DURATION
    return animateColorAsState(target, tween(durationMillis = duration))
}

@Composable
private fun CheckboxColors.boxColor(enabled: Boolean, state: ToggleableState): State<Color> {
    val target =
        if (enabled) {
            when (state) {
                ToggleableState.On,
                ToggleableState.Indeterminate -> checkedBoxColor

                ToggleableState.Off -> uncheckedBoxColor
            }
        } else {
            when (state) {
                ToggleableState.On -> disabledCheckedBoxColor
                ToggleableState.Indeterminate -> disabledIndeterminateBoxColor
                ToggleableState.Off -> disabledUncheckedBoxColor
            }
        }

    return if (enabled) {
        val duration = if (state == ToggleableState.Off) BOX_OUT_DURATION else BOX_IN_DURATION
        animateColorAsState(target, tween(durationMillis = duration))
    } else {
        rememberUpdatedState(target)
    }
}

@Composable
private fun CheckboxColors.borderColor(enabled: Boolean, state: ToggleableState): State<Color> {
    val target =
        if (enabled) {
            when (state) {
                ToggleableState.On,
                ToggleableState.Indeterminate -> checkedBorderColor

                ToggleableState.Off -> uncheckedBorderColor
            }
        } else {
            when (state) {
                ToggleableState.Indeterminate -> disabledIndeterminateBorderColor
                ToggleableState.On -> disabledBorderColor
                ToggleableState.Off -> disabledUncheckedBorderColor
            }
        }

    // If not enabled 'snap' to the disabled state, as there should be no animations between
    // enabled / disabled.
    return if (enabled) {
        val duration = if (state == ToggleableState.Off) BOX_OUT_DURATION else BOX_IN_DURATION
        animateColorAsState(target, tween(durationMillis = duration))
    } else {
        rememberUpdatedState(target)
    }
}

private fun DrawScope.drawBox(boxColor: Color, borderColor: Color, strokeWidth: Float, path: Path) {
    if (boxColor == borderColor) {
        drawPath(path = path, color = boxColor, style = Fill)
    } else {
        drawPath(path = path, color = boxColor, style = Fill)
        drawPath(path = path, color = borderColor, style = Stroke(strokeWidth))
    }
}

private fun DrawScope.drawCheck(
    checkColor: Color,
    checkFraction: Float,
    crossCenterGravitation: Float,
    strokeWidthPx: Float,
    drawingCache: CheckDrawingCache
) {
    val stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Square)
    val width = size.width
    val checkCrossX = 0.4f
    val checkCrossY = 0.7f
    val leftX = 0.2f
    val leftY = 0.5f
    val rightX = 0.8f
    val rightY = 0.3f

    val gravitatedCrossX = lerp(checkCrossX, 0.5f, crossCenterGravitation)
    val gravitatedCrossY = lerp(checkCrossY, 0.5f, crossCenterGravitation)
    // gravitate only Y for end to achieve center line
    val gravitatedLeftY = lerp(leftY, 0.5f, crossCenterGravitation)
    val gravitatedRightY = lerp(rightY, 0.5f, crossCenterGravitation)

    with(drawingCache) {
        checkPath.reset()
        checkPath.moveTo(width * leftX, width * gravitatedLeftY)
        checkPath.lineTo(width * gravitatedCrossX, width * gravitatedCrossY)
        checkPath.lineTo(width * rightX, width * gravitatedRightY)
        // TODO: replace with proper declarative non-android alternative when ready (b/158188351)
        pathMeasure.setPath(checkPath, false)
        pathToDraw.reset()
        pathMeasure.getSegment(0f, pathMeasure.length * checkFraction, pathToDraw, true)
    }
    drawPath(drawingCache.pathToDraw, checkColor, style = stroke)
}

@Immutable
private class CheckDrawingCache(
    val checkPath: Path = Path(),
    val pathMeasure: PathMeasure = PathMeasure(),
    val pathToDraw: Path = Path()
)
