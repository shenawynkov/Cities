package com.shenawynkov.cities.presentation.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Custom animation modifier for slide in from right and fade
fun Modifier.slideInFromRightAndFadeOnEnter(
    durationMillis: Int = 500,
    slideOffsetDp: Dp = 50.dp
): Modifier = composed {
    var visible by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMillis),
        label = "alphaOnEnter"
    )

    val translationX by animateFloatAsState(
        targetValue = if (visible) 0f else with(density) { slideOffsetDp.toPx() },
        animationSpec = tween(durationMillis = durationMillis),
        label = "slideInFromRight"
    )

    this.graphicsLayer {
        this.alpha = alpha
        this.translationX = translationX
    }
}
