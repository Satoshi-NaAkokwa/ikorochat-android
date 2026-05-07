package com.ikoro.android.media

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Video Recorder with up to 5 minutes recording limit
 */
@Composable
fun VideoRecorder(
    onVideoRecorded: (Uri) -> Unit,
    maxDurationSeconds: Int = 300 // 5 minutes
) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val storageGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: true

        if (cameraGranted && audioGranted && storageGranted) {
            // Permissions granted, prepare video URI
            val videoUri = createVideoUri(context)
            // Launch camera intent with video capture
            val intent = android.media.action.VIDEO_CAPTURE.apply {
                putExtra(android.provider.MediaStore.EXTRA_OUTPUT, videoUri)
                putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1) // High quality
                putExtra(android.provider.MediaStore.EXTRA_DURATION_LIMIT, maxDurationSeconds)
            }
            val cameraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                isRecording = false
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    onVideoRecorded(videoUri)
                }
            }
            cameraLauncher.launch(intent)
        } else {
            showPermissionDialog = true
        }
    }

    // Permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Camera and Microphone Permission Required") },
            text = {
                Text(
                    "Camera and microphone permissions are needed to record videos. " +
                    "Storage permission is needed to save videos."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            )
                        )
                    }
                ) {
                    Text("Grant Permissions")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Recording UI
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Record Video",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recording indicator
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (recordingTime % 2 == 0) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                        )
                    }
                }
                Text(
                    text = formatTime(recordingTime),
                    style = MaterialTheme.typography.headlineSmall
                )
            } else {
                IconButton(
                    onClick = {
                        isRecording = true
                        recordingTime = 0
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            )
                        )
                    },
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    )
                }
                Text(
                    text = "Tap to Record",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Max duration: ${maxDurationSeconds / 60} minutes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Video recording button with preview
 */
@Composable
fun VideoRecordingButton(
    onVideoRecorded: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    showPreview: Boolean = true,
    maxDurationSeconds: Int = 300
) {
    var showRecorder by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (showRecorder) {
            VideoRecorder(
                onVideoRecorded = { uri ->
                    onVideoRecorded(uri)
                    showRecorder = false
                },
                maxDurationSeconds = maxDurationSeconds
            )
        } else {
            Button(
                onClick = { showRecorder = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Video")
            }
        }
    }
}

/**
 * Create a temporary video file
 */
private fun createVideoFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES)
    return File(storageDir, "VIDEO_${timestamp}.mp4").apply {
        createNewFile()
    }
}

/**
 * Create a URI for video recording
 */
private fun createVideoUri(context: Context): Uri {
    val file = createVideoFile(context)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

/**
 * Format recording time as MM:SS
 */
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

/**
 * Video preview with playback controls
 */
@Composable
fun VideoPreview(
    videoUri: Uri,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {}
) {
    Card(
        modifier = modifier
    ) {
        Column {
            // Video player would go here
            // For now, we'll show a placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Video Preview",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClose) {
                    Text("Close")
                }
            }
        }
    }
}
