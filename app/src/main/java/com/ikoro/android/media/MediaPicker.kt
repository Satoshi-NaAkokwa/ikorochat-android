package com.ikoro.android.media

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Media Picker for selecting multiple images and videos from gallery or camera
 */
@Composable
fun MediaPicker(
    onMediaSelected: (List<MediaItem>) -> Unit,
    maxSelection: Int = 10
) {
    val context = LocalContext.current
    var mediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Gallery launcher for multiple media
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        mediaUris = uris.take(maxSelection)
        val mediaItems = uris.mapNotNull { uri ->
            MediaItem.fromUri(context, uri)
        }.take(maxSelection)
        onMediaSelected(mediaItems)
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Create a URI for the captured photo
            val photoFile = createImageFile(context)
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            mediaUris = listOf(photoUri)
            val mediaItem = MediaItem.fromUri(context, photoUri)
            if (mediaItem != null) {
                onMediaSelected(listOf(mediaItem))
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        if (cameraGranted) {
            cameraLauncher.launch(createImageUri(context))
        }
    }

    // Permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Camera Permission Required") },
            text = { Text("Camera permission is needed to take photos and videos") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        )
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Media picker UI
    Column {
        Button(
            onClick = {
                galleryLauncher.launch("image/*")
            }
        ) {
            Text("Select Images")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                galleryLauncher.launch("video/*")
            }
        ) {
            Text("Select Videos")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA
                    )
                )
            }
        ) {
            Text("Take Photo")
        }
    }
}

/**
 * Create a temporary image file
 */
private fun createImageFile(context: android.content.Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    return File(storageDir, "JPEG_${timestamp}_").apply {
        createNewFile()
    }
}

/**
 * Create a URI for camera capture
 */
private fun createImageUri(context: android.content.Context): Uri {
    val file = createImageFile(context)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

/**
 * Media item representing an image or video
 */
sealed class MediaItem {
    abstract val uri: Uri
    abstract val type: MediaType

    data class Image(
        override val uri: Uri,
        val width: Int,
        val height: Int,
        val size: Long
    ) : MediaItem() {
        override val type: MediaType = MediaType.IMAGE
    }

    data class Video(
        override val uri: Uri,
        val duration: Long, // in milliseconds
        val size: Long
    ) : MediaItem() {
        override val type: MediaType = MediaType.VIDEO
    }

    companion object {
        fun fromUri(context: android.content.Context, uri: Uri): MediaItem? {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(
                uri,
                arrayOf(android.provider.MediaStore.MediaColumns.MIME_TYPE),
                null,
                null,
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val mimeType = it.getString(0)
                    return if (mimeType?.startsWith("image/") == true) {
                        Image(uri, 0, 0, 0) // Dimensions loaded asynchronously
                    } else if (mimeType?.startsWith("video/") == true) {
                        Video(uri, 0, 0) // Duration loaded asynchronously
                    } else {
                        null
                    }
                }
            }
            return null
        }
    }
}

enum class MediaType {
    IMAGE,
    VIDEO
}
