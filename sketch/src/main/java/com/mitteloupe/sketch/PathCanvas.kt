package com.mitteloupe.sketch

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke.Companion.DefaultCap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
internal fun PathCanvas(
    modifier: Modifier = Modifier,
    randomSeed: Int = 0,
    pathGenerator: Path.(
        Random,
        Offset,
        Offset,
        maximumLateralOffset: Float,
        stepSize: Float
    ) -> Path,
    relativePointA: RelativePoint,
    relativePointB: RelativePoint,
    brush: Brush,
    strokeWidth: Dp = 0.dp,
    strokeCap: StrokeCap = DefaultCap,
    drawStyle: DrawStyle? = null,
    forceRedrawToggle: Boolean = false
) {
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val drawStyle = drawStyle ?: with(LocalDensity.current) {
        Stroke(width = strokeWidth.toPx(), cap = strokeCap)
    }
    val maximumJitter = with(LocalDensity.current) {
        MAXIMUM_LATERAL_OFFSET.toPx()
    }
    val stepSizePixel = with(LocalDensity.current) {
        STEP_SIZE.toPx()
    }

    val path = remember(canvasSize, randomSeed, relativePointA, relativePointB, forceRedrawToggle) {
        val pointA =
            Offset(relativePointA.x * canvasSize.width, relativePointA.y * canvasSize.height)
        val pointB =
            Offset(relativePointB.x * canvasSize.width, relativePointB.y * canvasSize.height)
        Path().pathGenerator(Random(randomSeed), pointA, pointB, maximumJitter, stepSizePixel)
    }

    Box(
        modifier = modifier
            .onSizeChanged {
                canvasSize = Size(it.width.toFloat(), it.height.toFloat())
            }
            .drawBehind {
                drawPath(
                    path = path,
                    brush = brush,
                    style = drawStyle
                )
            }
    )
}
