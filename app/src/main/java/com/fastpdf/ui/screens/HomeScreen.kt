package com.fastpdf.ui.screens

import android.content.Intent
import android.database.Cursor
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastpdf.data.CurrentFile
import com.fastpdf.data.ThemePreferences
import com.fastpdf.data.db.FileRepository
import com.fastpdf.domain.model.DocumentFile
import com.fastpdf.domain.model.DocumentType
import com.fastpdf.navigation.Screen
import com.fastpdf.ui.components.DocumentFileItem
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.PrimaryMedium
import com.fastpdf.ui.theme.PrimaryLight
import com.fastpdf.ui.theme.MintPale
import com.fastpdf.ui.theme.RecentCardStart
import com.fastpdf.ui.theme.RecentCardEnd
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
 * Document type filter for the horizontal chip row.
 */
private data class DocFilter(
    val label: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val type: DocumentType? // null = All
)

private val docFilters = listOf(
    DocFilter("All", Icons.Filled.Folder, Color(0xFFFFB300), Color(0xFFFFF8E1), null),
    DocFilter("PDF", Icons.Filled.PictureAsPdf, Color(0xFFE53935), Color(0xFFFFEBEE), DocumentType.PDF),
    DocFilter("Word", Icons.Filled.Description, Color(0xFF1565C0), Color(0xFFE3F2FD), DocumentType.WORD),
    DocFilter("Excel", Icons.Filled.TableChart, Color(0xFF2E7D32), Color(0xFFE8F5E9), DocumentType.EXCEL),
    DocFilter("PPT", Icons.Filled.Slideshow, Color(0xFFE65100), Color(0xFFFFF3E0), DocumentType.POWERPOINT)
)

/**
 * Home Screen — Redesigned to match "PDF Reader, Read All Docs" reference UI.
 *
 * Layout:
 * - Greeting header with settings icon
 * - Search bar
 * - Document type filter chips with colorful icons
 * - "Recently Added" dark teal card
 * - "All Documents" list with file rows
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<DocumentType?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val isDarkMode by ThemePreferences.isDarkMode(context).collectAsState(initial = false)

    // Filtered files based on search and type filter
    val filteredFiles = remember(allFiles, searchQuery, selectedFilter) {
        allFiles.filter { file ->
            val matchesSearch = searchQuery.isBlank() ||
                file.fileName.contains(searchQuery, ignoreCase = true)
            val matchesType = selectedFilter == null ||
                file.documentType == selectedFilter?.name
            matchesSearch && matchesType
        }
    }

    // SAF file picker — opens system file browser
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
            scope.launch {
                repository.recordFileAccess(
                    uri = selectedUri, fileName = fileName, mimeType = mimeType,
                    fileSize = fileSize, documentType = CurrentFile.type
                )
            }
            onFileClick("picked")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                isDarkMode = isDarkMode,
                onDarkModeToggle = { scope.launch { ThemePreferences.setDarkMode(context, it) } },
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    onNavigate(route)
                },
                onImportClick = {
                    scope.launch { drawerState.close() }
                    filePickerLauncher.launch(SUPPORTED_MIME_TYPES)
                }
            )
        }
    ) {
    Scaffold(
        floatingActionButton = {
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ━━━ Greeting Header with Hamburger ━━━
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Hamburger menu toggle
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Good morning",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 22.sp
                            )
                            Text(
                                text = "Reader 👋",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 28.sp
                            )
                        }
                    }
                    // Thumbs-up icon (matching screenshot)
                    IconButton(
                        onClick = {
                            // Open Play Store for rating
                            val rateUri = android.net.Uri.parse("market://details?id=com.fastpdf")
                            val rateIntent = Intent(Intent.ACTION_VIEW, rateUri)
                            try { context.startActivity(rateIntent) } catch (_: Exception) {
                                val webIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.fastpdf"))
                                try { context.startActivity(webIntent) } catch (_: Exception) { }
                            }
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.Filled.ThumbUp,
                            contentDescription = "Rate",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // ━━━ Search Bar ━━━
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search your documents...",
                            color = Color(0xFFADB5BD)
                        )
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = Color(0xFFADB5BD)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp)
                )
            }

            // ━━━ Document Type Filter Chips ━━━
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    docFilters.forEach { filter ->
                        val isSelected = selectedFilter == filter.type
                        DocTypeFilterChip(
                            filter = filter,
                            isSelected = isSelected,
                            onClick = {
                                selectedFilter = if (isSelected && filter.type != null) null else filter.type
                            }
                        )
                    }
                }
            }

            // ━━━ "Recently Added" Card ━━━
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp)
                        .clickable { onNavigate(Screen.Files.route) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(RecentCardStart, RecentCardEnd)
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Dark folder icon
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Folder,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Recently Added",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "View your latest files in one place",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            // Arrow
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ━━━ "All Documents" Header ━━━
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Documents",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onNavigate(Screen.Files.route) }
                    )
                }
            }

            // ━━━ Document List ━━━
            if (filteredFiles.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.InsertDriveFile,
                            contentDescription = null,
                            tint = Color(0xFFD1D5DB),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No documents found"
                                   else "Tap + to open your first document",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                items(filteredFiles.size) { index ->
                    val file = filteredFiles[index]
                    val docType = try {
                        DocumentType.valueOf(file.documentType)
                    } catch (_: Exception) {
                        DocumentType.OTHER
                    }

                    DocumentListRow(
                        fileName = file.fileName,
                        date = formatTimeAgo(file.lastAccessedAt),
                        fileSize = file.fileSize,
                        docType = docType,
                        onClick = {
                            val uri = android.net.Uri.parse(file.uriString)
                            CurrentFile.set(
                                uri = uri,
                                name = file.fileName,
                                mimeType = file.mimeType,
                                sizeBytes = file.fileSize
                            )
                            scope.launch {
                                repository.recordFileAccess(
                                    uri = uri,
                                    fileName = file.fileName,
                                    mimeType = file.mimeType,
                                    fileSize = file.fileSize,
                                    documentType = docType
                                )
                            }
                            onFileClick(file.uriString)
                        }
                    )
                }
            }

            // Bottom spacer for FAB
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
    } // End ModalNavigationDrawer
}

/**
 * Document type filter chip — matching the reference UI with colorful icons.
 */
@Composable
private fun DocTypeFilterChip(
    filter: DocFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = if (isSelected) 6.dp else 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = filter.iconTint.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isSelected) filter.iconTint.copy(alpha = 0.15f)
                    else filter.iconBg
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = filter.icon,
                contentDescription = filter.label,
                tint = filter.iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = filter.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

/**
 * Single document row in "All Documents" — matching the reference screenshot layout.
 * [DocTypeIcon] [FileName + Date · Size] [MoreVert menu]
 */
@Composable
private fun DocumentListRow(
    fileName: String,
    date: String,
    fileSize: Long,
    docType: DocumentType,
    onClick: () -> Unit
) {
    val displaySize = when {
        fileSize < 1024 -> "$fileSize B"
        fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
        fileSize < 1024 * 1024 * 1024 -> "%.1f MB".format(fileSize / (1024.0 * 1024.0))
        else -> "%.1f GB".format(fileSize / (1024.0 * 1024.0 * 1024.0))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File type icon with colored background
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(docType.tintColor.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = docType.icon,
                contentDescription = docType.displayName,
                tint = docType.tintColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // File name + metadata
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$date · $displaySize",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        // More options
        IconButton(onClick = { /* TODO: Options menu */ }) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Navigation drawer content — matching the reference "PDF Reader" side menu.
 */
@Composable
private fun HomeDrawerContent(
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onNavigate: (String) -> Unit,
    onImportClick: () -> Unit
) {
    val context = LocalContext.current
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            // ━━━ Header ━━━
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PDF Reader",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        Icons.Filled.ThumbUp,
                        contentDescription = "Rate",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // ━━━ Premium Banner ━━━
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 20.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                            )
                        )
                        .clickable {
                            // Open Play Store for premium
                            val uri = android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.fastpdf")
                            try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (_: Exception) { }
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Unlock Premium",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Remove all ads",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFFFB74D))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("GO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // ━━━ Main Menu Items ━━━
            item {
                DrawerMenuItem(Icons.Filled.SaveAlt, "Import PDF") { onImportClick() }
            }
            item {
                DrawerMenuItem(Icons.Filled.Help, "FAQ") { onNavigate(Screen.About.route) }
            }
            item {
                DrawerMenuItem(Icons.Filled.Feedback, "Request a new feature") {
                    val featureIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("mailto:support@fastpdf.app")
                        putExtra(Intent.EXTRA_SUBJECT, "FastPDF — Feature Request")
                        putExtra(Intent.EXTRA_TEXT, "Hi FastPDF team,\n\nI'd like to suggest a new feature:\n\n")
                    }
                    try { context.startActivity(featureIntent) } catch (_: Exception) { }
                }
            }

            // ━━━ Settings Section ━━━
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            item {
                DrawerToggleItem(
                    icon = Icons.Filled.DarkMode,
                    label = "Dark mode",
                    checked = isDarkMode,
                    onCheckedChange = onDarkModeToggle
                )
            }
            item {
                DrawerToggleItem(
                    icon = Icons.Filled.GppGood,
                    label = "Security question",
                    checked = false,
                    onCheckedChange = { }
                )
            }
            item {
                DrawerMenuItem(Icons.Filled.Settings, "Scan settings") {
                    onNavigate(Screen.Scanner.route)
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Language selector — currently only English supported
                            android.widget.Toast.makeText(context, "More languages coming soon!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                        Text("Language options", fontWeight = FontWeight.Normal, fontSize = 15.sp)
                        Text("English", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ━━━ About App Section ━━━
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    "About App",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(Screen.About.route) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.NewReleases, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                    Text("New version available", modifier = Modifier.weight(1f).padding(start = 16.dp), fontWeight = FontWeight.Normal, fontSize = 15.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEF5350))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("NEW", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item { DrawerMenuItem(Icons.Filled.Feedback, "Feedback") {
                val feedbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = android.net.Uri.parse("mailto:support@fastpdf.app")
                    putExtra(Intent.EXTRA_SUBJECT, "FastPDF Feedback")
                    putExtra(Intent.EXTRA_TEXT, "Hi FastPDF team,\n\n")
                }
                try { context.startActivity(feedbackIntent) } catch (_: Exception) { }
            } }
            item { DrawerMenuItem(Icons.Filled.Share, "Share app") {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "FastPDF — PDF Reader & Tools")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Check out FastPDF — a powerful PDF reader with scanning, editing, and AI features!\n\nhttps://play.google.com/store/apps/details?id=com.fastpdf"
                    )
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share FastPDF"))
            } }
            item { DrawerMenuItem(Icons.Filled.Policy, "Privacy policy") {
                val uri = android.net.Uri.parse("https://fastpdf.app/privacy")
                try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (_: Exception) { }
            } }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
        Text(label, modifier = Modifier.padding(start = 16.dp), fontWeight = FontWeight.Normal, fontSize = 15.sp)
    }
}

@Composable
private fun DrawerToggleItem(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
        Text(label, modifier = Modifier.weight(1f).padding(start = 16.dp), fontWeight = FontWeight.Normal, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Primary,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE0E0E0),
                uncheckedThumbColor = Color.White
            )
        )
    }
}
