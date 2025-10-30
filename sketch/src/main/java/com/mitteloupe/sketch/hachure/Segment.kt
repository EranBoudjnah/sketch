package com.mitteloupe.sketch.hachure

import kotlin.math.abs

internal data class Segment(val x1: Float, val y1: Float, val x2: Float, val y2: Float) {
    enum class Relation { INTERSECTS, NONE }

    var intersectionX: Float = 0f
        private set
    var intersectionY: Float = 0f
        private set

    fun compare(other: Segment): Relation {
        val denominator = (other.y2 - other.y1) * (x2 - x1) - (other.x2 - other.x1) * (y2 - y1)
        if (abs(denominator) < EPSILON_PARALLEL) {
            return Relation.NONE
        }

        val intersectionOnThisSegment =
            ((other.x2 - other.x1) * (y1 - other.y1) - (other.y2 - other.y1) * (x1 - other.x1)) /
                denominator
        val intersectionOnOtherSegment =
            ((x2 - x1) * (y1 - other.y1) - (y2 - y1) * (x1 - other.x1)) / denominator

        return if (intersectionOnThisSegment in 0f..1f && intersectionOnOtherSegment in 0f..1f) {
            intersectionX = x1 + intersectionOnThisSegment * (x2 - x1)
            intersectionY = y1 + intersectionOnThisSegment * (y2 - y1)
            Relation.INTERSECTS
        } else {
            Relation.NONE
        }
    }
}
