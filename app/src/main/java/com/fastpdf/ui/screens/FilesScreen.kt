package com.fastpdf.ui.screens

import android.content.Intent
import android.database.Cursor
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fastpdf.data.CurrentFile
import com.fastpdf.data.db.FileHistoryEntity
import com.fastpdf.data.db.FileRepository
import com.fastpdf.domain.model.DocumentFile
import com.fastpdf.domain.model.DocumentType
import com.fastpdf.ui.components.DocumentFileItem
import com.fastpdf.ui.theme.AccentRed
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.PrimaryLight
import com.fastpdf.util.formatTimeAgo
import kotlinx.coroutines.launch

/**
 * Supported MIME types for file picker.
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
 * Files Screen — Real file browser with Room-backed data.
 * Redesigned with teal theme, clean white background, and modern styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    onFileClick: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { FileRepository(context) }

    var sortOption by remember { mutableStateOf("Recent First") }
    var isGridView by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    // Multi-select state
    var isSelectMode by remember { mutableStateOf(false) }
    val selectedUris = remember { mutableStateListOf<String>() }

    // Observe files — search or full list
    val allFiles by if (searchQuery.isNotBlank()) {
        repository.searchFiles(searchQuery).collectAsState(initial = emptyList())
    } else {
        repository.allFiles.collectAsState(initial = emptyList())
    }

    // Sort files
    val sortedFiles = remember(allFiles, sortOption) {
        when (sortOption) {
            "Name" -> allFiles.sortedBy { it.fileName.lowercase() }
            "Size" -> allFiles.sortedByDescending { it.fileSize }
            else -> allFiles // Already sorted by recent from Room
        }
    }

    // SAF file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { selectedUri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    selectedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { }

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

            CurrentFile.set(uri = selectedUri, name = fileName, mimeType = mimeType, sizeBytes = fileSize)

            // Record in Room
            scope.launch {
                repository.recordFileAccess(
                    uri = selectedUri, fileName = fileName, mimeType = mimeType,
                    fileSize = fileSize, documentType = CurrentFile.type
                )
            }

            onFileClick("picked")
        }
    }

    // Exit select mode helper
    fun exitSelectMode() {
        isSelectMode = false
        selectedUris.clear()
    }

    Scaffold(
        topBar = {
            if (isSelectMode) {
                // ━━━ Batch Select Top Bar ━━━
                TopAppBar(
                    title = {
                        Text(
                            text = "${selectedUris.size} selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { exitSelectMode() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = Primary)
                        }
                    },
                    actions = {
                        // Select all
                        IconButton(onClick = {
                            if (selectedUris.size == sortedFiles.size) {
                                selectedUris.clear()
                            } else {
                                selectedUris.clear()
                                selectedUris.addAll(sortedFiles.map { it.uriString })
                            }
                        }) {
                            Icon(Icons.Filled.SelectAll, contentDescription = "Select All", tint = Primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryLight
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = "My Files",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    },
                    actions = {
                        IconButton(onClick = { showSearch = !showSearch; if (!showSearch) searchQuery = "" }) {
                            Icon(
                                if (showSearch) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = Primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        },
        floatingActionButton = {
            if (!isSelectMode) {
                FloatingActionButton(
                    onClick = { filePickerLauncher.launch(SUPPORTED_MIME_TYPES) },
                    containerColor = Primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        spotColor = Primary.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Open File")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // ━━━ Search Bar ━━━
                item {
                    AnimatedVisibility(visible = showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search files...", color = Color(0xFFADB5BD)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color(0xFFADB5BD)) }
                        )
                    }
                }

                // ━━━ Sort Bar ━━━
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { showSortMenu = true }) {
                                Text(
                                    text = sortOption,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Sort",
                                    modifier = Modifier.size(20.dp),
                                    tint = Primary
                                )
                            }
                            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                                listOf("Recent First", "Name", "Size").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = { sortOption = option; showSortMenu = false }
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${sortedFiles.size} files",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(onClick = { isGridView = !isGridView }) {
                                Icon(
                                    imageVector = if (isGridView) Icons.Filled.ViewList else Icons.Filled.GridView,
                                    contentDescription = "Toggle View",
                                    tint = Primary
                                )
                            }
                        }
                    }
                }

                // ━━━ File List (from Room) ━━━
                if (sortedFiles.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.InsertDriveFile,
                                contentDescription = null,
                                tint = Color(0xFFD1D5DB),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No files match \"$searchQuery\"" else "No files yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Tap + to open a document",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(items = sortedFiles, key = { it.uriString }) { entity ->
                        val docType = try { DocumentType.valueOf(entity.documentType) } catch (_: Exception) { DocumentType.OTHER }
                        val isSelected = entity.uriString in selectedUris

                        @OptIn(ExperimentalFoundationApi::class)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectMode) {
                                            if (isSelected) selectedUris.remove(entity.uriString)
                                            else selectedUris.add(entity.uriString)
                                            if (selectedUris.isEmpty()) exitSelectMode()
                                        } else {
                                            val uri = android.net.Uri.parse(entity.uriString)
                                            CurrentFile.set(uri = uri, name = entity.fileName, mimeType = entity.mimeType, sizeBytes = entity.fileSize)
                                            scope.launch {
                                                repository.recordFileAccess(
                                                    uri = uri, fileName = entity.fileName, mimeType = entity.mimeType,
                                                    fileSize = entity.fileSize, documentType = docType
                                                )
                                            }
                                            onFileClick(entity.uriString)
                                        }
                                    },
                                    onLongClick = {
                                        if (!isSelectMode) {
                                            isSelectMode = true
                                            selectedUris.add(entity.uriString)
                                        }
                                    }
                                )
                                .background(
                                    if (isSelected) PrimaryLight else Color.Transparent
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Selection indicator
                            if (isSelectMode) {
                                Icon(
                                    imageVector = if (isSelected)
                                        Icons.Filled.CheckCircle
                                    else
                                        Icons.Filled.RadioButtonUnchecked,
                                    contentDescription = if (isSelected) "Selected" else "Unselected",
                                    tint = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .size(24.dp)
                                )
                            }

                            // File item (takes remaining space)
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentFileItem(
                                    file = DocumentFile(
                                        id = entity.uriString,
                                        name = entity.fileName,
                                        type = docType,
                                        sizeBytes = entity.fileSize,
                                        lastModified = formatTimeAgo(entity.lastAccessedAt),
                                        isFavorite = entity.isFavorite
                                    ),
                                    onClick = {
                                        // Handled by parent Row
                                    },
                                    onFavoriteToggle = {
                                        if (!isSelectMode) {
                                            scope.launch { repository.toggleFavorite(entity.uriString) }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Extra padding for batch bar
                item { Spacer(modifier = Modifier.height(if (isSelectMode) 140.dp else 80.dp)) }
            }

            // ━━━ Batch Action Bar ━━━
            AnimatedVisibility(
                visible = isSelectMode && selectedUris.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(innerPadding),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Primary.copy(alpha = 0.2f)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Batch Delete
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            scope.launch {
                                repository.batchSoftDelete(selectedUris.toList())
                                exitSelectMode()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = AccentRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Text("Delete", style = MaterialTheme.typography.labelSmall, color = AccentRed)
                    }

                    // Batch Favorite
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            scope.launch {
                                repository.batchFavorite(selectedUris.toList())
                                exitSelectMode()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Favorite",
                            tint = Color(0xFFE8B931),
                            modifier = Modifier.size(24.dp)
                        )
                        Text("Favorite", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE8B931))
                    }

                    // Batch Share
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                type = "*/*"
                                val uris = ArrayList(selectedUris.map { android.net.Uri.parse(it) })
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share ${selectedUris.size} files"))
                            exitSelectMode()
                        }
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text("Share", style = MaterialTheme.typography.labelSmall, color = Primary)
                    }
                }
            }
        }
    }
}
