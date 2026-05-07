package com.ikoro.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikoro.android.model.IkoroMessage
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modern Message Bubble Component
 * Features:
 * - Rounded corners with different radii for top/bottom messages
 * - Color differentiation between sent/received messages
 * - Dark mode support
 * - Shadows and elevations
 * - Timestamp display
 */
@Composable
fun MessageBubble(
    message: IkoroMessage,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val timeFormatter = rememberTimeFormatter()
    val timestamp = timeFormatter.format(Date(message.timestamp))

    // Determine bubble color based on sender and theme
    val bubbleColor = if (isCurrentUser) {
        colorScheme.primaryContainer.copy(alpha = 0.9f)
    } else {
        colorScheme.surfaceColorAtElevation(2.dp)
    }

    // Determine bubble shape based on message position
    val cornerRadius = 16.dp

    // Text color based on bubble background
    val textColor = if (isCurrentUser) {
        colorScheme.onPrimaryContainer
    } else {
        colorScheme.onSurface
    }

    val timestampColor = colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 120.dp, max = 280.dp)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(bubbleColor)
                    .padding(12.dp, 10.dp, 12.dp, 8.dp),
                horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
            ) {
                // Message content
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )

                // Timestamp
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = timestampColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MessageBubbleWithReactions(
    message: IkoroMessage,
    isCurrentUser: Boolean,
    reactions: List<MessageReaction>,
    onReactionClick: (emoji: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        MessageBubble(
            message = message,
            isCurrentUser = isCurrentUser
        )

        // Reactions row
        if (reactions.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                reactions.forEach { reaction ->
                    ReactionChip(
                        emoji = reaction.emoji,
                        count = reaction.count,
                        onClick = { onReactionClick(reaction.emoji) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReactionChip(
    emoji: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    androidx.compose.material3.AssistChip(
        onClick = onClick,
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    fontSize = 16.sp
                )
                if (count > 1) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        modifier = modifier,
        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
            containerColor = colorScheme.secondaryContainer
        ),
        border = null
    )
}

@Composable
fun MessageBubbleWithMeta(
    message: IkoroMessage,
    isCurrentUser: Boolean,
    reactions: List<MessageReaction>,
    onReactionClick: (emoji: String) -> Unit,
    isEdited: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        // Message bubble with reactions
        MessageBubbleWithReactions(
            message = message,
            isCurrentUser = isCurrentUser,
            reactions = reactions,
            onReactionClick = onReactionClick
        )

        // Additional metadata (edited indicator, etc.)
        if (isEdited) {
            Text(
                text = "edited",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

data class MessageReaction(
    val emoji: String,
    val count: Int,
    val reacted: Boolean
)

private fun rememberTimeFormatter(): SimpleDateFormat {
    return SimpleDateFormat("HH:mm", Locale.getDefault())
}