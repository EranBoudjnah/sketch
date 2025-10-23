package com.mitteloupe.sketch

import androidx.compose.material3.DividerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = DividerDefaults.color,
    strokeWidth: Dp = 1.dp,
    seed: Int = 0
) {
    PathCanvas(
        modifier = modifier,
        pathGenerator = Path::sketchedLine,
        relativePointA = RelativePoint(.5f, 0f),
        relativePointB = RelativePoint(.5f, 1f),
        brush = SolidColor(color),
        strokeWidth = strokeWidth,
        randomSeed = seed
    )
}
