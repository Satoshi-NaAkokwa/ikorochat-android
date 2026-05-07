package com.ikoro.android.media

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

/**
 * Document picker for PDF and Office files
 */
@Composable
fun DocumentPicker(
    onDocumentSelected: (DocumentItem) -> Unit,
    maxFileSizeMB: Int = 50,
    allowedTypes: List<DocumentType> = DocumentType.values().toList()
) {
    val context = LocalContext.current
    var selectedDocument by remember { mutableStateOf<DocumentItem?>(null) }

    // Document picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val document = DocumentItem.fromUri(context, uri)
            if (document != null) {
                // Check file size
                val sizeKB = document.sizeKB
                if (sizeKB > maxFileSizeMB * 1024) {
                    // File too large
                    // Show error snackbar or toast
                } else {
                    selectedDocument = document
                    onDocumentSelected(document)
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Select Document",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allowedTypes.forEach { documentType ->
                    DocumentTypeButton(
                        documentType = documentType,
                        onClick = {
                            documentPickerLauncher.launch(documentType.mimeType)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Max file size: $maxFileSizeMB MB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Show selected document info
    selectedDocument?.let { doc ->
        Spacer(modifier = Modifier.height(16.dp))
        DocumentInfoCard(doc)
    }
}

/**
 * Document type button
 */
@Composable
private fun DocumentTypeButton(
    documentType: DocumentType,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = documentType.icon,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(documentType.displayName)
    }
}

/**
 * Display document info card
 */
@Composable
fun DocumentInfoCard(document: DocumentItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = document.type.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = document.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                    Text(
                        text = "${document.type.displayName} • ${formatDocumentSize(document.sizeKB)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Document item wrapper
 */
data class DocumentItem(
    val uri: Uri,
    val name: String,
    val type: DocumentType,
    val sizeKB: Long
) {
    companion object {
        fun fromUri(context: android.content.Context, uri: Uri): DocumentItem? {
            val contentResolver = context.contentResolver

            // Get file name
            val nameCursor = contentResolver.query(
                uri,
                arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )

            val name = nameCursor?.use {
                if (it.moveToFirst()) {
                    it.getString(0)
                } else {
                    "Unknown"
                }
            } ?: "Unknown"

            // Get file size
            val sizeCursor = contentResolver.query(
                uri,
                arrayOf(android.provider.OpenableColumns.SIZE),
                null,
                null,
                null
            )

            val size = sizeCursor?.use {
                if (it.moveToFirst()) {
                    it.getLong(0)
                } else {
                    0L
                }
            } ?: 0L

            // Get MIME type
            val mimeType = contentResolver.getType(uri)
            val type = DocumentType.fromMimeType(mimeType)

            return DocumentItem(
                uri = uri,
                name = name,
                type = type,
                sizeKB = size / 1024
            )
        }
    }

    val sizeInMB: Float
        get() = sizeKB / 1024f
}

/**
 * Document types
 */
enum class DocumentType(
    val displayName: String,
    val mimeType: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    PDF(
        displayName = "PDF Document",
        mimeType = "application/pdf",
        icon = Icons.Default.PictureAsPdf
    ),
    WORD(
        displayName = "Word Document",
        mimeType = "application/msword",
        icon = Icons.Default.Description
    ),
    WORD_X(
        displayName = "Word Document (.docx)",
        mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        icon = Icons.Default.Description
    ),
    EXCEL(
        displayName = "Excel Spreadsheet",
        mimeType = "application/vnd.ms-excel",
        icon = Icons.Default.TableChart
    ),
    EXCEL_X(
        displayName = "Excel Spreadsheet (.xlsx)",
        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        icon = Icons.Default.TableChart
    ),
    POWERPOINT(
        displayName = "PowerPoint Presentation",
        mimeType = "application/vnd.ms-powerpoint",
        icon = Icons.Default.Slideshow
    ),
    POWERPOINT_X(
        displayName = "PowerPoint Presentation (.pptx)",
        mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        icon = Icons.Default.Slideshow
    ),
    TEXT(
        displayName = "Text File",
        mimeType = "text/plain",
        icon = Icons.Default.TextSnippet
    ),
    RTF(
        displayName = "Rich Text Format",
        mimeType = "application/rtf",
        icon = Icons.Default.TextSnippet
    ),
    ANY(
        displayName = "Any Document",
        mimeType = "*/*",
        icon = Icons.Default.InsertDriveFile
    );

    companion object {
        fun fromMimeType(mimeType: String?): DocumentType {
            return when {
                mimeType == null -> ANY
                mimeType == "application/pdf" -> PDF
                mimeType == "application/msword" -> WORD
                mimeType.contains("wordprocessingml") -> WORD_X
                mimeType == "application/vnd.ms-excel" -> EXCEL
                mimeType.contains("spreadsheetml") -> EXCEL_X
                mimeType == "application/vnd.ms-powerpoint" -> POWERPOINT
                mimeType.contains("presentationml") -> POWERPOINT_X
                mimeType == "text/plain" -> TEXT
                mimeType == "application/rtf" -> RTF
                else -> ANY
            }
        }
    }
}

/**
 * Format document size
 */
fun formatDocumentSize(sizeKB: Long): String {
    return when {
        sizeKB < 1024 -> "$sizeKB KB"
        else -> String.format("%.1f MB", sizeKB / 1024.0)
    }
}

/**
 * Document list for displaying multiple documents
 */
@Composable
fun DocumentList(
    documents: List<DocumentItem>,
    modifier: Modifier = Modifier,
    onDocumentClick: (DocumentItem) -> Unit = {},
    onDocumentRemove: (DocumentItem) -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        documents.forEach { document ->
            DocumentListItem(
                document = document,
                onClick = { onDocumentClick(document) },
                onRemove = { onDocumentRemove(document) }
            )
        }
    }
}

/**
 * Single document list item
 */
@Composable
fun DocumentListItem(
    document: DocumentItem,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = document.type.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text = "${document.type.displayName} • ${formatDocumentSize(document.sizeKB)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove"
                )
            }
        }
    }
}

/**
 * Document picker with multiple selection
 */
@Composable
fun MultiDocumentPicker(
    onDocumentsSelected: (List<DocumentItem>) -> Unit,
    maxDocuments: Int = 5,
    maxFileSizeMB: Int = 50
) {
    var selectedDocuments by remember { mutableStateOf<List<DocumentItem>>(emptyList()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Documents",
                    style = MaterialTheme.typography.titleMedium
                )
                AssistChip(
                    onClick = { /* Open document picker */ },
                    label = {
                        Text("${selectedDocuments.size} / $maxDocuments")
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedDocuments.isEmpty()) {
                Button(
                    onClick = { /* Open document picker */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Document")
                }
            } else {
                DocumentList(
                    documents = selectedDocuments,
                    onDocumentRemove = { doc ->
                        selectedDocuments = selectedDocuments.filterNot { it == doc }
                    }
                )

                if (selectedDocuments.size < maxDocuments) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { /* Open document picker */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Another Document")
                    }
                }
            }
        }
    }

    if (selectedDocuments.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onDocumentsSelected(selectedDocuments) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done (${
   selectedDocuments.size} document${if(selectedDocuments.size > 1) "s" else ""})")
        }
    }
}