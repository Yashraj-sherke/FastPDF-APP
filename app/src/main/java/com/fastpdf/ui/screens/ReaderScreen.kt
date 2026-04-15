package com.fastpdf.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fastpdf.data.CurrentFile
import com.fastpdf.domain.model.DocumentType
import com.fastpdf.ui.viewer.ImageViewerContent
import com.fastpdf.ui.viewer.OfficeViewerContent
import com.fastpdf.ui.viewer.PdfViewerContent
import com.fastpdf.ui.viewer.TextViewerContent

/**
 * Smart Reader Screen — routes to the correct viewer based on file type.
 *
 * Flow:
 * 1. User picks file via SAF → CurrentFile singleton is set
 * 2. Navigation sends user here
 * 3. This screen reads CurrentFile.type and shows the right viewer
 *
 * Supported:
 * - PDF → PdfViewerContent (native PdfRenderer)
 * - Image → ImageViewerContent (Coil + zoom)
 * - Text → TextViewerContent (scroll reader)
 * - Word/Excel/PPT → OfficeViewerContent (open with intent)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    fileId: String,
    onBack: () -> Unit
) {
    val fileName = CurrentFile.name.ifEmpty { "Document" }
    val fileType = CurrentFile.type

    // Image viewer has black background
    val isImageViewer = fileType == DocumentType.IMAGE && CurrentFile.uri != null
    val topBarContainerColor = if (isImageViewer) Color.Black.copy(alpha = 0.5f) else Color.Transparent
    val topBarContentColor = if (isImageViewer) Color.White else MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = topBarContentColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        CurrentFile.clear()
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = topBarContentColor
                        )
                    }
                },
                actions = {
                    if (CurrentFile.uri != null) {
                        IconButton(onClick = { /* TODO: Share */ }) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = "Share",
                                tint = topBarContentColor
                            )
                        }
                        IconButton(onClick = { /* TODO: More options */ }) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "More",
                                tint = topBarContentColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarContainerColor
                )
            )
        },
        containerColor = if (isImageViewer) Color.Black else MaterialTheme.colorScheme.background
    ) { innerPadding ->

        if (CurrentFile.uri == null) {
            // No file selected — placeholder
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📄",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Use the + button to open a file",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Route to the correct viewer based on file type
            when (fileType) {
                DocumentType.PDF -> PdfViewerContent()
                DocumentType.IMAGE -> ImageViewerContent()
                DocumentType.TEXT -> TextViewerContent()
                DocumentType.WORD,
                DocumentType.EXCEL,
                DocumentType.POWERPOINT -> OfficeViewerContent()
                DocumentType.OTHER -> OfficeViewerContent()
            }
        }
    }
}
