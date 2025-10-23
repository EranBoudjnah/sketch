package com.mitteloupe.sketch

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.random.Random

data class SketchRoundRectangleShape(
    private val topStart: CornerSize,
    private val topEnd: CornerSize,
    private val bottomEnd: CornerSize,
    private val bottomStart: CornerSize,
    private val randomSeed: Int = 0
) : Shape {
    constructor(cornerSize: CornerSize, randomSeed: Int = 0) : this(
        topStart = cornerSize,
        topEnd = cornerSize,
        bottomEnd = cornerSize,
        bottomStart = cornerSize,
        randomSeed = randomSeed
    )

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val pointA = Offset.Zero
            val pointB = Offset(size.width, size.height)
            with(density) {
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
}
