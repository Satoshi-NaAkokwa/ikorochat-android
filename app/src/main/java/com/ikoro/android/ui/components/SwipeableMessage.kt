package com.ikoro.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import com.ikoro.android.model.IkoroMessage
import kotlin.math.roundToInt

/**
 * Swipeable Message Component
 * Features:
 * - Swipe to reply (left, green)
 * - Swipe to forward (right, blue)
 * - Swipe to delete (long left, red)
 * - Swipe to star (long right, yellow)
 * - Animated swipe actions
 * - Haptic feedback support
 */
@Composable
fun SwipeableMessage(
    message: IkoroMessage,
    isCurrentUser: Boolean,
    reactions: List<MessageReaction>,
    onReactionClick: (emoji: String) -> Unit,
    onSwipeReply: () -> Unit,
    onSwipeForward: () -> Unit,
    onSwipeDelete: () -> Unit,
    onSwipeStar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current

    Box(
        modifier = modifier
    ) {
        SwipeActionsContainer(
            message = message,
            isCurrentUser = isCurrentUser,
            onReplyClick = {
                hapticFeedback.performHapticFeedback(
                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                )
                onSwipeReply()
            },
            onForwardClick = {
                hapticFeedback.performHapticFeedback(
                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                )
                onSwipeForward()
            },
            onDeleteClick = {
                hapticFeedback.performHapticFeedback(
                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                )
                onSwipeDelete()
            },
            onStarClick = {
                hapticFeedback.performHapticFeedback(
                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                )
                onSwipeStar()
            }
        ) { swipeOffset ->
            MessageBubbleWithReactions(
                message = message,
                isCurrentUser = isCurrentUser,
                reactions = reactions,
                onReactionClick = onReactionClick,
                modifier = Modifier.offset { IntOffset(swipeOffset.roundToInt(), 0) }
            )
        }
    }
}

@Composable
private fun SwipeActionsContainer(
    message: IkoroMessage,
    isCurrentUser: Boolean,
    onReplyClick: () -> Unit,
    onForwardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStarClick: () -> Unit,
    content: @Composable (offset: Float) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    var offsetX by remember { mutableStateOf(0f) }
    val maxSwipeDistance = 200f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
    ) {
        // Background swipe actions
        if (offsetX < 0) {
            // Swipe left - show reply (short) and delete (long)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .background(
                        when {
                            offsetX < -maxSwipeDistance * 0.7f -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                Icon(
                    imageVector = if (offsetX < -maxSwipeDistance * 0.7f) {
                        Icons.Default.Delete
                    } else {
                        Icons.Default.Reply
                    },
                    contentDescription = if (offsetX < -maxSwipeDistance * 0.7f) "Delete" else "Reply",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 24.dp)
                )
                Text(
                    text = if (offsetX < -maxSwipeDistance * 0.7f) "Delete" else "Reply",
                    color = Color.White,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        } else if (offsetX > 0) {
            // Swipe right - show forward (short) and star (long)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .background(
                        when {
                            offsetX > maxSwipeDistance * 0.7f -> Color(0xFFFFC107)
                            else -> MaterialTheme.colorScheme.tertiary
                        }
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Icon(
                    imageVector = if (offsetX > maxSwipeDistance * 0.7f) {
                        Icons.Default.Star
                    } else {
                        Icons.Default.Forward
                    },
                    contentDescription = if (offsetX > maxSwipeDistance * 0.7f) "Star" else "Forward",
                    tint = if (offsetX > maxSwipeDistance * 0.7f) Color.Black else Color.White,
                    modifier = Modifier.padding(start = 24.dp)
                )
                Text(
                    text = if (offsetX > maxSwipeDistance * 0.7f) "Star" else "Forward",
                    color = if (offsetX > maxSwipeDistance * 0.7f) Color.Black else Color.White,
                    modifier = Modifier.padding(start = 24.dp)
                )
            }
        }

        // Foreground content with swipe gesture
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    androidx.compose.foundation.gestures.detectHorizontalDragGestures(
                        onDragEnd = {
                            val threshold = maxSwipeDistance * 0.5f
                            when {
                                offsetX < -maxSwipeDistance * 0.7f -> onDeleteClick()
                                offsetX < -threshold -> onReplyClick()
                                offsetX > maxSwipeDistance * 0.7f -> onStarClick()
                                offsetX > threshold -> onForwardClick()
                                else -> {
                                    // Reset if not enough swipe distance
                                    offsetX = 0f
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = offsetX + dragAmount
                            // Limit swipe distance
                            offsetX = newOffset.coerceIn(-maxSwipeDistance, maxSwipeDistance)
                        }
                    )
                }
        ) {
            content(offsetX)
        }
    }
}

@Composable
fun QuickActionButtons(
    onReply: () -> Unit,
    onForward: () -> Unit,
    onDelete: () -> Unit,
    onStar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reply button
        ActionButton(
            icon = Icons.Default.Reply,
            color = colorScheme.primary,
            onClick = onReply
        )

        // Forward button
        ActionButton(
            icon = Icons.Default.Forward,
            color = colorScheme.tertiary,
            onClick = onForward
        )

        // Star button
        ActionButton(
            icon = Icons.Default.Star,
            color = Color(0xFFFFC107),
            onClick = onStar
        )

        // Delete button
        ActionButton(
            icon = Icons.Default.Delete,
            color = colorScheme.error,
            onClick = onDelete
        )
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.1f)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}