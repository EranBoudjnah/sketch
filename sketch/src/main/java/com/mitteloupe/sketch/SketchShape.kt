package com.mitteloupe.sketch

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Shape

interface SketchShape : Shape {
    @Stable
    fun simplifiedShape(): Shape
}
