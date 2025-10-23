package com.mitteloupe.sketch

import androidx.compose.material3.DividerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val middleLeft = RelativePoint(0f, .5f)

private val middleRight = RelativePoint(1f, .5f)

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    color: Color = DividerDefaults.color,
    strokeWidth: Dp = 1.dp
) {
    PathCanvas(
        modifier = modifier,
        pathGenerator = Path::sketchedLine,
        relativePointA = middleLeft,
        relativePointB = middleRight,
        brush = SolidColor(color),
        strokeWidth = strokeWidth
    )
}
