package com.fastpdf.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fastpdf.data.CurrentFile
import com.fastpdf.domain.model.DocumentType
import com.fastpdf.domain.model.DrawingStroke
import com.fastpdf.domain.model.EditTool
import com.fastpdf.domain.model.StampType
import com.fastpdf.domain.model.TextNote
import com.fastpdf.ui.editor.AnnotationToolbar
import com.fastpdf.ui.editor.DrawingCanvas
import com.fastpdf.ui.editor.SignaturePad
import com.fastpdf.ui.editor.StampPicker
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.viewer.ImageViewerContent
import com.fastpdf.ui.viewer.OfficeViewerContent
import com.fastpdf.ui.viewer.PdfViewerContent
import com.fastpdf.ui.viewer.TextViewerContent

/**
 * Smart Reader Screen with editing capabilities.
 *
 * Phase 4 additions:
 * - Edit mode toggle (pencil icon in top bar)
 * - Annotation toolbar (Pen, Highlighter, Text, Stamp)
 * - Drawing canvas overlay on documents
 * - Signature pad (bottom sheet)
 * - Stamp picker (bottom sheet)
 * - Undo/redo for annotations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    fileId: String,
    onBack: () -> Unit
) {
    val fileName = CurrentFile.name.ifEmpty { "Document" }
    val fileType = CurrentFile.type

    // Edit mode state
    var isEditMode by remember { mutableStateOf(false) }
    var activeTool by remember { mutableStateOf(EditTool.NONE) }
    var activeColor by remember { mutableStateOf(Color.Black) }

    // Annotation data
    val strokes = remember { mutableStateListOf<DrawingStroke>() }
    val undoneStrokes = remember { mutableStateListOf<DrawingStroke>() }
    val textNotes = remember { mutableStateListOf<TextNote>() }

    // Bottom sheet state
    var showSignaturePad by remember { mutableStateOf(false) }
    var showStampPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Image viewer has black background
    val isImageViewer = fileType == DocumentType.IMAGE && CurrentFile.uri != null
    val topBarContainerColor = if (isImageViewer) Color.Black.copy(alpha = 0.5f) else Color.Transparent
    val topBarContentColor = if (isImageViewer) Color.White else MaterialTheme.colorScheme.onSurface

    // Can this file type be annotated?
    val canEdit = CurrentFile.uri != null && fileType in listOf(
        DocumentType.PDF, DocumentType.IMAGE, DocumentType.TEXT
    )

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
                        // Edit toggle
                        if (canEdit) {
                            IconButton(onClick = {
                                isEditMode = !isEditMode
                                if (!isEditMode) activeTool = EditTool.NONE
                            }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit",
                                    tint = if (isEditMode) Primary else topBarContentColor
                                )
                            }
                        }
                        IconButton(onClick = { /* TODO: Share */ }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share", tint = topBarContentColor)
                        }
                        IconButton(onClick = { /* TODO: More */ }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = topBarContentColor)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarContainerColor)
            )
        },
        containerColor = if (isImageViewer) Color.Black else MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ━━━ Document Viewer ━━━
            if (CurrentFile.uri == null) {
                // Placeholder
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "📄", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Use the + button to open a file",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
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

            // ━━━ Drawing Canvas Overlay (edit mode) ━━━
            if (isEditMode && activeTool != EditTool.NONE) {
                DrawingCanvas(
                    strokes = strokes,
                    textNotes = textNotes,
                    activeTool = activeTool,
                    activeColor = activeColor,
                    onStrokeAdd = { stroke ->
                        strokes.add(stroke)
                        undoneStrokes.clear()
                    },
                    onTextTap = { position ->
                        textNotes.add(
                            TextNote(
                                text = "Note",
                                position = position,
                                color = activeColor
                            )
                        )
                    },
                    onEraseAt = { position ->
                        val toRemove = strokes.filter { stroke ->
                            stroke.points.any { point ->
                                (point - position).getDistance() < 30f
                            }
                        }
                        strokes.removeAll(toRemove)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ━━━ Annotation Toolbar (bottom) ━━━
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                AnnotationToolbar(
                    isVisible = isEditMode,
                    activeTool = activeTool,
                    activeColor = activeColor,
                    onToolSelect = { tool ->
                        when (tool) {
                            EditTool.SIGNATURE -> {
                                showSignaturePad = true
                                activeTool = EditTool.NONE
                            }
                            EditTool.STAMP -> {
                                showStampPicker = true
                                activeTool = EditTool.NONE
                            }
                            else -> activeTool = tool
                        }
                    },
                    onColorSelect = { color -> activeColor = color },
                    onUndo = {
                        if (strokes.isNotEmpty()) {
                            undoneStrokes.add(strokes.removeLast())
                        }
                    },
                    onRedo = {
                        if (undoneStrokes.isNotEmpty()) {
                            strokes.add(undoneStrokes.removeLast())
                        }
                    },
                    onClose = {
                        isEditMode = false
                        activeTool = EditTool.NONE
                    }
                )
            }
        }

        // ━━━ Signature Pad Bottom Sheet ━━━
        if (showSignaturePad) {
            ModalBottomSheet(
                onDismissRequest = { showSignaturePad = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                SignaturePad(
                    onSignatureDone = { signatureStrokes ->
                        // Convert signature strokes to annotation strokes
                        signatureStrokes.forEach { points ->
                            strokes.add(
                                DrawingStroke(
                                    points = points,
                                    color = Color(0xFF1A1C2E),
                                    strokeWidth = 3f
                                )
                            )
                        }
                        showSignaturePad = false
                    },
                    onCancel = { showSignaturePad = false }
                )
            }
        }

        // ━━━ Stamp Picker Bottom Sheet ━━━
        if (showStampPicker) {
            ModalBottomSheet(
                onDismissRequest = { showStampPicker = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                StampPicker(
                    onStampSelect = { stamp ->
                        // Add stamp as a text note at center
                        textNotes.add(
                            TextNote(
                                text = stamp.label,
                                position = Offset(200f, 400f),
                                color = stamp.color,
                                fontSize = 24f
                            )
                        )
                        showStampPicker = false
                    }
                )
            }
        }
    }
}
