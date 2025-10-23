package com.mitteloupe.sketch

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastCoerceIn
import kotlin.math.abs
import kotlin.math.min

private val TrackThickness = 4.0.dp
private val LinearIndicatorWidth = 240.dp
private val LinearIndicatorHeight = TrackThickness
private const val LINEAR_ANIMATION_DURATION = 1800
private const val FIRST_LINE_HEAD_DELAY = 0
private const val FIRST_LINE_TAIL_DELAY = 333
private const val SECOND_LINE_HEAD_DELAY = 1000
private const val SECOND_LINE_TAIL_DELAY = 1267
private val FirstLineHeadEasing = CubicBezierEasing(0.2f, 0f, 0.8f, 1f)
private val FirstLineTailEasing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
private val SecondLineHeadEasing = CubicBezierEasing(0f, 0f, 0.65f, 1f)
private val SecondLineTailEasing = CubicBezierEasing(0.1f, 0f, 0.45f, 1f)
private const val FIRST_LINE_HEAD_DURATION = 750
private const val FIRST_LINE_TAIL_DURATION = 850
private const val SECOND_LINE_HEAD_DURATION = 567
private const val SECOND_LINE_TAIL_DURATION = 533
private const val BASE_ROTATION_ANGLE = 286f
private val CircularEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
private const val ROTATION_DURATION = 1332
private const val HEAD_AND_TAIL_ANIMATION_DURATION = (ROTATION_DURATION * 0.5).toInt()
private const val HEAD_AND_TAIL_DELAY_DURATION = HEAD_AND_TAIL_ANIMATION_DURATION
private const val JUMP_ROTATION_ANGLE = 290f
private const val ROTATIONS_PER_CYCLE = 5
private val ProgressIndicatorSize = 48.0.dp
private val CircularIndicatorDiameter = ProgressIndicatorSize - TrackThickness * 2
private const val ROTATION_ANGLE_OFFSET = (BASE_ROTATION_ANGLE + JUMP_ROTATION_ANGLE) % 360f
private const val START_ANGLE_OFFSET = -90f
private val SemanticsBoundsPadding: Dp = 10.dp
private val IncreaseSemanticsBounds: Modifier =
    Modifier
        .layout { measurable, constraints ->
            val paddingPx = SemanticsBoundsPadding.roundToPx()
            val newConstraint = constraints.offset(0, paddingPx * 2)
            val placeable = measurable.measure(newConstraint)
            val height = placeable.height - paddingPx * 2
            val width = placeable.width
            layout(width, height) { placeable.place(0, -paddingPx) }
        }
        .semantics(mergeDescendants = true) {}
        .padding(vertical = SemanticsBoundsPadding)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.linearColor,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
    gapSize: Dp = ProgressIndicatorDefaults.LinearIndicatorTrackGapSize,
    randomSeed: Int = 0
) {
    val infiniteTransition = rememberInfiniteTransition()
    val firstLineHead =
        infiniteTransition.animateFloat(
            0f,
            1f,
            infiniteRepeatable(
                animation = keyframes {
                    durationMillis = LINEAR_ANIMATION_DURATION
                    0f at FIRST_LINE_HEAD_DELAY using FirstLineHeadEasing
                    1f at FIRST_LINE_HEAD_DURATION + FIRST_LINE_HEAD_DELAY
                }
            )
        )
    val firstLineTail =
        infiniteTransition.animateFloat(
            0f,
            1f,
            infiniteRepeatable(
                animation = keyframes {
                    durationMillis = LINEAR_ANIMATION_DURATION
                    0f at FIRST_LINE_TAIL_DELAY using FirstLineTailEasing
                    1f at FIRST_LINE_TAIL_DURATION + FIRST_LINE_TAIL_DELAY
                }
            )
        )
    val secondLineHead =
        infiniteTransition.animateFloat(
            0f,
            1f,
            infiniteRepeatable(
                animation = keyframes {
                    durationMillis = LINEAR_ANIMATION_DURATION
                    0f at SECOND_LINE_HEAD_DELAY using SecondLineHeadEasing
                    1f at SECOND_LINE_HEAD_DURATION + SECOND_LINE_HEAD_DELAY
                }
            )
        )
    val secondLineTail =
        infiniteTransition.animateFloat(
            0f,
            1f,
            infiniteRepeatable(
                animation = keyframes {
                    durationMillis = LINEAR_ANIMATION_DURATION
                    0f at SECOND_LINE_TAIL_DELAY using SecondLineTailEasing
                    1f at SECOND_LINE_TAIL_DURATION + SECOND_LINE_TAIL_DELAY
                }
            )
        )

    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier
            .then(IncreaseSemanticsBounds)
            .progressSemantics()
            .size(LinearIndicatorWidth, LinearIndicatorHeight)
            .onSizeChanged { newSize -> size = newSize }
    ) {
        val strokeWidth = size.height.toFloat()
        val adjustedGapSize =
            if (strokeCap == StrokeCap.Butt || size.height > size.width) {
                gapSize
            } else {
                with(LocalDensity.current) {
                    gapSize + strokeWidth.toDp()
                }
            }
        val gapSizeFraction = with(LocalDensity.current) {
            adjustedGapSize / size.width.toDp()
        }

        if (firstLineHead.value < 1f - gapSizeFraction) {
            val start = if (firstLineHead.value > 0) firstLineHead.value + gapSizeFraction else 0f
            DrawLinearIndicator(
                start,
                1f,
                trackColor,
                strokeWidth,
                strokeCap,
                randomSeed
            )
        }

        if (firstLineHead.value - firstLineTail.value > 0) {
            DrawLinearIndicator(
                firstLineHead.value,
                firstLineTail.value,
                color,
                strokeWidth,
                strokeCap,
                randomSeed
            )
        }

        if (firstLineTail.value > gapSizeFraction) {
            val start = if (secondLineHead.value > 0) secondLineHead.value + gapSizeFraction else 0f
            val end = if (firstLineTail.value < 1f) firstLineTail.value - gapSizeFraction else 1f
            DrawLinearIndicator(
                start,
                end,
                trackColor,
                strokeWidth,
                strokeCap,
                randomSeed
            )
        }

        if (secondLineHead.value - secondLineTail.value > 0) {
            DrawLinearIndicator(
                secondLineHead.value,
                secondLineTail.value,
                color,
                strokeWidth,
                strokeCap,
                randomSeed
            )
        }

        if (secondLineTail.value > gapSizeFraction) {
            val end = if (secondLineTail.value < 1) secondLineTail.value - gapSizeFraction else 1f
            DrawLinearIndicator(
                0f,
                end,
                trackColor,
                strokeWidth,
                strokeCap,
                randomSeed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.linearColor,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
    gapSize: Dp = ProgressIndicatorDefaults.LinearIndicatorTrackGapSize,
    drawStopIndicator: DrawScope.() -> Unit = {
        drawStopIndicator(
            drawScope = this,
            stopSize = ProgressIndicatorDefaults.LinearTrackStopIndicatorSize,
            color = color,
            strokeCap = strokeCap
        )
    },
    randomSeed: Int = 0
) {
    val coercedProgress = { progress().coerceIn(0f, 1f) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier
            .then(IncreaseSemanticsBounds)
            .semantics(mergeDescendants = true) {
                progressBarRangeInfo = ProgressBarRangeInfo(coercedProgress(), 0f..1f)
            }
            .size(LinearIndicatorWidth, LinearIndicatorHeight)
            .onSizeChanged { newSize -> size = newSize }
    ) {
        val strokeWidth = size.height.toFloat()
        val adjustedGapSize =
            if (strokeCap == StrokeCap.Butt || size.height > size.width) {
                gapSize
            } else {
                with(LocalDensity.current) {
                    gapSize + strokeWidth.toDp()
                }
            }
        val gapSizeFraction = with(LocalDensity.current) {
            adjustedGapSize / size.width.toDp()
        }
        val currentCoercedProgress = coercedProgress()

        val trackStartFraction =
            currentCoercedProgress + min(currentCoercedProgress, gapSizeFraction)
        if (trackStartFraction <= 1f) {
            DrawLinearIndicator(
                trackStartFraction,
                1f,
                trackColor,
                strokeWidth,
                strokeCap,
                randomSeed
            )
        }
        // indicator
        DrawLinearIndicator(0f, currentCoercedProgress, color, strokeWidth, strokeCap, randomSeed)
        // stop
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawStopIndicator(this)
        }
    }
}

@Composable
private fun DrawLinearIndicator(
    startFraction: Float,
    endFraction: Float,
    color: Color,
    strokeWidth: Float,
    strokeCap: StrokeCap,
    randomSeed: Int
) {
    val strokeWidth = with(LocalDensity.current) {
        strokeWidth.toDp()
    }
    PathCanvas(
        modifier = Modifier.fillMaxSize(),
        randomSeed = randomSeed,
        pathGenerator = { random, offset1, offset2, maximumLateralOffset, stepSize ->
            sketchedLine(
                random = random,
                pointA = offset1,
                pointB = offset2,
                maximumLateralOffsetPixel = maximumLateralOffset,
                stepSizePixel = stepSize
            )
        },
        RelativePoint(startFraction, .5f),
        RelativePoint(endFraction, .5f),
        SolidColor(color),
        strokeWidth = strokeWidth,
        strokeCap = strokeCap
    )
}

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.circularColor,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    trackColor: Color = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap,
    randomSeed: Int = 0
) {
    val transition = rememberInfiniteTransition()
    val currentRotation =
        transition.animateValue(
            0,
            ROTATIONS_PER_CYCLE,
            Int.Companion.VectorConverter,
            infiniteRepeatable(
                animation = tween(
                    durationMillis = ROTATION_DURATION * ROTATIONS_PER_CYCLE,
                    easing = LinearEasing
                )
            )
        )
    val baseRotation =
        transition.animateFloat(
            0f,
            BASE_ROTATION_ANGLE,
            infiniteRepeatable(
                animation = tween(durationMillis = ROTATION_DURATION, easing = LinearEasing)
            )
        )
    val endAngle =
        transition.animateFloat(
            0f,
            JUMP_ROTATION_ANGLE,
            infiniteRepeatable(
                animation = keyframes {
                    durationMillis = HEAD_AND_TAIL_ANIMATION_DURATION + HEAD_AND_TAIL_DELAY_DURATION
                    0f at 0 using CircularEasing
                    JUMP_ROTATION_ANGLE at HEAD_AND_TAIL_ANIMATION_DURATION
                }
            )
        )
    val startAngle =
        transition.animateFloat(
            0f,
            JUMP_ROTATION_ANGLE,
            infiniteRepeatable(
                animation = keyframes {
                    durationMillis = HEAD_AND_TAIL_ANIMATION_DURATION + HEAD_AND_TAIL_DELAY_DURATION
                    0f at HEAD_AND_TAIL_DELAY_DURATION using CircularEasing
                    JUMP_ROTATION_ANGLE at durationMillis
                }
            )
        )

    Box(
        modifier = modifier
            .progressSemantics()
            .size(CircularIndicatorDiameter)
    ) {
        PathCanvas(
            modifier = modifier.fillMaxSize(),
            randomSeed = randomSeed,
            pathGenerator = { random, offset1, offset2, maximumLateralOffset, stepSize ->
                sketchedCircle(random, offset1, offset2, maximumLateralOffset)
            },
            relativePointA = RelativePoint(0f, 0f),
            relativePointB = RelativePoint(1f, 1f),
            brush = SolidColor(trackColor),
            strokeWidth = strokeWidth
        )
        val diameterOffset = with(LocalDensity.current) {
            strokeWidth.toPx() / 2
        }

        var redrawToggle by remember { mutableStateOf(false) }
        LaunchedEffect(
            currentRotation.value,
            baseRotation.value,
            startAngle.value,
            endAngle.value
        ) { redrawToggle = !redrawToggle }
        PathCanvas(
            modifier = modifier.fillMaxSize(),
            randomSeed = randomSeed,
            pathGenerator = { random, offset1, offset2, maximumLateralOffset, stepSize ->
                val currentRotationAngleOffset =
                    (currentRotation.value * ROTATION_ANGLE_OFFSET) % 360f
                val sweep = abs(endAngle.value - startAngle.value)
                val offset = START_ANGLE_OFFSET + currentRotationAngleOffset + baseRotation.value
                sketchedCircle(
                    random = random,
                    pointA = Offset(offset1.x + diameterOffset, offset1.y + diameterOffset),
                    pointB = Offset(offset2.x - diameterOffset, offset2.y - diameterOffset),
                    startAngle = startAngle.value + offset,
                    sweep = sweep,
                    maximumLateralOffsetPixel = maximumLateralOffset
                )
            },
            RelativePoint(0f, 0f),
            RelativePoint(1f, 1f),
            SolidColor(color),
            strokeWidth = strokeWidth,
            strokeCap = strokeCap,
            forceRedrawToggle = redrawToggle
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircularProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.circularColor,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    trackColor: Color = ProgressIndicatorDefaults.circularDeterminateTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
    gapSize: Dp = ProgressIndicatorDefaults.CircularIndicatorTrackGapSize,
    randomSeed: Int = 0
) {
    val coercedProgress = { progress().fastCoerceIn(0f, 1f) }
    val startAngle = 270f
    val sweep = coercedProgress() * 360f
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .progressSemantics()
            .semantics(mergeDescendants = true) {
                progressBarRangeInfo = ProgressBarRangeInfo(coercedProgress(), 0f..1f)
            }
            .size(CircularIndicatorDiameter)
            .onSizeChanged { newSize -> size = newSize }
    ) {
        val adjustedGapSize =
            if (strokeCap == StrokeCap.Butt || size.height > size.width) {
                gapSize
            } else {
                gapSize + strokeWidth
            }
        val gapSizeSweep =
            (adjustedGapSize.value / (Math.PI * size.width).toFloat()) * 360f

        PathCanvas(
            modifier = modifier.fillMaxSize(),
            randomSeed = randomSeed,
            pathGenerator = { random, offset1, offset2, maximumLateralOffset, stepSize ->
                sketchedCircle(
                    random = random,
                    pointA = offset1,
                    pointB = offset2,
                    maximumLateralOffsetPixel = maximumLateralOffset,
                    startAngle = startAngle + sweep + min(sweep, gapSizeSweep),
                    sweep = 360f - sweep - min(sweep, gapSizeSweep) * 2
                )
            },
            relativePointA = RelativePoint(0f, 0f),
            relativePointB = RelativePoint(1f, 1f),
            brush = SolidColor(trackColor),
            strokeWidth = strokeWidth
        )

        val diameterOffset = with(LocalDensity.current) {
            strokeWidth.toPx() / 2
        }
        var redrawToggle by remember { mutableStateOf(false) }
        LaunchedEffect(sweep) { redrawToggle = !redrawToggle }
        PathCanvas(
            modifier = modifier.fillMaxSize(),
            randomSeed = randomSeed,
            pathGenerator = { random, offset1, offset2, maximumLateralOffset, stepSize ->
                sketchedCircle(
                    random = random,
                    pointA = Offset(offset1.x + diameterOffset, offset1.y + diameterOffset),
                    pointB = Offset(offset2.x - diameterOffset, offset2.y - diameterOffset),
                    startAngle = startAngle,
                    sweep = sweep,
                    maximumLateralOffsetPixel = maximumLateralOffset
                )
            },
            relativePointA = RelativePoint(0f, 0f),
            relativePointB = RelativePoint(1f, 1f),
            brush = SolidColor(color),
            strokeWidth = strokeWidth,
            strokeCap = strokeCap,
            forceRedrawToggle = redrawToggle
        )
    }
}

fun drawStopIndicator(drawScope: DrawScope, stopSize: Dp, color: Color, strokeCap: StrokeCap) {
    with(drawScope) {
        val adjustedStopSize = min(stopSize.toPx(), size.height) // Stop can't be bigger than track
        val stopOffset = (size.height - adjustedStopSize) / 2 // Offset from end
        if (strokeCap == StrokeCap.Round) {
            drawCircle(
                color = color,
                radius = adjustedStopSize / 2f,
                center = Offset(
                    x = size.width - (adjustedStopSize / 2f) - stopOffset,
                    y = size.height / 2f
                )
            )
        } else {
            drawRect(
                color = color,
                topLeft = Offset(
                    x = size.width - adjustedStopSize - stopOffset,
                    y = (size.height - adjustedStopSize) / 2f
                ),
                size = Size(width = adjustedStopSize, height = adjustedStopSize)
            )
        }
    }
}
