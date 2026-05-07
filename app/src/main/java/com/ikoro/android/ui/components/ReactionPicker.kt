package com.ikoro.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ikoro.android.model.IkoroMessage

/**
 * Reaction Picker Dialog
 * Shows emoji reactions that can be added to messages
 */
@Composable
fun ReactionPickerDialog(
    message: IkoroMessage,
    currentReactions: List<MessageReaction>,
    onReactionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("React to message")
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(commonEmojis.size) { index ->
                    val emoji = commonEmojis[index]
                    val isReacted = currentReactions.any { it.emoji == emoji }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isReacted) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .clickable { onReactionSelected(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private val commonEmojis = listOf(
    "👍", "❤️", "😂", "😮", "😢",
    "🔥", "🎉", "👀", "✨", "💯",
    "🙏", "👋", "🤔", "😎", "🤣",
    "💕", "😍", "🥳", "😭", "🤗"
)