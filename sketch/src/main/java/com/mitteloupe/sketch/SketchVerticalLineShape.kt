import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.mitteloupe.sketch.MAXIMUM_LATERAL_OFFSET
import com.mitteloupe.sketch.STEP_SIZE
import com.mitteloupe.sketch.sketchedLine
import kotlin.random.Random

class SketchVerticalLineShape(private val randomSeed: Int = 0) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val pointA = Offset(size.width / 2f, 0f)
            val pointB = Offset(size.width / 2f, size.height)
            with(density) {
                sketchedLine(
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
