package com.mitteloupe.sketch

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.random.Random

class SketchCircleShape(private val randomSeed: Int = 0) : SketchShape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val pointA = Offset.Zero
            val pointB = Offset(size.width, size.height)
            with(density) {
                sketchedCircle(Random(randomSeed), pointA, pointB, MAXIMUM_LATERAL_OFFSET.toPx())
            }
        }
        return Outline.Generic(path)
    }

    override fun simplifiedShape(): Shape = CircleShape
}
