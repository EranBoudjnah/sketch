package com.mitteloupe.sketch

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import java.lang.Math.toRadians
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

val MAXIMUM_LATERAL_OFFSET = 1.5.dp
val STEP_SIZE = 2.dp
private val maximumDeviation = toRadians(15.0)
private val correctionRate = toRadians(2.5)

internal fun Path.sketchedLine(
    random: Random,
    pointA: Offset,
    pointB: Offset,
    maximumLateralOffsetPixel: Float,
    stepSizePixel: Float,
    startWithMove: Boolean = true
): Path {
    val totalLength = (pointA - pointB).getDistance()
    val baseDirection = (pointB - pointA).normalize()

    val numSegments = ceil(totalLength / stepSizePixel).toInt()

    var accumulatedAngle = 0.0

    var inCorrection = false

    if (startWithMove) {
        moveTo(pointA.x, pointA.y)
    }

    for (segmentIndex in 1 until numSegments) {
        val basePoint = pointA + baseDirection * (segmentIndex * stepSizePixel)

        if (!inCorrection && abs(accumulatedAngle) > maximumDeviation) {
            inCorrection = true
        } else if (inCorrection && abs(accumulatedAngle) <= toRadians(1.0)) {
            inCorrection = false
        }

        accumulatedAngle += when {
            inCorrection -> {
                if (accumulatedAngle > 0) {
                    -correctionRate
                } else {
                    correctionRate
                }
            }

            else -> {
                val jitterDegrees = random.nextInt(1, 3)
                val jitterDirection = if (random.nextInt(0, 2) == 0) {
                    -1
                } else {
                    1
                }
                toRadians(jitterDegrees * jitterDirection.toDouble())
            }
        }

        accumulatedAngle = accumulatedAngle.fastCoerceIn(-maximumDeviation, maximumDeviation)

        val baseDirectionNormal = Offset(-baseDirection.y, baseDirection.x)

        val lateralOffsetMagnitude =
            (accumulatedAngle / maximumDeviation) * maximumLateralOffsetPixel

        val offsetPoint = basePoint + baseDirectionNormal * lateralOffsetMagnitude.toFloat()

        lineTo(offsetPoint.x, offsetPoint.y)
    }

    lineTo(pointB.x, pointB.y)

    return this
}

internal fun Path.sketchedRectangle(
    random: Random,
    pointA: Offset,
    pointB: Offset,
    maximumLateralOffsetPixel: Float,
    stepSizePixel: Float
): Path = sketchedLine(
    random,
    Offset(pointB.x, pointA.y),
    pointA,
    maximumLateralOffsetPixel,
    stepSizePixel
).sketchedLine(
    random,
    pointA,
    Offset(pointA.x, pointB.y),
    maximumLateralOffsetPixel,
    stepSizePixel,
    false
).sketchedLine(
    random,
    Offset(pointA.x, pointB.y),
    pointB,
    maximumLateralOffsetPixel,
    stepSizePixel,
    false
).sketchedLine(
    random,
    pointB,
    Offset(pointB.x, pointA.y),
    maximumLateralOffsetPixel,
    stepSizePixel,
    false
).apply { close() }

internal fun Path.sketchedCircle(
    random: Random,
    pointA: Offset,
    pointB: Offset,
    maximumLateralOffsetPixel: Float,
    stepDegrees: Float = 5f
): Path = sketchedCircle(
    random = random,
    pointA = pointA,
    pointB = pointB,
    startAngle = 0f,
    sweep = 360f,
    maximumLateralOffsetPixel = maximumLateralOffsetPixel,
    stepDegrees = stepDegrees
)

internal fun Path.sketchedCircle(
    random: Random,
    pointA: Offset,
    pointB: Offset,
    startAngle: Float,
    sweep: Float,
    maximumLateralOffsetPixel: Float,
    stepDegrees: Float = 5f
): Path {
    val sweep = sweep.fastCoerceIn(0f, 360f)
    val center: Offset = (pointA + pointB) / 2f
    val radius: Float = minOf(abs(pointA.x - pointB.x), abs(pointA.y - pointB.y)) / 2f

    val pointsCount = (sweep / stepDegrees).toInt()
    var accumulatedAngle = 0.0
    var inCorrection = false

    val stepRadians = toRadians(stepDegrees.toDouble()).toFloat()

    var circleAngle = toRadians(startAngle.toDouble()).toFloat()
    for (i in 0..pointsCount) {
        val baseX = center.x + radius * cos(circleAngle)
        val baseY = center.y + radius * sin(circleAngle)
        val basePoint = Offset(baseX, baseY)

        val tangent = Offset((-sin(circleAngle)), cos(circleAngle)).normalize()

        if (i == 0) {
            moveTo(basePoint.x, basePoint.y)
        } else {
            if (!inCorrection && abs(accumulatedAngle) > maximumDeviation) {
                inCorrection = true
            } else if (inCorrection && abs(accumulatedAngle) <= toRadians(1.0)) {
                inCorrection = false
            }

            accumulatedAngle += when {
                inCorrection -> {
                    if (accumulatedAngle > 0) -correctionRate else correctionRate
                }

                else -> {
                    val jitterDegrees = random.nextInt(1, 3)
                    val jitterDirection = if (random.nextInt(0, 2) == 0) -1 else 1
                    toRadians(jitterDegrees * jitterDirection.toDouble())
                }
            }

            accumulatedAngle = accumulatedAngle.fastCoerceIn(-maximumDeviation, maximumDeviation)
        }

        val normal = Offset(-tangent.y, tangent.x)

        val lateralOffsetMagnitude =
            (accumulatedAngle / maximumDeviation) * maximumLateralOffsetPixel

        val offsetPoint = basePoint + normal * lateralOffsetMagnitude.toFloat()

        lineTo(offsetPoint.x, offsetPoint.y)
        circleAngle += stepRadians
    }

    if (sweep == 360f) {
        close()
    }
    return this
}

internal fun Path.sketchedCapsule(
    random: Random,
    pointA: Offset,
    pointB: Offset,
    maximumLateralOffsetPixel: Float,
    stepSizePixel: Float,
    stepDegrees: Float = 5f
): Path {
    val right = maxOf(pointA.x, pointB.x)
    val top = minOf(pointA.y, pointB.y)
    val left = minOf(pointA.x, pointB.x)
    val bottom = maxOf(pointA.y, pointB.y)

    val width = right - left
    val height = bottom - top

    if (width <= height) {
        return sketchedCircle(random, pointA, pointB, maximumLateralOffsetPixel, stepDegrees)
    }

    val radius = height / 2f
    val centerRight = Offset(right - radius, top + radius)
    drawArc(
        random = random,
        center = centerRight,
        radius = radius,
        startAngleDegrees = -90f,
        endAngleDegrees = 90f,
        maximumLateralOffsetPixel = maximumLateralOffsetPixel,
        stepDegrees = stepDegrees,
        startWithMove = true
    )

    val bottomLeft = Offset(left + radius, bottom)
    val bottomRight = Offset(right - radius, bottom)
    sketchedLine(
        random,
        bottomRight,
        bottomLeft,
        maximumLateralOffsetPixel,
        stepSizePixel,
        startWithMove = false
    )

    val centerLeft = Offset(left + radius, top + radius)
    drawArc(
        random = random,
        center = centerLeft,
        radius = radius,
        startAngleDegrees = 90f,
        endAngleDegrees = 270f,
        maximumLateralOffsetPixel = maximumLateralOffsetPixel,
        stepDegrees = stepDegrees,
        startWithMove = false
    )

    val topLeft = Offset(left + radius, top)
    val topRight = Offset(right - radius, top)
    sketchedLine(
        random,
        topLeft,
        topRight,
        maximumLateralOffsetPixel,
        stepSizePixel,
        startWithMove = false
    )

    close()
    return this
}

internal fun Path.sketchedRoundRectangle(
    random: Random,
    pointA: Offset,
    pointB: Offset,
    maximumLateralOffsetPixel: Float,
    stepSizePixel: Float,
    topLeftCornerRadius: Float,
    topRightCornerRadius: Float,
    bottomRightCornerRadius: Float,
    bottomLeftCornerRadius: Float,
    stepDegrees: Float = 5f
): Path {
    val left = minOf(pointA.x, pointB.x)
    val right = maxOf(pointA.x, pointB.x)
    val top = minOf(pointA.y, pointB.y)
    val bottom = maxOf(pointA.y, pointB.y)

    val width = right - left
    val height = bottom - top

    if (topRightCornerRadius > 0f) {
        val centerTopRight = Offset(right - topRightCornerRadius, top + topRightCornerRadius)
        drawArc(
            random = random,
            center = centerTopRight,
            radius = topRightCornerRadius,
            startAngleDegrees = -90f,
            endAngleDegrees = 0f,
            maximumLateralOffsetPixel = maximumLateralOffsetPixel,
            stepDegrees = stepDegrees,
            startWithMove = true
        )
    } else {
        moveTo(right, top)
    }

    if (height > topRightCornerRadius + bottomRightCornerRadius) {
        sketchedLine(
            random,
            Offset(right, top + topRightCornerRadius),
            Offset(right, bottom - bottomRightCornerRadius),
            maximumLateralOffsetPixel,
            stepSizePixel,
            startWithMove = false
        )
    }

    if (bottomRightCornerRadius > 0f) {
        val centerBottomRight =
            Offset(right - bottomRightCornerRadius, bottom - bottomRightCornerRadius)
        drawArc(
            random = random,
            center = centerBottomRight,
            radius = bottomRightCornerRadius,
            startAngleDegrees = 0f,
            endAngleDegrees = 90f,
            maximumLateralOffsetPixel = maximumLateralOffsetPixel,
            stepDegrees = stepDegrees,
            startWithMove = false
        )
    }

    if (width > bottomLeftCornerRadius + bottomRightCornerRadius) {
        sketchedLine(
            random,
            Offset(right - bottomRightCornerRadius, bottom),
            Offset(left + bottomLeftCornerRadius, bottom),
            maximumLateralOffsetPixel,
            stepSizePixel,
            startWithMove = false
        )
    }

    if (bottomLeftCornerRadius > 0f) {
        val centerBottomLeft =
            Offset(left + bottomLeftCornerRadius, bottom - bottomLeftCornerRadius)
        drawArc(
            random = random,
            center = centerBottomLeft,
            radius = bottomLeftCornerRadius,
            startAngleDegrees = 90f,
            endAngleDegrees = 180f,
            maximumLateralOffsetPixel = maximumLateralOffsetPixel,
            stepDegrees = stepDegrees,
            startWithMove = false
        )
    }

    if (height > topLeftCornerRadius + bottomLeftCornerRadius) {
        sketchedLine(
            random,
            Offset(left, bottom - bottomLeftCornerRadius),
            Offset(left, top + topLeftCornerRadius),
            maximumLateralOffsetPixel,
            stepSizePixel,
            startWithMove = false
        )
    }

    if (topLeftCornerRadius > 0f) {
        val centerTopLeft = Offset(left + topLeftCornerRadius, top + topLeftCornerRadius)
        drawArc(
            random = random,
            center = centerTopLeft,
            radius = topLeftCornerRadius,
            startAngleDegrees = 180f,
            endAngleDegrees = 270f,
            maximumLateralOffsetPixel = maximumLateralOffsetPixel,
            stepDegrees = stepDegrees,
            startWithMove = false
        )
    }

    if (width > topLeftCornerRadius + topRightCornerRadius) {
        sketchedLine(
            random,
            Offset(left + topLeftCornerRadius, top),
            Offset(right - topRightCornerRadius, top),
            maximumLateralOffsetPixel,
            stepSizePixel,
            startWithMove = false
        )
    }

    close()

    return this
}

private fun Path.drawArc(
    random: Random,
    center: Offset,
    radius: Float,
    startAngleDegrees: Float,
    endAngleDegrees: Float,
    maximumLateralOffsetPixel: Float,
    stepDegrees: Float,
    startWithMove: Boolean
) {
    val direction = if (endAngleDegrees > startAngleDegrees) 1 else -1
    val totalDegrees = abs(endAngleDegrees - startAngleDegrees)
    val steps = (totalDegrees / stepDegrees).toInt()
    val step = toRadians(stepDegrees.toDouble()) * direction
    val startAngle = toRadians(startAngleDegrees.toDouble())

    var accumulatedAngle = 0.0
    var inCorrection = false

    for (i in 0..steps) {
        val angle = startAngle + step * i

        val baseX = center.x + radius * cos(angle).toFloat()
        val baseY = center.y + radius * sin(angle).toFloat()
        val basePoint = Offset(baseX, baseY)

        val tangent = Offset(-sin(angle).toFloat(), cos(angle).toFloat()).normalize()
        val normal = Offset(-tangent.y, tangent.x)

        if (i == 0 && startWithMove) {
            moveTo(basePoint.x, basePoint.y)
        }

        if (!inCorrection && abs(accumulatedAngle) > maximumDeviation) {
            inCorrection = true
        } else if (inCorrection && abs(accumulatedAngle) <= toRadians(1.0)) {
            inCorrection = false
        }

        accumulatedAngle += when {
            inCorrection -> if (accumulatedAngle > 0) -correctionRate else correctionRate
            else -> {
                val jitterDegrees = random.nextInt(1, 3)
                val jitterDirection = if (random.nextInt(0, 2) == 0) -1 else 1
                toRadians(jitterDegrees * jitterDirection.toDouble())
            }
        }

        accumulatedAngle = accumulatedAngle.fastCoerceIn(-maximumDeviation, maximumDeviation)

        val lateralOffsetMagnitude =
            (accumulatedAngle / maximumDeviation) * maximumLateralOffsetPixel

        val offsetPoint = basePoint + normal * lateralOffsetMagnitude.toFloat()

        lineTo(offsetPoint.x, offsetPoint.y)
    }
}

private fun Offset.normalize(): Offset {
    val length = getDistance()
    return if (length == 0f) {
        Offset.Zero
    } else {
        this / length
    }
}
