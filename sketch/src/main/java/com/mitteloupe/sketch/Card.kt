package com.mitteloupe.sketch

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Card(
    modifier: Modifier = Modifier,
    shape: SketchShape = SketchRoundedCornerShape(12.0.dp),
    colors: CardColors = CardDefaults.cardColors(),
    elevation: SketchCardElevation = SketchCardElevation(),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.containerColor(enabled = true),
        contentColor = colors.contentColor(enabled = true),
        shadowElevation = elevation.shadowElevation(enabled = true, interactionSource = null).value,
        border = border
    ) {
        Column(content = content)
    }
}

@Stable
private fun CardColors.containerColor(enabled: Boolean): Color = if (enabled) {
    containerColor
} else {
    disabledContainerColor
}

@Stable
private fun CardColors.contentColor(enabled: Boolean) = if (enabled) {
    contentColor
} else {
    disabledContentColor
}

data class SketchCardElevation(
    val defaultElevation: Dp = 0.0.dp,
    val pressedElevation: Dp = 0.0.dp,
    val focusedElevation: Dp = 0.0.dp,
    val hoveredElevation: Dp = 1.0.dp,
    val draggedElevation: Dp = 6.0.dp
) {
    @Composable
    internal fun shadowElevation(
        enabled: Boolean,
        interactionSource: InteractionSource?
    ): State<Dp> {
        if (interactionSource == null) {
            return remember { mutableStateOf(0.0.dp) }
        }
        return animateElevation(enabled = enabled, interactionSource = interactionSource)
    }

    @Composable
    private fun animateElevation(
        enabled: Boolean,
        interactionSource: InteractionSource
    ): State<Dp> {
        val interactions = remember { mutableStateListOf<Interaction>() }
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is HoverInteraction.Enter -> {
                        interactions.add(interaction)
                    }

                    is HoverInteraction.Exit -> {
                        interactions.remove(interaction.enter)
                    }

                    is FocusInteraction.Focus -> {
                        interactions.add(interaction)
                    }

                    is FocusInteraction.Unfocus -> {
                        interactions.remove(interaction.focus)
                    }

                    is PressInteraction.Press -> {
                        interactions.add(interaction)
                    }

                    is PressInteraction.Release -> {
                        interactions.remove(interaction.press)
                    }

                    is PressInteraction.Cancel -> {
                        interactions.remove(interaction.press)
                    }

                    is DragInteraction.Start -> {
                        interactions.add(interaction)
                    }

                    is DragInteraction.Stop -> {
                        interactions.remove(interaction.start)
                    }

                    is DragInteraction.Cancel -> {
                        interactions.remove(interaction.start)
                    }
                }
            }
        }

        val interaction = interactions.lastOrNull()

        val target =
            if (!enabled) {
                0.0.dp
            } else {
                when (interaction) {
                    is PressInteraction.Press -> pressedElevation
                    is HoverInteraction.Enter -> hoveredElevation
                    is FocusInteraction.Focus -> focusedElevation
                    is DragInteraction.Start -> draggedElevation
                    else -> defaultElevation
                }
            }

        val animatable = remember { Animatable(target, Dp.VectorConverter) }

        LaunchedEffect(target) {
            if (animatable.targetValue != target) {
                if (!enabled) {
                    animatable.snapTo(target)
                } else {
                    val lastInteraction =
                        when (animatable.targetValue) {
                            pressedElevation -> PressInteraction.Press(Offset.Zero)
                            hoveredElevation -> HoverInteraction.Enter()
                            focusedElevation -> FocusInteraction.Focus()
                            draggedElevation -> DragInteraction.Start()
                            else -> null
                        }
                    animatable.animateElevation(
                        from = lastInteraction,
                        to = interaction,
                        target = target
                    )
                }
            }
        }

        return animatable.asState()
    }

    private suspend fun Animatable<Dp, *>.animateElevation(
        target: Dp,
        from: Interaction? = null,
        to: Interaction? = null
    ) {
        val spec =
            when {
                to != null -> ElevationDefaults.incomingAnimationSpecForInteraction(to)
                from != null -> ElevationDefaults.outgoingAnimationSpecForInteraction(from)
                else -> null
            }
        if (spec != null) {
            animateTo(target, spec)
        } else {
            snapTo(target)
        }
    }

    private object ElevationDefaults {
        fun incomingAnimationSpecForInteraction(interaction: Interaction): AnimationSpec<Dp>? =
            when (interaction) {
                is PressInteraction.Press -> DefaultIncomingSpec
                is DragInteraction.Start -> DefaultIncomingSpec
                is HoverInteraction.Enter -> DefaultIncomingSpec
                is FocusInteraction.Focus -> DefaultIncomingSpec
                else -> null
            }

        fun outgoingAnimationSpecForInteraction(interaction: Interaction): AnimationSpec<Dp>? =
            when (interaction) {
                is PressInteraction.Press -> DefaultOutgoingSpec
                is DragInteraction.Start -> DefaultOutgoingSpec
                is HoverInteraction.Enter -> HoveredOutgoingSpec
                is FocusInteraction.Focus -> DefaultOutgoingSpec
                else -> null
            }

        private val OutgoingSpecEasing: Easing = CubicBezierEasing(0.40f, 0.00f, 0.60f, 1.00f)

        private val DefaultIncomingSpec =
            TweenSpec<Dp>(durationMillis = 120, easing = FastOutSlowInEasing)

        private val DefaultOutgoingSpec =
            TweenSpec<Dp>(durationMillis = 150, easing = OutgoingSpecEasing)

        private val HoveredOutgoingSpec =
            TweenSpec<Dp>(durationMillis = 120, easing = OutgoingSpecEasing)
    }
}
