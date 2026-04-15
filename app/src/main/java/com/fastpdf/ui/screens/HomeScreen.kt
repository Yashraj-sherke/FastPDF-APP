package com.fastpdf.ui.screens

import android.content.Intent
import android.database.Cursor
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fastpdf.data.CurrentFile
import com.fastpdf.domain.model.DocumentFile
import com.fastpdf.domain.model.DocumentType
import com.fastpdf.navigation.Screen
import com.fastpdf.ui.components.AiSummaryCard
import com.fastpdf.ui.components.DocumentFileItem
import com.fastpdf.ui.components.SectionHeader
import com.fastpdf.ui.theme.Primary

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
 * Home Screen with real file picker integration.
 *
 * - FAB opens system file picker (SAF — no permissions needed!)
 * - Picked file is stored in CurrentFile singleton
 * - Navigation to ReaderScreen for viewing
 * - Mock data shown for UI demonstration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onFileClick: (String) -> Unit
) {
    val context = LocalContext.current

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

            // Set current file and navigate to reader
            CurrentFile.set(
                uri = selectedUri,
                name = fileName,
                mimeType = mimeType,
                sizeBytes = fileSize
            )
            onFileClick("picked")
        }
    }

    // Mock data for UI demonstration
    val recentFiles = remember {
        listOf(
            DocumentFile("1", "Quarterly_Report_2024.pdf", DocumentType.PDF, 4_404_019, "Modified 2h ago"),
            DocumentFile("2", "Product_Design_Brief.pdf", DocumentType.PDF, 1_887_436, "Modified yesterday"),
            DocumentFile("3", "Sprint_Planning.docx", DocumentType.WORD, 524_288, "Modified 3h ago"),
            DocumentFile("4", "Budget_Q4.xlsx", DocumentType.EXCEL, 2_097_152, "Modified today")
        )
    }

    val favoriteFiles = remember {
        listOf(
            DocumentFile("5", "Brand_Guidelines_Final.pdf", DocumentType.PDF, 13_107_200, "Added Dec 12", isFavorite = true),
            DocumentFile("6", "Tax_Returns_2023.pdf", DocumentType.PDF, 870_400, "Added Nov 05", isFavorite = true),
            DocumentFile("7", "Team_Presentation.pptx", DocumentType.POWERPOINT, 8_388_608, "Added Oct 20", isFavorite = true)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My PDFs",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Drawer */ }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
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
                .padding(innerPadding)
        ) {
            // ━━━ Recent Files ━━━
            item { SectionHeader(title = "Recent Files") }
            items(items = recentFiles, key = { it.id }) { file ->
                DocumentFileItem(
                    file = file,
                    onClick = { onFileClick(file.id) },
                    onFavoriteToggle = { /* TODO */ }
                )
            }

            // ━━━ AI Summary Card ━━━
            item {
                Spacer(modifier = Modifier.height(8.dp))
                AiSummaryCard(fileCount = 12)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ━━━ Favorites ━━━
            item { SectionHeader(title = "Favorites") }
            items(items = favoriteFiles, key = { it.id }) { file ->
                DocumentFileItem(
                    file = file,
                    onClick = { onFileClick(file.id) },
                    onFavoriteToggle = { /* TODO */ }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
