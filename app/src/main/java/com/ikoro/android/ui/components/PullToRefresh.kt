package com.ikoro.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

/**
 * Pull-to-Refresh Component
 * Features:
 * - Smooth swipe-to-refresh gesture
 * - Modern Material 3 design
 * - Progress indicator animation
 * - Haptic feedback
 */
@Composable
fun PullToRefreshLayout(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current

    var pullOffset by remember { mutableStateOf(0f) }
    val maxPullDistance = with(LocalDensity.current) { 150.dp.toPx() }
    val refreshThreshold = with(LocalDensity.current) { 80.dp.toPx() }

    // Reset pull offset when refresh completes
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing && pullOffset > 0) {
            pullOffset = 0f
        }
    }

    Box(
        modifier = modifier
    ) {
        // Refresh indicator
        if (pullOffset > 0 || isRefreshing) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset {
                        val y = pullOffset - maxPullDistance
                        IntOffset(0, y.roundToInt())
                    }
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            }
        }

        // Content container with pull gesture
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, pullOffset.roundToInt()) }
                .pointerInput(Unit) {
                    androidx.compose.foundation.gestures.detectVerticalDragGestures(
                        onDragEnd = {
                            if (pullOffset > refreshThreshold && !isRefreshing) {
                                hapticFeedback.performHapticFeedback(
                                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                                )
                                onRefresh()
                            }
                            // Animate back to position
                            pullOffset = 0f
                        },
                        onVerticalDrag = { _, dragAmount ->
                            // Only allow pulling down at the top of the list
                            if (dragAmount > 0) {
                                val newOffset = pullOffset + dragAmount
                                // Apply resistance as we pull further
                                val resistance = 1f - (newOffset / maxPullDistance).coerceIn(0f, 0.7f)
                                pullOffset = (newOffset * resistance).coerceIn(0f, maxPullDistance)
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

/**
 * Alternative pull-to-refresh using Material3's SwipeRefresh
 * Simpler implementation with built-in gesture handling
 */
@Composable
fun SimplePullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            hapticFeedback.performHapticFeedback(
                androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
            )
        }
    }

    Box(
        modifier = modifier
    ) {
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .size(40.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    var startY = 0f
                    var totalDrag = 0f
                    val threshold = with(LocalDensity.current) { 100.dp.toPx() }

                    androidx.compose.foundation.gestures.detectVerticalDragGestures(
                        onDragStart = { offset ->
                            startY = offset.y
                            totalDrag = 0f
                        },
                        onDragEnd = {
                            if (totalDrag > threshold && !isRefreshing) {
                                onRefresh()
                            }
                            totalDrag = 0f
                        },
                        onVerticalDrag = { _, dragAmount ->
                            totalDrag += dragAmount
                        }
                    )
                }
        ) {
            content()
        }
    }
}

/**
 * Pull-to-refresh header with icon
 */
@Composable
fun PullToRefreshHeader(
    pullProgress: Float, // 0f to 1f
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    val rotationAngle = pullProgress * 360f
    val alpha = pullProgress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .graphicsLayer {
                this.alpha = alpha
            },
        contentAlignment = Alignment.Center
    ) {
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Pull to refresh",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer {
                        rotationZ = rotationAngle
                    }
            )
        }
    }
}