package com.ikoro.android.calls.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ikoro.android.calls.CallManager
import com.ikoro.android.calls.CallState

@Composable
fun CallScreen(
    callManager: CallManager,
    modifier: Modifier = Modifier
) {
    val callState by callManager.callState.collectAsState()
    val error by callManager.error.collectAsState()
    var muted by remember { mutableStateOf(false) }
    var videoEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (callState) {
                is CallState.Idle -> "No active call"
                is CallState.Connecting -> "Connecting..."
                is CallState.Ringing -> "Incoming call"
                is CallState.InAudioCall -> "Audio call in progress"
                is CallState.InVideoCall -> "Video call in progress"
                is CallState.Error -> "Call error"
            },
            style = MaterialTheme.typography.headlineSmall
        )

        if (error.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = {
                muted = !muted
                callManager.toggleMute(muted)
            }) {
                Icon(
                    imageVector = if (muted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Toggle mute"
                )
            }

            if (callState is CallState.InVideoCall) {
                IconButton(onClick = {
                    videoEnabled = !videoEnabled
                    callManager.toggleVideo(videoEnabled)
                }) {
                    Icon(
                        imageVector = if (videoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Toggle video"
                    )
                }
            }

            FilledIconButton(
                onClick = { callManager.hangUp() },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "Hang up")
            }
        }
    }
}
