package com.ikoro.android.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ikoro.android.media.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(MediaTab.GALLERY) }
    var showImageCarousel by remember { mutableStateOf(false) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedDocuments by remember { mutableStateOf<List<DocumentItem>>(emptyList()) }
    var selectedVideos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Media") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // Tab selector
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            MediaTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.displayName) },
                    icon = { Icon(tab.icon, contentDescription = null) }
                )
            }
        }

        when (selectedTab) {
            MediaTab.GALLERY -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        GallerySection(
                            selectedImages = selectedImages,
                            onImagesSelected = { selectedImages = it },
                            onCarouselOpen = {
                                selectedImages = it
                                showImageCarousel = true
                            }
                        )
                    }
                }
            }
            MediaTab.VIDEOS -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        VideoSection(
                            selectedVideos = selectedVideos,
                            onVideosSelected = { selectedVideos = it }
                        )
                    }
                }
            }
            MediaTab.DOCUMENTS -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DocumentsSection(
                            selectedDocuments = selectedDocuments,
                            onDocumentsSelected = { selectedDocuments = it }
                        )
                    }
                }
            }
        }
    }

    // Full-screen image carousel
    if (showImageCarousel && selectedImages.isNotEmpty()) {
        ImageCarousel(
            images = selectedImages,
            modifier = Modifier.fillMaxSize(),
            onDismiss = { showImageCarousel = false }
        )
    }
}

@Composable
fun GallerySection(
    selectedImages: List<Uri>,
    onImagesSelected: (List<Uri>) -> Unit,
    onCarouselOpen: (List<Uri>) -> Unit
) {
    var showMediaPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Photo Gallery",
                    style = MaterialTheme.typography.titleLarge
                )
                if (selectedImages.isNotEmpty()) {
                    AssistChip(
                        onClick = { onCarouselOpen(selectedImages) },
                        label = { Text("View All") },
                        leadingIcon = {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedImages.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No photos yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { showMediaPicker = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Photos")
                    }
                }
            } else {
                // Show carousel preview
                CompactImageCarousel(
                    images = selectedImages,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    onImageClick = { index ->
                        // Open full carousel starting from selected image
                        onCarouselOpen(selectedImages.drop(index) + selectedImages.take(index))
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showMediaPicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add More")
                    }
                    Button(
                        onClick = { onImagesSelected(emptyList()) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear All")
                    }
                }
            }
        }
    }

    if (showMediaPicker) {
        AlertDialog(
            onDismissRequest = { showMediaPicker = false },
            title = { Text("Select Photos") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Choose a source for your photos",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    MediaPicker(
                        onMediaSelected = { mediaItems ->
                            val newImages = mediaItems.mapNotNull { it.uri }
                            onImagesSelected(selectedImages + newImages)
                            showMediaPicker = false
                        },
                        maxSelection = 10
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showMediaPicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun VideoSection(
    selectedVideos: List<Uri>,
    onVideosSelected: (List<Uri>) -> Unit
) {
    var showRecorder by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Videos",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedVideos.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No videos yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    VideoRecordingButton(
                        onVideoRecorded = { uri ->
                            onVideosSelected(selectedVideos + uri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    selectedVideos.forEach { videoUri ->
                        VideoPreview(
                            videoUri = videoUri,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    VideoRecordingButton(
                        onVideoRecorded = { uri ->
                            onVideosSelected(selectedVideos + uri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showRecorder) {
        AlertDialog(
            onDismissRequest = { showRecorder = false },
            title = { Text("Record Video") },
            text = {
                VideoRecorder(
                    onVideoRecorded = { uri ->
                        onVideosSelected(selectedVideos + uri)
                        showRecorder = false
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showRecorder = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun DocumentsSection(
    selectedDocuments: List<DocumentItem>,
    onDocumentsSelected: (List<DocumentItem>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Documents",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedDocuments.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No documents yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { /* Trigger document picker */ }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Document")
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DocumentList(
                        documents = selectedDocuments,
                        onDocumentRemove = { doc ->
                            onDocumentsSelected(selectedDocuments.filterNot { it == doc })
                        }
                    )

                    Button(
                        onClick = { /* Trigger document picker */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Document")
                    }
                }
            }
        }
    }
}

enum class MediaTab(
    val displayName: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    GALLERY("Gallery", Icons.Default.PhotoLibrary),
    VIDEOS("Videos", Icons.Default.Videocam),
    DOCUMENTS("Documents", Icons.Default.InsertDriveFile)
}
