package com.mitteloupe.sketch

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.FocusedIndicatorThickness
import androidx.compose.material3.TextFieldDefaults.UnfocusedIndicatorThickness
import androidx.compose.material3.TextFieldDefaults.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private const val TEXT_FIELD_ANIMATION_DURATION = 150

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape? = null,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    seed: Int = 0
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val textColor =
        textStyle.color.takeOrElse {
            val focused = interactionSource.collectIsFocusedAsState().value
            colors.textColor(enabled, isError, focused)
        }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))
    val random by remember(seed) {
        mutableStateOf(Random(seed))
    }
    val shape = shape ?: SketchRoundedCornerShape(CornerSize(4.dp), seed).top()
    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = value,
            modifier = modifier
                .defaultErrorSemantics(
                    isError,
                    stringResource(Strings.DefaultErrorMessage.value)
                )
                .defaultMinSize(
                    minWidth = TextFieldDefaults.MinWidth,
                    minHeight = TextFieldDefaults.MinHeight
                ),
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            cursorBrush = SolidColor(colors.cursorColor(isError)),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            decorationBox = @Composable { innerTextField ->
                @OptIn(ExperimentalMaterial3Api::class)
                TextFieldDefaults.DecorationBox(
                    value = value,
                    visualTransformation = visualTransformation,
                    innerTextField = innerTextField,
                    placeholder = placeholder,
                    label = label,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    shape = shape,
                    singleLine = singleLine,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = colors,
                    container = {
                        Container(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            modifier = Modifier,
                            colors = colors,
                            shape = shape,
                            focusedIndicatorLineThickness = FocusedIndicatorThickness,
                            unfocusedIndicatorLineThickness = UnfocusedIndicatorThickness,
                            random = random
                        )
                    }
                )
            }
        )
    }
}

@Composable
private fun Container(
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
    modifier: Modifier = Modifier,
    colors: TextFieldColors = colors(),
    shape: Shape = TextFieldDefaults.shape,
    focusedIndicatorLineThickness: Dp,
    unfocusedIndicatorLineThickness: Dp,
    random: Random
) {
    val focused = interactionSource.collectIsFocusedAsState().value
    val containerColor =
        animateColorAsState(
            targetValue = colors.containerColor(enabled, isError, focused),
            animationSpec = tween(durationMillis = TEXT_FIELD_ANIMATION_DURATION)
        )
    val maximumJitter = with(LocalDensity.current) {
        MAXIMUM_LATERAL_OFFSET.toPx()
    }
    val stepSizePixel = with(LocalDensity.current) {
        STEP_SIZE.toPx()
    }

    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val stablePath = remember(canvasSize, random) {
        val relativePointA = RelativePoint(0f, 1f)
        val relativePointB = RelativePoint(1f, 1f)
        val pointA =
            Offset(relativePointA.x * canvasSize.width, relativePointA.y * canvasSize.height)
        val pointB =
            Offset(relativePointB.x * canvasSize.width, relativePointB.y * canvasSize.height)
        Path().sketchedLine(random, pointA, pointB, maximumJitter, stepSizePixel)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Box(
        modifier
            .onSizeChanged {
                canvasSize = Size(it.width.toFloat(), it.height.toFloat())
            }
            .textFieldBackground(containerColor::value, shape)
            .sketchIndicatorLine(
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSource,
                colors = colors,
                focusedIndicatorLineThickness = focusedIndicatorLineThickness,
                unfocusedIndicatorLineThickness = unfocusedIndicatorLineThickness,
                path = stablePath
            )
    )
}

@JvmInline
@Immutable
private value class Strings(val value: Int) {
    companion object {
        inline val DefaultErrorMessage
            @SuppressLint("PrivateResource")
            get() = Strings(androidx.compose.ui.R.string.default_error_message)
    }
}

private fun TextFieldColors.textColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
    when {
        !enabled -> disabledTextColor
        isError -> errorTextColor
        focused -> focusedTextColor
        else -> unfocusedTextColor
    }

@Stable
private fun TextFieldColors.cursorColor(isError: Boolean): Color = if (isError) {
    errorCursorColor
} else {
    cursorColor
}

@Stable
private fun TextFieldColors.containerColor(
    enabled: Boolean,
    isError: Boolean,
    focused: Boolean
): Color = when {
    !enabled -> disabledContainerColor
    isError -> errorContainerColor
    focused -> focusedContainerColor
    else -> unfocusedContainerColor
}

@Stable
private fun TextFieldColors.indicatorColor(
    enabled: Boolean,
    isError: Boolean,
    focused: Boolean
): Color = when {
    !enabled -> disabledIndicatorColor
    isError -> errorIndicatorColor
    focused -> focusedIndicatorColor
    else -> unfocusedIndicatorColor
}

private fun Modifier.defaultErrorSemantics(
    isError: Boolean,
    defaultErrorMessage: String
): Modifier = if (isError) {
    semantics { error(defaultErrorMessage) }
} else {
    this
}

private fun Modifier.textFieldBackground(color: ColorProducer, shape: Shape): Modifier =
    this.drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        onDrawBehind { drawOutline(outline, color = color()) }
    }

fun Modifier.sketchIndicatorLine(
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
    colors: TextFieldColors,
    focusedIndicatorLineThickness: Dp = FocusedIndicatorThickness,
    unfocusedIndicatorLineThickness: Dp = UnfocusedIndicatorThickness,
    path: Path
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "indicatorLine"
        properties["enabled"] = enabled
        properties["isError"] = isError
        properties["interactionSource"] = interactionSource
        properties["colors"] = colors
        properties["focusedIndicatorLineThickness"] = focusedIndicatorLineThickness
        properties["unfocusedIndicatorLineThickness"] = unfocusedIndicatorLineThickness
    }
) {
    val focused = interactionSource.collectIsFocusedAsState().value
    val stroke =
        animateBorderStrokeAsState(
            enabled,
            isError,
            focused,
            colors,
            focusedIndicatorLineThickness,
            unfocusedIndicatorLineThickness
        )
    Modifier.drawIndicatorLine(stroke, path)
}

private fun Modifier.drawIndicatorLine(indicatorBorder: State<BorderStroke>, path: Path): Modifier =
    drawWithContent {
        drawContent()
        val strokeWidth = indicatorBorder.value.width.toPx()
        drawPath(
            path = path,
            brush = indicatorBorder.value.brush,
            style = Stroke(width = strokeWidth)
        )
    }

@Composable
private fun animateBorderStrokeAsState(
    enabled: Boolean,
    isError: Boolean,
    focused: Boolean,
    colors: TextFieldColors,
    focusedBorderThickness: Dp,
    unfocusedBorderThickness: Dp
): State<BorderStroke> {
    val targetColor = colors.indicatorColor(enabled, isError, focused)
    val indicatorColor =
        if (enabled) {
            animateColorAsState(targetColor, tween(durationMillis = TEXT_FIELD_ANIMATION_DURATION))
        } else {
            rememberUpdatedState(targetColor)
        }

    val thickness =
        if (enabled) {
            val targetThickness = if (focused) focusedBorderThickness else unfocusedBorderThickness
            animateDpAsState(targetThickness, tween(durationMillis = TEXT_FIELD_ANIMATION_DURATION))
        } else {
            rememberUpdatedState(unfocusedBorderThickness)
        }

    return rememberUpdatedState(BorderStroke(thickness.value, indicatorColor.value))
}
