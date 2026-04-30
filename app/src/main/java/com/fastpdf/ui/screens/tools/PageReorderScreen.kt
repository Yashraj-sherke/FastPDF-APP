package com.fastpdf.ui.screens.tools

import android.content.Intent
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastpdf.ui.components.FilePickerButton
import com.fastpdf.ui.theme.AccentGreen
import com.fastpdf.ui.theme.AccentRed
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.PrimaryLight
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Page Reorder Screen — Drag & drop PDF page rearrangement.
 *
 * Phase 9: Pick a PDF → see page thumbnails → move up/down/delete → save reordered PDF.
 *
 * Uses:
 * - PdfRenderer for thumbnails
 * - iText 7 for reordering and saving
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageReorderScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var totalPages by remember { mutableIntStateOf(0) }

    // Page indices representing current order (0-based)
    val pageOrder = remember { mutableStateListOf<Int>() }
    // Set of removed page indices
    val removedPages = remember { mutableStateListOf<Int>() }

    var isProcessing by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }
    var selectedPageIndex by remember { mutableIntStateOf(-1) } // currently selected for move

    // File picker
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}

            selectedUri = it
            isSaved = false
            removedPages.clear()
            selectedPageIndex = -1

            context.contentResolver.query(it, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (ni >= 0) selectedName = c.getString(ni)
                }
            }

            // Count pages
            try {
                context.contentResolver.openFileDescriptor(it, "r")?.use { pfd ->
                    val renderer = PdfRenderer(pfd)
                    totalPages = renderer.pageCount
                    pageOrder.clear()
                    for (i in 0 until renderer.pageCount) {
                        pageOrder.add(i)
                    }
                    renderer.close()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reorder Pages",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // File picker
            FilePickerButton(
                label = "Select PDF",
                selectedFileName = selectedName,
                selectedFileSize = if (totalPages > 0) "$totalPages pages" else null,
                onClick = {
                    filePicker.launch(arrayOf("application/pdf"))
                }
            )

            if (pageOrder.isNotEmpty()) {
                // Info bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap a page to select, then move or delete",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${pageOrder.size - removedPages.size} pages",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }

                // Page grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = pageOrder.toList(),
                        key = { index, page -> "${index}_$page" }
                    ) { index, pageNum ->
                        val isRemoved = pageNum in removedPages
                        val isSelected = index == selectedPageIndex && !isRemoved

                        PageThumbnailCard(
                            pageNumber = pageNum + 1, // display as 1-based
                            listIndex = index,
                            isSelected = isSelected,
                            isRemoved = isRemoved,
                            onClick = {
                                if (!isRemoved) {
                                    selectedPageIndex = if (selectedPageIndex == index) -1 else index
                                }
                            }
                        )
                    }
                }

                // Action toolbar
                if (selectedPageIndex >= 0 && selectedPageIndex < pageOrder.size) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Move to first
                            IconButton(
                                onClick = {
                                    if (selectedPageIndex > 0) {
                                        val item = pageOrder.removeAt(selectedPageIndex)
                                        pageOrder.add(0, item)
                                        selectedPageIndex = 0
                                    }
                                },
                                enabled = selectedPageIndex > 0
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.ArrowUpward, "Move to top", Modifier.size(20.dp), tint = Primary)
                                    Text("First", fontSize = 10.sp, color = Primary)
                                }
                            }

                            // Move up
                            IconButton(
                                onClick = {
                                    if (selectedPageIndex > 0) {
                                        val item = pageOrder.removeAt(selectedPageIndex)
                                        val newIndex = selectedPageIndex - 1
                                        pageOrder.add(newIndex, item)
                                        selectedPageIndex = newIndex
                                    }
                                },
                                enabled = selectedPageIndex > 0
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.ArrowUpward, "Move up", Modifier.size(20.dp))
                                    Text("Up", fontSize = 10.sp)
                                }
                            }

                            // Move down
                            IconButton(
                                onClick = {
                                    if (selectedPageIndex < pageOrder.size - 1) {
                                        val item = pageOrder.removeAt(selectedPageIndex)
                                        val newIndex = selectedPageIndex + 1
                                        pageOrder.add(newIndex, item)
                                        selectedPageIndex = newIndex
                                    }
                                },
                                enabled = selectedPageIndex < pageOrder.size - 1
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.ArrowDownward, "Move down", Modifier.size(20.dp))
                                    Text("Down", fontSize = 10.sp)
                                }
                            }

                            // Move to last
                            IconButton(
                                onClick = {
                                    if (selectedPageIndex < pageOrder.size - 1) {
                                        val item = pageOrder.removeAt(selectedPageIndex)
                                        pageOrder.add(item)
                                        selectedPageIndex = pageOrder.size - 1
                                    }
                                },
                                enabled = selectedPageIndex < pageOrder.size - 1
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.ArrowDownward, "Move to bottom", Modifier.size(20.dp), tint = Primary)
                                    Text("Last", fontSize = 10.sp, color = Primary)
                                }
                            }

                            // Delete page
                            IconButton(
                                onClick = {
                                    val pageNum = pageOrder[selectedPageIndex]
                                    if (pageNum in removedPages) {
                                        removedPages.remove(pageNum)
                                    } else {
                                        removedPages.add(pageNum)
                                    }
                                    selectedPageIndex = -1
                                }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.Delete, "Remove page", Modifier.size(20.dp), tint = AccentRed)
                                    Text("Remove", fontSize = 10.sp, color = AccentRed)
                                }
                            }
                        }
                    }
                }

                // Save button
                Button(
                    onClick = {
                        if (!isProcessing && selectedUri != null) {
                            isProcessing = true
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    reorderPdf(context, selectedUri!!, pageOrder.toList(), removedPages.toSet())
                                }
                                isProcessing = false
                                if (result) {
                                    isSaved = true
                                    Toast.makeText(context, "Reordered PDF saved!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSaved) AccentGreen else Primary
                    ),
                    enabled = !isProcessing && pageOrder.isNotEmpty()
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Saving...", fontWeight = FontWeight.SemiBold)
                    } else if (isSaved) {
                        Icon(Icons.Filled.CheckCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Saved ✓", fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.Filled.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Reordered PDF", fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.SwapVert,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Reorder PDF Pages",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Select a PDF to rearrange, remove,\nor reorganize its pages",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PageThumbnailCard(
    pageNumber: Int,
    listIndex: Int,
    isSelected: Boolean,
    isRemoved: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isRemoved -> AccentRed.copy(alpha = 0.5f)
            isSelected -> Primary
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "border"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = tween(200),
        label = "elevation"
    )

    Card(
        modifier = Modifier
            .aspectRatio(0.707f) // A4 ratio
            .shadow(elevation, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRemoved)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Page number display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isRemoved) Modifier.background(
                            AccentRed.copy(alpha = 0.08f)
                        ) else Modifier
                    )
            ) {
                Text(
                    text = "$pageNumber",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isRemoved) AccentRed.copy(alpha = 0.5f)
                    else if (isSelected) Primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (isRemoved) {
                    Text(
                        "REMOVED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed.copy(alpha = 0.6f)
                    )
                }
            }

            // Page badge (top-end corner)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${listIndex + 1}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Reorder PDF pages using iText 7.
 * Creates a new PDF with pages in the specified order, excluding removed pages.
 */
private fun reorderPdf(
    context: android.content.Context,
    sourceUri: Uri,
    pageOrder: List<Int>,
    removedPages: Set<Int>
): Boolean {
    return try {
        val outputFile = File(context.cacheDir, "reordered_${System.currentTimeMillis()}.pdf")

        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            val reader = PdfReader(inputStream)
            val writer = PdfWriter(outputFile)
            val srcDoc = PdfDocument(reader)
            val destDoc = PdfDocument(writer)

            // Build the final page list: original order from pageOrder, skip removed
            val finalPages = pageOrder.filter { it !in removedPages }

            // iText pages are 1-based
            for (pageIndex in finalPages) {
                srcDoc.copyPagesTo(pageIndex + 1, pageIndex + 1, destDoc)
            }

            destDoc.close()
            srcDoc.close()
        }

        // Share the output
        val shareUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            outputFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Save Reordered PDF"))

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
