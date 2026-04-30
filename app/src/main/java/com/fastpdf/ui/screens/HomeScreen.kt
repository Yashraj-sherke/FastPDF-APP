package com.fastpdf.ui.screens

import android.content.Intent
import android.database.Cursor
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.fastpdf.data.CurrentFile
import com.fastpdf.data.db.FileRepository
import com.fastpdf.domain.model.DocumentFile
import com.fastpdf.domain.model.DocumentType
import com.fastpdf.navigation.Screen
import com.fastpdf.ui.components.DocumentFileItem
import com.fastpdf.ui.theme.Primary
import com.fastpdf.util.formatTimeAgo
import kotlinx.coroutines.launch

/**
 * Supported MIME types for the file picker.
 * Covers all document types FastPDF supports.
 */
private val SUPPORTED_MIME_TYPES = arrayOf(
    "application/pdf",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/vnd.ms-powerpoint",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "text/*",
    "image/*"
)

/**
 * Home Screen inspired by Solo Reader style layout.
 * - Tools shortcuts grid
 * - File type cards with counts
 * - More files quick actions
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onFileClick: (String) -> Unit,
    onAiClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { FileRepository(context) }

    // Observe Room data reactively
    val recentFiles by repository.recentFiles.collectAsState(initial = emptyList())
    val favorites by repository.favorites.collectAsState(initial = emptyList())
    val allFiles by repository.allFiles.collectAsState(initial = emptyList())
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val deletedCount by repository.deletedCount.collectAsState(initial = 0)

    val searchResults = remember(allFiles, searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            allFiles.filter { it.fileName.contains(searchQuery, ignoreCase = true) }.take(8)
        }
    }

    val pdfCount = remember(allFiles) { allFiles.count { it.documentType == DocumentType.PDF.name } }
    val docCount = remember(allFiles) { allFiles.count { it.documentType == DocumentType.WORD.name } }
    val xlsCount = remember(allFiles) { allFiles.count { it.documentType == DocumentType.EXCEL.name } }
    val pptCount = remember(allFiles) { allFiles.count { it.documentType == DocumentType.POWERPOINT.name } }
    val txtCount = remember(allFiles) { allFiles.count { it.documentType == DocumentType.TEXT.name } }
    val otherCount = remember(allFiles) {
        allFiles.count {
            it.documentType !in listOf(
                DocumentType.PDF.name,
                DocumentType.WORD.name,
                DocumentType.EXCEL.name,
                DocumentType.POWERPOINT.name,
                DocumentType.TEXT.name
            )
        }
    }

    val toolShortcuts = remember {
        listOf(
            Triple("Merge PDF", Icons.Filled.CallMerge, Screen.Merge.route),
            Triple("Split Files", Icons.Filled.ContentCut, Screen.Split.route),
            Triple("Compress", Icons.Filled.Compress, Screen.Compress.route),
            Triple("Image to PDF", Icons.Filled.Image, Screen.ImageToPdf.route),
            Triple("OCR", Icons.Filled.TextFields, Screen.Ocr.route),
            Triple("Secure PDF", Icons.Filled.Lock, Screen.Protect.route),
            Triple("Scan to PDF", Icons.Filled.DocumentScanner, Screen.Scanner.route),
            Triple("Convert", Icons.Filled.InsertDriveFile, Screen.Convert.route),
            Triple("AI Summary", Icons.Filled.TextSnippet, Screen.AiSummary.route)
        )
    }

    val fileTypePages = remember(allFiles, pdfCount, docCount, xlsCount, pptCount, txtCount, otherCount) {
        listOf(
            listOf(
                FileTypeItem("All Files", allFiles.size, Icons.Filled.Assignment, Color(0xFFEEF3FF)),
                FileTypeItem("PDF", pdfCount, Icons.Filled.PictureAsPdf, Color(0xFFFFEFEF)),
                FileTypeItem("DOC", docCount, Icons.Filled.Description, Color(0xFFEDF6FF)),
                FileTypeItem("XLS", xlsCount, Icons.Filled.TableChart, Color(0xFFEFFAF1))
            ),
            listOf(
                FileTypeItem("PPT", pptCount, Icons.Filled.Slideshow, Color(0xFFFFF3E9)),
                FileTypeItem("TXT", txtCount, Icons.Filled.TextSnippet, Color(0xFFEDFAFF)),
                FileTypeItem("Other", otherCount, Icons.Filled.InsertDriveFile, Color(0xFFF8F1FF))
            )
        )
    }
    val pagerState = rememberPagerState(pageCount = { fileTypePages.size })

    // SAF file picker — opens system file browser
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { selectedUri ->
            // Take persistent read permission
            try {
                context.contentResolver.takePersistableUriPermission(
                    selectedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Persistent permission not available — URI still works for this session
            }

            // Get file metadata
            var fileName = "Unknown File"
            var fileSize = 0L
            var mimeType = context.contentResolver.getType(selectedUri) ?: ""

            context.contentResolver.query(selectedUri, null, null, null, null)?.use { cursor: Cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex >= 0) fileSize = cursor.getLong(sizeIndex)
                }
            }

            // Set current file
            CurrentFile.set(
                uri = selectedUri,
                name = fileName,
                mimeType = mimeType,
                sizeBytes = fileSize
            )

            // Record in Room database
            scope.launch {
                repository.recordFileAccess(
                    uri = selectedUri,
                    fileName = fileName,
                    mimeType = mimeType,
                    fileSize = fileSize,
                    documentType = CurrentFile.type
                )
            }

            onFileClick("picked")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Solo Reader",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Drawer */ }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) {
                            searchQuery = ""
                        }
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch(SUPPORTED_MIME_TYPES) },
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Open File")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AnimatedVisibility(visible = showSearch) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search files by name") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        if (searchQuery.isNotBlank()) {
                            if (searchResults.isEmpty()) {
                                Text(
                                    text = "No files found",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            } else {
                                searchResults.forEach { result ->
                                    val docType = try {
                                        DocumentType.valueOf(result.documentType)
                                    } catch (_: Exception) {
                                        DocumentType.OTHER
                                    }

                                    DocumentFileItem(
                                        file = DocumentFile(
                                            id = result.uriString,
                                            name = result.fileName,
                                            type = docType,
                                            sizeBytes = result.fileSize,
                                            lastModified = formatTimeAgo(result.lastAccessedAt),
                                            isFavorite = result.isFavorite
                                        ),
                                        onClick = {
                                            val uri = android.net.Uri.parse(result.uriString)
                                            CurrentFile.set(
                                                uri = uri,
                                                name = result.fileName,
                                                mimeType = result.mimeType,
                                                sizeBytes = result.fileSize
                                            )
                                            scope.launch {
                                                repository.recordFileAccess(
                                                    uri = uri,
                                                    fileName = result.fileName,
                                                    mimeType = result.mimeType,
                                                    fileSize = result.fileSize,
                                                    documentType = docType
                                                )
                                            }
                                            onFileClick(result.uriString)
                                        },
                                        onFavoriteToggle = {
                                            scope.launch { repository.toggleFavorite(result.uriString) }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                SectionRow(title = "Tools", actionLabel = "See all") {
                    onNavigate(Screen.Tools.route)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    toolShortcuts.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { item ->
                                HomeShortcutCard(
                                    title = item.first,
                                    icon = item.second,
                                    tint = Primary,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (item.third == Screen.AiSummary.route) {
                                        onAiClick()
                                    } else {
                                        onNavigate(item.third)
                                    }
                                }
                            }
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                SectionRow(title = "File Types", actionLabel = "")
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            fileTypePages[page].forEach { type ->
                                FileTypeCard(
                                    item = type,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onNavigate(Screen.Files.route) }
                                )
                            }
                            repeat(4 - fileTypePages[page].size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(fileTypePages.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                .background(
                                    color = if (pagerState.currentPage == index) Primary else Color.LightGray,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionRow(title = "More Files", actionLabel = "See all") {
                    onNavigate(Screen.Files.route)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeShortcutCard(
                        title = "Recent",
                        subtitle = "${recentFiles.size} files",
                        icon = Icons.Filled.History,
                        tint = Color(0xFF5E8BFF),
                        modifier = Modifier.weight(1f)
                    ) { onNavigate(Screen.Files.route) }
                    HomeShortcutCard(
                        title = "Favorite",
                        subtitle = "${favorites.size} files",
                        icon = Icons.Filled.Star,
                        tint = Color(0xFFE8B931),
                        modifier = Modifier.weight(1f)
                    ) { onNavigate(Screen.Files.route) }
                    HomeShortcutCard(
                        title = "Recycle Bin",
                        subtitle = "$deletedCount files",
                        icon = Icons.Filled.DeleteOutline,
                        tint = Color(0xFF8A97A6),
                        modifier = Modifier.weight(1f)
                    ) { onNavigate(Screen.RecycleBin.route) }
                }
            }

            item {
                if (recentFiles.isEmpty()) {
                    Text(
                        text = "Tap + to open your first document",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    val latest = recentFiles.first()
                    val docType = try {
                        com.fastpdf.domain.model.DocumentType.valueOf(latest.documentType)
                    } catch (_: Exception) {
                        com.fastpdf.domain.model.DocumentType.OTHER
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable {
                                val uri = android.net.Uri.parse(latest.uriString)
                                CurrentFile.set(
                                    uri = uri,
                                    name = latest.fileName,
                                    mimeType = latest.mimeType,
                                    sizeBytes = latest.fileSize
                                )
                                onFileClick(latest.uriString)
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            // File type icon
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        docType.tintColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Icon(
                                    imageVector = docType.icon,
                                    contentDescription = null,
                                    tint = docType.tintColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Continue Reading",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = latest.fileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${docType.displayName} · Opened ${latest.accessCount} time${if (latest.accessCount != 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = Primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

private data class FileTypeItem(
    val title: String,
    val count: Int,
    val icon: ImageVector,
    val background: Color
)

@Composable
private fun SectionRow(
    title: String,
    actionLabel: String,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        if (actionLabel.isNotEmpty() && onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

@Composable
private fun HomeShortcutCard(
    title: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, fontSize = 12.sp, maxLines = 1)
            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun FileTypeCard(
    item: FileTypeItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(96.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(item.background, shape = RoundedCornerShape(8.dp)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(item.title, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            Text(
                text = "${item.count} files",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
