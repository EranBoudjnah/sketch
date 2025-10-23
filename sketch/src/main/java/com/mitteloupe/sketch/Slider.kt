package com.mitteloupe.sketch

import androidx.annotation.IntRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderDefaults.TickSize
import androidx.compose.material3.SliderDefaults.TrackStopIndicatorSize
import androidx.compose.material3.SliderState
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastFirst
import kotlin.random.Random

private val TrackHeight = 16.0.dp
private val ThumbWidth = 4.0.dp
private val ThumbHeight = 44.0.dp
private val ThumbTrackGapSize: Dp = 6.0.dp
private val TrackInsideCornerSize: Dp = 2.dp
private val ThumbSize = DpSize(ThumbWidth, ThumbHeight)
private const val DISABLED_HANDLE_OPACITY = 0.38f
private const val DISABLED_ACTIVE_TRACK_OPACITY = 0.38f
private const val DISABLED_INACTIVE_TRACK_OPACITY = 0.12f

@ExperimentalMaterial3Api
@Composable
fun Slider(
    state: SketchSliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    randomSeed: Int = 0,
    thumb: @Composable (SketchSliderState) -> Unit = {
        Thumb(
            interactionSource = interactionSource,
            colors = colors,
            enabled = enabled,
            randomSeed = randomSeed
        )
    },
    track: @Composable (SketchSliderState) -> Unit = { sliderState ->
        Track(
            sliderState = sliderState,
            colors = colors,
            enabled = enabled,
            randomSeed = randomSeed
        )
    }
) {
    val thumbWrapper: @Composable (SliderState) -> Unit = { thumb(state) }
    val trackWrapper: @Composable (SliderState) -> Unit = { track(state) }
    Layout(
        content = {
            Box(
                modifier = Modifier
                    .layoutId(SliderComponents.THUMB)
                    .wrapContentWidth()
                    .onSizeChanged {
                        state.thumbWidth = it.width.toFloat()
                    }
            ) {
                thumb(state)
            }
            Box(modifier = Modifier.layoutId(SliderComponents.TRACK)) { track(state) }
        },
        modifier = modifier
            .size(0.dp)
            .requiredSizeIn(minWidth = ThumbWidth, minHeight = TrackHeight)
            .alpha(0f)
    ) { measurables, constraints ->
        val thumbPlaceable =
            measurables.fastFirst { it.layoutId == SliderComponents.THUMB }.measure(constraints)

        val trackPlaceable =
            measurables
                .fastFirst { it.layoutId == SliderComponents.TRACK }
                .measure(constraints.offset(horizontal = -thumbPlaceable.width).copy(minHeight = 0))

        state.trackHeight = trackPlaceable.height.toFloat()

        layout(width = 0, height = 0) {}
    }
    androidx.compose.material3.Slider(
        state = state.sliderState,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        thumb = thumbWrapper,
        track = trackWrapper
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    randomSeed: Int = 0
) {
    val startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }

    val startThumbWrapper: @Composable (SketchRangeSliderState) -> Unit = {
        Thumb(
            interactionSource = startInteractionSource,
            colors = colors,
            enabled = enabled,
            randomSeed = randomSeed
        )
    }
    val endThumbWrapper: @Composable (SketchRangeSliderState) -> Unit = {
        Thumb(
            interactionSource = startInteractionSource,
            colors = colors,
            enabled = enabled,
            randomSeed = randomSeed
        )
    }
    val trackWrapper: @Composable (SketchRangeSliderState) -> Unit = { rangeSliderState ->
        Track(
            colors = colors,
            enabled = enabled,
            rangeSliderState = rangeSliderState,
            randomSeed = randomSeed
        )
    }

    RangeSlider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource,
        randomSeed = randomSeed,
        startThumb = startThumbWrapper,
        endThumb = endThumbWrapper,
        track = trackWrapper
    )
}

@Composable
@ExperimentalMaterial3Api
fun RangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    randomSeed: Int = 0,
    startThumb: @Composable (SketchRangeSliderState) -> Unit = {
        Thumb(
            interactionSource = startInteractionSource,
            colors = colors,
            enabled = enabled,
            randomSeed = randomSeed
        )
    },
    endThumb: @Composable (SketchRangeSliderState) -> Unit = {
        Thumb(
            interactionSource = endInteractionSource,
            colors = colors,
            enabled = enabled,
            randomSeed = randomSeed
        )
    },
    track: @Composable (SketchRangeSliderState) -> Unit = { rangeSliderState ->
        Track(
            colors = colors,
            enabled = enabled,
            rangeSliderState = rangeSliderState,
            randomSeed = randomSeed
        )
    },
    @IntRange(from = 0) steps: Int = 0
) {
    val state = remember(steps, valueRange) {
        SketchRangeSliderState(
            activeRangeStart = value.start,
            activeRangeEndInclusive = value.endInclusive,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange
        )
    }

    LaunchedEffect(state.rangeSliderState.activeRangeStart, state.rangeSliderState.activeRangeEnd) {
        onValueChange(
            state.rangeSliderState.activeRangeStart..state.rangeSliderState.activeRangeEnd
        )
    }

    RangeSlider(
        modifier = modifier,
        state = state,
        enabled = enabled,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource,
        randomSeed = randomSeed,
        startThumb = startThumb,
        endThumb = endThumb,
        track = track
    )
}

@Composable
@ExperimentalMaterial3Api
fun RangeSlider(
    state: SketchRangeSliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(),
    startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    randomSeed: Int = 0,
    startThumb: @Composable (SketchRangeSliderState) -> Unit = {
        Thumb(
            interactionSource = startInteractionSource,
            colors = colors,
            enabled = enabled,
            randomSeed = randomSeed
        )
    },
    endThumb: @Composable (SketchRangeSliderState) -> Unit = {
        Thumb(
            interactionSource = endInteractionSource,
            colors = colors,
            enabled = enabled,
            randomSeed = randomSeed
        )
    },
    track: @Composable (SketchRangeSliderState) -> Unit = { rangeSliderState ->
        Track(
            rangeSliderState = rangeSliderState,
            colors = colors,
            enabled = enabled,
            randomSeed = randomSeed
        )
    }
) {
    val startThumbWrapper: @Composable (RangeSliderState) -> Unit = { startThumb(state) }
    val endThumbWrapper: @Composable (RangeSliderState) -> Unit = { endThumb(state) }
    val trackWrapper: @Composable (RangeSliderState) -> Unit = { track(state) }
    Layout(
        {
            Box(
                modifier = Modifier
                    .layoutId(RangeSliderComponents.START_THUMB)
                    .wrapContentWidth()
                    .onSizeChanged { state.startThumbWidth = it.width.toFloat() }
            ) {
                startThumb(state)
            }
            Box(
                modifier = Modifier
                    .layoutId(RangeSliderComponents.END_THUMB)
                    .wrapContentWidth()
                    .onSizeChanged { state.endThumbWidth = it.width.toFloat() }
            ) {
                endThumb(state)
            }
            Box(modifier = Modifier.layoutId(RangeSliderComponents.TRACK)) { track(state) }
        },
        modifier = modifier
            .minimumInteractiveComponentSize()
            .requiredSizeIn(minWidth = ThumbWidth, minHeight = TrackHeight)
            .alpha(0f)
    ) { measurables, constraints ->
        val startThumbPlaceable =
            measurables
                .fastFirst { it.layoutId == RangeSliderComponents.START_THUMB }
                .measure(constraints)

        val endThumbPlaceable =
            measurables
                .fastFirst { it.layoutId == RangeSliderComponents.END_THUMB }
                .measure(constraints)

        val trackPlaceable =
            measurables
                .fastFirst { it.layoutId == RangeSliderComponents.TRACK }
                .measure(
                    constraints
                        .offset(
                            horizontal = -(startThumbPlaceable.width + endThumbPlaceable.width) / 2
                        )
                        .copy(minHeight = 0)
                )

        state.trackHeight = trackPlaceable.height.toFloat()

        layout(0, 0) {}
    }

    androidx.compose.material3.RangeSlider(
        state = state.rangeSliderState,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource,
        startThumb = startThumbWrapper,
        endThumb = endThumbWrapper,
        track = trackWrapper
    )
}

@Composable
fun Thumb(
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    colors: SliderColors = colors(),
    enabled: Boolean = true,
    thumbSize: DpSize = ThumbSize,
    randomSeed: Int = 0,
    shape: Shape = SketchRectangleShape(randomSeed)
) {
    val interactions = remember { mutableStateListOf<Interaction>() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> interactions.add(interaction)
                is PressInteraction.Release -> interactions.remove(interaction.press)
                is PressInteraction.Cancel -> interactions.remove(interaction.press)
                is DragInteraction.Start -> interactions.add(interaction)
                is DragInteraction.Stop -> interactions.remove(interaction.start)
                is DragInteraction.Cancel -> interactions.remove(interaction.start)
            }
        }
    }

    val size =
        if (interactions.isNotEmpty()) {
            thumbSize.copy(width = thumbSize.width / 2)
        } else {
            thumbSize
        }
    Spacer(
        modifier
            .size(size)
            .hoverable(interactionSource = interactionSource)
            .background(colors.thumbColor(enabled), shape)
    )
}

@ExperimentalMaterial3Api
@Composable
fun Track(
    sliderState: SketchSliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(),
    drawStopIndicator: (DrawScope.(Offset) -> Unit)? = {
        drawStopIndicator(
            drawScope = this,
            offset = it,
            color = colors.trackColor(enabled, active = true),
            size = TrackStopIndicatorSize
        )
    },
    drawTick: DrawScope.(Offset, Color) -> Unit = { offset, color ->
        drawStopIndicator(
            drawScope = this,
            offset = offset,
            color = color,
            size = TickSize
        )
    },
    thumbTrackGapSize: Dp = ThumbTrackGapSize,
    trackInsideCornerSize: Dp = TrackInsideCornerSize,
    randomSeed: Int = 0
) {
    val inactiveTrackColor = colors.trackColor(enabled, active = false)
    val activeTrackColor = colors.trackColor(enabled, active = true)
    val inactiveTickColor = colors.tickColor(enabled, active = false)
    val activeTickColor = colors.tickColor(enabled, active = true)
    val isRightToLeft = LocalLayoutDirection.current == LayoutDirection.Rtl

    val inactivePath1: Path by remember { mutableStateOf(Path()) }
    val inactivePath2: Path by remember { mutableStateOf(Path()) }
    val activePath: Path by remember { mutableStateOf(Path()) }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(TrackHeight)
            .rotate(if (isRightToLeft) 180f else 0f)
    ) {
        drawTrack(
            tickFractions = sliderState.tickFractions,
            activeRangeStart = 0f,
            activeRangeEnd = sliderState.coercedValueAsFraction,
            inactiveTrackColor = inactiveTrackColor,
            activeTrackColor = activeTrackColor,
            inactiveTickColor = inactiveTickColor,
            activeTickColor = activeTickColor,
            height = sliderState.trackHeight.toDp(),
            isRightToLeft = isRightToLeft,
            startThumbWidth = 0.toDp(),
            endThumbWidth = sliderState.thumbWidth.toDp(),
            thumbTrackGapSize = thumbTrackGapSize,
            trackInsideCornerSize = trackInsideCornerSize,
            drawStopIndicator = drawStopIndicator,
            drawTick = drawTick,
            isRangeSlider = false,
            inactivePath1 = inactivePath1,
            inactivePath2 = inactivePath2,
            activePath = activePath,
            randomSeed = randomSeed
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Track(
    rangeSliderState: SketchRangeSliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(),
    drawStopIndicator: (DrawScope.(Offset) -> Unit)? = {
        drawStopIndicator(
            drawScope = this,
            offset = it,
            color = colors.trackColor(enabled, active = true),
            size = TrackStopIndicatorSize
        )
    },
    drawTick: DrawScope.(Offset, Color) -> Unit = { offset, color ->
        drawStopIndicator(
            drawScope = this,
            offset = offset,
            color = color,
            size = TickSize
        )
    },
    thumbTrackGapSize: Dp = ThumbTrackGapSize,
    trackInsideCornerSize: Dp = TrackInsideCornerSize,
    randomSeed: Int = 0
) {
    val inactiveTrackColor = colors.trackColor(enabled, active = false)
    val activeTrackColor = colors.trackColor(enabled, active = true)
    val inactiveTickColor = colors.tickColor(enabled, active = false)
    val activeTickColor = colors.tickColor(enabled, active = true)
    val isRightToLeft = LocalLayoutDirection.current == LayoutDirection.Rtl

    val inactivePath1: Path by remember { mutableStateOf(Path()) }
    val inactivePath2: Path by remember { mutableStateOf(Path()) }
    val activePath: Path by remember { mutableStateOf(Path()) }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(TrackHeight)
            .rotate(if (LocalLayoutDirection.current == LayoutDirection.Rtl) 180f else 0f)
    ) {
        drawTrack(
            tickFractions = rangeSliderState.tickFractions,
            activeRangeStart = rangeSliderState.coercedActiveRangeStartAsFraction,
            activeRangeEnd = rangeSliderState.coercedActiveRangeEndAsFraction,
            inactiveTrackColor = inactiveTrackColor,
            activeTrackColor = activeTrackColor,
            inactiveTickColor = inactiveTickColor,
            activeTickColor = activeTickColor,
            height = rangeSliderState.trackHeight.toDp(),
            isRightToLeft = isRightToLeft,
            startThumbWidth = rangeSliderState.startThumbWidth.toDp(),
            endThumbWidth = rangeSliderState.endThumbWidth.toDp(),
            thumbTrackGapSize = thumbTrackGapSize,
            trackInsideCornerSize = trackInsideCornerSize,
            drawStopIndicator = drawStopIndicator,
            drawTick = drawTick,
            isRangeSlider = true,
            inactivePath1 = inactivePath1,
            inactivePath2 = inactivePath2,
            activePath = activePath,
            randomSeed = randomSeed
        )
    }
}

@Composable
fun colors() = MaterialTheme.colorScheme.defaultSliderColors

private var defaultSliderColorsCached: SliderColors? = null

@Stable
private fun SliderColors.thumbColor(enabled: Boolean): Color = if (enabled) {
    thumbColor
} else {
    disabledThumbColor
}

@ExperimentalMaterial3Api
private val SketchSliderState.tickFractions
    get() = stepsToTickFractions(sliderState.steps)

@ExperimentalMaterial3Api
private val SketchRangeSliderState.tickFractions
    get() = stepsToTickFractions(rangeSliderState.steps)

@ExperimentalMaterial3Api
private val SketchRangeSliderState.coercedActiveRangeStartAsFraction
    get() = with(rangeSliderState) {
        calcFraction(valueRange.start, valueRange.endInclusive, activeRangeStart)
    }

@ExperimentalMaterial3Api
private val SketchRangeSliderState.coercedActiveRangeEndAsFraction
    get() = with(rangeSliderState) {
        calcFraction(valueRange.start, valueRange.endInclusive, activeRangeEnd)
    }

private fun stepsToTickFractions(steps: Int): FloatArray = if (steps == 0) {
    floatArrayOf()
} else {
    FloatArray(steps + 2) { it.toFloat() / (steps + 1) }
}

@ExperimentalMaterial3Api
private val SketchSliderState.coercedValueAsFraction
    get() = calcFraction(
        sliderState.valueRange.start,
        sliderState.valueRange.endInclusive,
        sliderState.value.coerceIn(
            sliderState.valueRange.start,
            sliderState.valueRange.endInclusive
        )
    )

private fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

@Stable
private fun SliderColors.trackColor(enabled: Boolean, active: Boolean): Color = if (enabled) {
    if (active) {
        activeTrackColor
    } else {
        inactiveTrackColor
    }
} else {
    if (active) {
        disabledActiveTrackColor
    } else {
        disabledInactiveTrackColor
    }
}

@Stable
private fun SliderColors.tickColor(enabled: Boolean, active: Boolean): Color = if (enabled) {
    if (active) {
        activeTickColor
    } else {
        inactiveTickColor
    }
} else {
    if (active) {
        disabledActiveTickColor
    } else {
        disabledInactiveTickColor
    }
}

private fun drawStopIndicator(drawScope: DrawScope, offset: Offset, size: Dp, color: Color) {
    with(drawScope) { drawCircle(color = color, center = offset, radius = size.toPx() / 2f) }
}

private fun DrawScope.drawTrack(
    tickFractions: FloatArray,
    activeRangeStart: Float,
    activeRangeEnd: Float,
    inactiveTrackColor: Color,
    activeTrackColor: Color,
    inactiveTickColor: Color,
    activeTickColor: Color,
    height: Dp,
    isRightToLeft: Boolean,
    startThumbWidth: Dp,
    endThumbWidth: Dp,
    thumbTrackGapSize: Dp,
    trackInsideCornerSize: Dp,
    drawStopIndicator: (DrawScope.(Offset) -> Unit)?,
    drawTick: DrawScope.(offset: Offset, color: Color) -> Unit,
    isRangeSlider: Boolean,
    inactivePath1: Path,
    inactivePath2: Path,
    activePath: Path,
    randomSeed: Int
) {
    val sliderStart = Offset(0f, center.y)
    val sliderEnd = Offset(size.width, center.y)
    val trackStrokeWidth = height.toPx()

    val sliderValueEnd =
        Offset(sliderStart.x + (sliderEnd.x - sliderStart.x) * activeRangeEnd, center.y)

    val sliderValueStart =
        Offset(sliderStart.x + (sliderEnd.x - sliderStart.x) * activeRangeStart, center.y)

    val cornerSize = trackStrokeWidth / 2
    val insideCornerSize = trackInsideCornerSize.toPx()
    var startGap = 0f
    var endGap = 0f
    if (thumbTrackGapSize > 0.dp) {
        startGap = startThumbWidth.toPx() / 2 + thumbTrackGapSize.toPx()
        endGap = endThumbWidth.toPx() / 2 + thumbTrackGapSize.toPx()
    }

    if (isRangeSlider && sliderValueStart.x > sliderStart.x + startGap + cornerSize) {
        val start = sliderStart.x
        val end = sliderValueStart.x - startGap
        drawTrackPath(
            path = inactivePath1,
            offset = Offset.Zero,
            size = Size(end - start, trackStrokeWidth),
            isRightToLeft = isRightToLeft,
            startCornerRadius = cornerSize,
            endCornerRadius = insideCornerSize,
            randomSeed = randomSeed
        )
        drawPath(inactivePath1, inactiveTrackColor)
        drawStopIndicator?.invoke(this, Offset(start + cornerSize, center.y))
    }

    if (sliderValueEnd.x < sliderEnd.x - endGap - cornerSize) {
        val start = sliderValueEnd.x + endGap
        val end = sliderEnd.x
        drawTrackPath(
            path = inactivePath2,
            offset = Offset(start, 0f),
            size = Size(end - start, trackStrokeWidth),
            isRightToLeft = isRightToLeft,
            startCornerRadius = insideCornerSize,
            endCornerRadius = cornerSize,
            randomSeed = randomSeed
        )
        drawPath(inactivePath2, inactiveTrackColor)
        drawStopIndicator?.invoke(this, Offset(end - cornerSize, center.y))
    }

    val activeTrackStart = if (isRangeSlider) sliderValueStart.x + startGap else 0f
    val activeTrackEnd = sliderValueEnd.x - endGap
    val startCornerRadius = if (isRangeSlider) insideCornerSize else cornerSize
    if (activeTrackEnd - activeTrackStart > startCornerRadius) {
        drawTrackPath(
            path = activePath,
            offset = Offset(activeTrackStart, 0f),
            size = Size(activeTrackEnd - activeTrackStart, trackStrokeWidth),
            isRightToLeft = isRightToLeft,
            startCornerRadius = startCornerRadius,
            endCornerRadius = insideCornerSize,
            randomSeed = randomSeed
        )
        drawPath(activePath, activeTrackColor)
    }

    val start = Offset(sliderStart.x + cornerSize, sliderStart.y)
    val end = Offset(sliderEnd.x - cornerSize, sliderEnd.y)
    val tickStartGap = sliderValueStart.x - startGap..sliderValueStart.x + startGap
    val tickEndGap = sliderValueEnd.x - endGap..sliderValueEnd.x + endGap
    tickFractions.forEachIndexed { index, tick ->
        if (drawStopIndicator != null) {
            val isOnStopIndicator = (isRangeSlider && index == 0) || index == tickFractions.size - 1
            if (isOnStopIndicator) {
                return@forEachIndexed
            }
        }

        val outsideFraction = tick > activeRangeEnd || tick < activeRangeStart
        val center = Offset(lerp(start, end, tick).x, center.y)
        val isInGap = (isRangeSlider && center.x in tickStartGap) || center.x in tickEndGap
        if (isInGap) {
            return@forEachIndexed
        }
        drawTick(center, if (outsideFraction) inactiveTickColor else activeTickColor)
    }
}

private fun DrawScope.drawTrackPath(
    path: Path,
    offset: Offset,
    size: Size,
    isRightToLeft: Boolean,
    startCornerRadius: Float,
    endCornerRadius: Float,
    randomSeed: Int
): Path {
    val random = Random(randomSeed)
    val (leftCornerRadius, rightCornerRadius) = if (isRightToLeft) {
        endCornerRadius to startCornerRadius
    } else {
        startCornerRadius to endCornerRadius
    }
    path.rewind()
    return path.sketchedRoundRectangle(
        random = random,
        pointA = Offset(offset.x, 0f),
        pointB = Offset(offset.x + size.width, size.height),
        maximumLateralOffsetPixel = MAXIMUM_LATERAL_OFFSET.toPx(),
        stepSizePixel = STEP_SIZE.toPx(),
        topLeftCornerRadius = leftCornerRadius,
        topRightCornerRadius = rightCornerRadius,
        bottomRightCornerRadius = rightCornerRadius,
        bottomLeftCornerRadius = leftCornerRadius
    )
}

private val ColorScheme.defaultSliderColors: SliderColors
    get() = defaultSliderColorsCached
        ?: SliderColors(
            thumbColor = primary,
            activeTrackColor = primary,
            activeTickColor = secondaryContainer,
            inactiveTrackColor = secondaryContainer,
            inactiveTickColor = primary,
            disabledThumbColor = onSurface
                .copy(alpha = DISABLED_HANDLE_OPACITY)
                .compositeOver(surface),
            disabledActiveTrackColor = onSurface.copy(alpha = DISABLED_ACTIVE_TRACK_OPACITY),
            disabledActiveTickColor = onSurface.copy(alpha = DISABLED_INACTIVE_TRACK_OPACITY),
            disabledInactiveTrackColor = onSurface.copy(alpha = DISABLED_INACTIVE_TRACK_OPACITY),
            disabledInactiveTickColor = onSurface.copy(alpha = DISABLED_ACTIVE_TRACK_OPACITY)
        ).also { defaultSliderColorsCached = it }

private enum class SliderComponents {
    THUMB,
    TRACK
}

private enum class RangeSliderComponents {
    END_THUMB,
    START_THUMB,
    TRACK
}

@ExperimentalMaterial3Api
data class SketchSliderState(val sliderState: SliderState) {
    constructor(
        value: Float = 0f,
        @IntRange(from = 0) steps: Int = 0,
        onValueChangeFinished: (() -> Unit)? = null,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f
    ) : this(
        SliderState(
            value = value,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange
        )
    )

    var thumbWidth: Float by mutableFloatStateOf(0f)
    var trackHeight: Float by mutableFloatStateOf(0f)
}

@ExperimentalMaterial3Api
data class SketchRangeSliderState(val rangeSliderState: RangeSliderState) {
    constructor(
        activeRangeStart: Float = 0f,
        activeRangeEndInclusive: Float = 1f,
        @IntRange(from = 0) steps: Int = 0,
        onValueChangeFinished: (() -> Unit)? = null,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f
    ) : this(
        RangeSliderState(
            activeRangeStart = activeRangeStart,
            activeRangeEnd = activeRangeEndInclusive,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange
        )
    )

    var startThumbWidth: Float by mutableFloatStateOf(0f)
    var endThumbWidth: Float by mutableFloatStateOf(0f)
    var trackHeight: Float by mutableFloatStateOf(0f)
}
