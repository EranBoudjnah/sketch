package com.mitteloupe.sketch.hachure

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

internal const val EPSILON_PARALLEL = 0.0001f
internal const val EPSILON_VERTICAL = 0.9999f

internal class HachureIterator(
    private val top: Float,
    private val bottom: Float,
    private val left: Float,
    private val right: Float,
    private val gap: Float,
    angleRadians: Float
) {
    private val sinAngle: Float = sin(angleRadians)
    private val cosAngle: Float = cos(angleRadians)
    private val tanAngle: Float = tan(angleRadians)

    private var currentOffset: Float = 0f
    private var lineWidthX: Float = 0f
    private var horizontalGap: Float = 0f
    private var leftSegment: Segment? = null
    private var rightSegment: Segment? = null

    init {
        when {
            abs(sinAngle) < EPSILON_PARALLEL -> {
                currentOffset = left + gap
            }

            abs(sinAngle) > EPSILON_VERTICAL -> {
                currentOffset = top + gap
            }

            else -> {
                lineWidthX = (bottom - top) * abs(tanAngle)
                currentOffset = left - abs(lineWidthX)
                horizontalGap = abs(gap / cosAngle)
                leftSegment = Segment(left, bottom, left, top)
                rightSegment = Segment(right, bottom, right, top)
            }
        }
    }

    fun nextLine(): Pair<Offset, Offset>? {
        return when {
            abs(sinAngle) < EPSILON_PARALLEL -> {
                if (currentOffset < right) {
                    val line = Offset(currentOffset, top) to Offset(currentOffset, bottom)
                    currentOffset += gap
                    line
                } else {
                    null
                }
            }

            abs(sinAngle) > EPSILON_VERTICAL -> {
                if (currentOffset < bottom) {
                    val line = Offset(left, currentOffset) to Offset(right, currentOffset)
                    currentOffset += gap
                    line
                } else {
                    null
                }
            }

            else -> {
                var xLower = currentOffset - lineWidthX / 2
                var xUpper = currentOffset + lineWidthX / 2
                var yLower = bottom
                var yUpper = top

                if (currentOffset < right + lineWidthX) {
                    while (
                        ((xLower < left && xUpper < left) || (xLower > right && xUpper > right))
                    ) {
                        currentOffset += horizontalGap
                        xLower = currentOffset - lineWidthX / 2
                        xUpper = currentOffset + lineWidthX / 2
                        if (currentOffset > right + lineWidthX) return null
                    }

                    val segment = Segment(xLower, yLower, xUpper, yUpper)
                    leftSegment?.let {
                        if (segment.compare(it) == Segment.Relation.INTERSECTS) {
                            xLower = segment.intersectionX
                            yLower = segment.intersectionY
                        }
                    }
                    rightSegment?.let {
                        if (segment.compare(it) == Segment.Relation.INTERSECTS) {
                            xUpper = segment.intersectionX
                            yUpper = segment.intersectionY
                        }
                    }

                    if (tanAngle > 0) {
                        xLower = right - (xLower - left)
                        xUpper = right - (xUpper - left)
                    }

                    val line = Offset(xLower, yLower) to Offset(xUpper, yUpper)
                    currentOffset += horizontalGap
                    line
                } else {
                    null
                }
            }
        }
    }
}
