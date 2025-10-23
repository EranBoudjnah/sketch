package com.mitteloupe.sketch

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.random.Random

class SketchCapsuleShape(private val randomSeed: Int = 0) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val pointA = Offset.Zero
            val pointB = Offset(size.width, size.height)
            with(density) {
                sketchedCapsule(
                    random = Random(randomSeed),
                    pointA = pointA,
                    pointB = pointB,
                    maximumLateralOffsetPixel = MAXIMUM_LATERAL_OFFSET.toPx(),
                    stepSizePixel = STEP_SIZE.toPx()
                )
            }
        }
        return Outline.Generic(path)
    }
}
