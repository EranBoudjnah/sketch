package com.mitteloupe.sketch

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mitteloupe.sketch.hachure.HachureIterator
import kotlin.random.Random

@Composable
fun Modifier.hachure(
    randomSeed: Int = 0,
    strokeThickness: Dp = 1.dp,
    gap: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = SketchRectangleShape(),
    style: Style = Style.Hatch(45f)
): Modifier {
    val maximumJitter = MAXIMUM_LATERAL_OFFSET.toPxWithDensity()
    val stepSizePixel = STEP_SIZE.toPxWithDensity()
    val thicknessPixel = strokeThickness.toPxWithDensity()
    val gapPixel = gap.toPxWithDensity()

    return drawWithContent {
        val outline = shape.createOutline(size, layoutDirection, this)
        val clipPath = Path().apply {
            when (outline) {
                is Outline.Rectangle -> addRect(outline.rect)
                is Outline.Rounded -> addRoundRect(outline.roundRect)
                is Outline.Generic -> addPath(outline.path)
            }
        }

        clipPath(clipPath) {
            with(style) {
                draw(
                    gapPixel = gapPixel,
                    randomSeed = randomSeed,
                    maximumJitter = maximumJitter,
                    stepSizePixel = stepSizePixel,
                    color = color,
                    thicknessPixel = thicknessPixel
                )
            }
        }

        drawContent()
    }
}

@Stable
@Composable
private fun Dp.toPxWithDensity() = with(LocalDensity.current) {
    toPx()
}

sealed class Style {
    abstract fun DrawScope.draw(
        gapPixel: Float,
        randomSeed: Int,
        maximumJitter: Float,
        stepSizePixel: Float,
        color: Color,
        thicknessPixel: Float
    )

    data class Hatch(private val angle: Float) : Style() {
        override fun DrawScope.draw(
            gapPixel: Float,
            randomSeed: Int,
            maximumJitter: Float,
            stepSizePixel: Float,
            color: Color,
            thicknessPixel: Float
        ) {
            drawHatch(
                angle = angle,
                gapPixel = gapPixel,
                randomSeed = randomSeed,
                maximumJitter = maximumJitter,
                stepSizePixel = stepSizePixel,
                color = color,
                thicknessPixel = thicknessPixel
            )
        }
    }

    data class CrossHatch(private val angle: Float) : Style() {
        override fun DrawScope.draw(
            gapPixel: Float,
            randomSeed: Int,
            maximumJitter: Float,
            stepSizePixel: Float,
            color: Color,
            thicknessPixel: Float
        ) {
            drawHatch(
                angle = angle,
                gapPixel = gapPixel,
                randomSeed = randomSeed,
                maximumJitter = maximumJitter,
                stepSizePixel = stepSizePixel,
                color = color,
                thicknessPixel = thicknessPixel
            )
            drawHatch(
                angle = angle + 90f,
                gapPixel = gapPixel,
                randomSeed = randomSeed,
                maximumJitter = maximumJitter,
                stepSizePixel = stepSizePixel,
                color = color,
                thicknessPixel = thicknessPixel
            )
        }
    }

    protected fun DrawScope.drawHatch(
        angle: Float,
        gapPixel: Float,
        randomSeed: Int,
        maximumJitter: Float,
        stepSizePixel: Float,
        color: Color,
        thicknessPixel: Float
    ) {
        val iterator = HachureIterator(
            top = 0f,
            bottom = size.height,
            left = 0f,
            right = size.width,
            gap = gapPixel,
            angleRadians = Math.toRadians(angle.toDouble()).toFloat()
        )
        var nextLine = iterator.nextLine()
        val path = Path()
        val random = Random(randomSeed)

        while (nextLine != null) {
            path.sketchedLine(
                random = random,
                pointA = nextLine.first,
                pointB = nextLine.second,
                maximumLateralOffsetPixel = maximumJitter,
                stepSizePixel = stepSizePixel,
                startWithMove = true
            )
            drawPath(path = path, color = color, style = Stroke(thicknessPixel))
            path.rewind()
            nextLine = iterator.nextLine()
        }
    }
}
