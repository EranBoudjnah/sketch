package com.mitteloupe.sketch

import androidx.annotation.IntRange
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.random.Random

data class SketchRoundedCornerShape(
    private val topStart: CornerSize,
    private val topEnd: CornerSize,
    private val bottomEnd: CornerSize,
    private val bottomStart: CornerSize,
    private val randomSeed: Int = 0
) : SketchShape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val pointA = Offset.Zero
            val pointB = Offset(size.width, size.height)
            val (
                topLeftCornerRadius,
                topRightCornerRadius,
                bottomRightCornerRadius,
                bottomLeftCornerRadius
            ) = if (layoutDirection == LayoutDirection.Ltr) {
                listOf(
                    topStart.toPx(size, density),
                    topEnd.toPx(size, density),
                    bottomEnd.toPx(size, density),
                    bottomStart.toPx(size, density)
                )
            } else {
                listOf(
                    topEnd.toPx(size, density),
                    topStart.toPx(size, density),
                    bottomStart.toPx(size, density),
                    bottomEnd.toPx(size, density)
                )
            }
            with(density) {
                sketchedRoundRectangle(
                    random = Random(randomSeed),
                    pointA = pointA,
                    pointB = pointB,
                    maximumLateralOffsetPixel = MAXIMUM_LATERAL_OFFSET.toPx(),
                    stepSizePixel = STEP_SIZE.toPx(),
                    topLeftCornerRadius = topLeftCornerRadius,
                    topRightCornerRadius = topRightCornerRadius,
                    bottomRightCornerRadius = bottomRightCornerRadius,
                    bottomLeftCornerRadius = bottomLeftCornerRadius
                )
            }
        }
        return Outline.Generic(path)
    }

    fun top() = copy(bottomEnd = CornerSize(0.dp), bottomStart = CornerSize(0.dp))

    fun bottom() = copy(topEnd = CornerSize(0.dp), topStart = CornerSize(0.dp))

    override fun simplifiedShape(): Shape = RoundedCornerShape(
        topStart = topStart,
        topEnd = topEnd,
        bottomEnd = bottomEnd,
        bottomStart = bottomStart
    )
}

fun SketchRoundedCornerShape(cornerSize: CornerSize, randomSeed: Int = 0) =
    SketchRoundedCornerShape(
        topStart = cornerSize,
        topEnd = cornerSize,
        bottomEnd = cornerSize,
        bottomStart = cornerSize,
        randomSeed = randomSeed
    )

fun SketchRoundedCornerShape(size: Dp, randomSeed: Int = 0) =
    SketchRoundedCornerShape(cornerSize = CornerSize(size), randomSeed = randomSeed)

fun SketchRoundedCornerShape(sizePixels: Float, randomSeed: Int = 0) =
    SketchRoundedCornerShape(cornerSize = CornerSize(sizePixels), randomSeed = randomSeed)

fun SketchRoundedCornerShape(percent: Int, randomSeed: Int = 0) =
    SketchRoundedCornerShape(cornerSize = CornerSize(percent), randomSeed = randomSeed)

fun SketchRoundedCornerShape(
    topStart: Dp,
    topEnd: Dp,
    bottomEnd: Dp,
    bottomStart: Dp,
    randomSeed: Int = 0
) = SketchRoundedCornerShape(
    topStart = CornerSize(topStart),
    topEnd = CornerSize(topEnd),
    bottomEnd = CornerSize(bottomEnd),
    bottomStart = CornerSize(bottomStart),
    randomSeed = randomSeed
)

fun SketchRoundedCornerShape(
    topStartPixels: Float,
    topEndPixels: Float,
    bottomEndPixels: Float,
    bottomStartPixels: Float,
    randomSeed: Int = 0
) = SketchRoundedCornerShape(
    topStart = CornerSize(topStartPixels),
    topEnd = CornerSize(topEndPixels),
    bottomEnd = CornerSize(bottomEndPixels),
    bottomStart = CornerSize(bottomStartPixels),
    randomSeed = randomSeed
)

fun SketchRoundedCornerShape(
    @IntRange(from = 0, to = 100) topStartPercent: Int,
    @IntRange(from = 0, to = 100) topEndPercent: Int,
    @IntRange(from = 0, to = 100) bottomEndPercent: Int,
    @IntRange(from = 0, to = 100) bottomStartPercent: Int,
    randomSeed: Int = 0
) = SketchRoundedCornerShape(
    topStart = CornerSize(topStartPercent),
    topEnd = CornerSize(topEndPercent),
    bottomEnd = CornerSize(bottomEndPercent),
    bottomStart = CornerSize(bottomStartPercent),
    randomSeed = randomSeed
)
