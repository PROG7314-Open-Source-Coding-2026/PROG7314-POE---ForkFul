package com.emeris.forkful.ui.components

/**
 * Forkful - Tactile Micro-Interaction Modifier
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Provides subtle physical press-and-release spring animations for touchable cards and buttons.
 */

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

data class PressPop(
    val interactionSource: MutableInteractionSource,
    val modifier: Modifier
)

@Composable
fun rememberPressPop(pressedScale: Float = 0.96f): PressPop {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 500f),
        label = "pressPop"
    )

    return PressPop(
        interactionSource = interactionSource,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}

//------------------------------End-Of-File------------------------------//
