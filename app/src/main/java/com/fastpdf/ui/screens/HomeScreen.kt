package com.fastpdf.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fastpdf.domain.model.DocumentFile
import com.fastpdf.domain.model.DocumentType
import com.fastpdf.ui.components.AiSummaryCard
import com.fastpdf.ui.components.DocumentFileItem
import com.fastpdf.ui.components.SectionHeader
import com.fastpdf.ui.theme.Primary

/**
 * Home Screen — Main landing page.
 *
 * Layout matches reference screenshot:
 * - Top bar: Menu + "My PDFs" + Search
 * - Recent Files section
 * - AI Summary card row
 * - Favorites section
 * - FAB for adding new files
 *
 * Uses LazyColumn for performant scrolling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onFileClick: (String) -> Unit
) {
    // Mock data — will be replaced with real data layer in Day 2+
    val recentFiles = remember {
        listOf(
            DocumentFile(
                id = "1",
                name = "Quarterly_Report_2024.pdf",
                type = DocumentType.PDF,
                sizeBytes = 4_404_019,
                lastModified = "Modified 2h ago",
                isFavorite = false
            ),
            DocumentFile(
                id = "2",
                name = "Product_Design_Brief.pdf",
                type = DocumentType.PDF,
                sizeBytes = 1_887_436,
                lastModified = "Modified yesterday",
                isFavorite = false
            ),
            DocumentFile(
                id = "3",
                name = "Sprint_Planning.docx",
                type = DocumentType.WORD,
                sizeBytes = 524_288,
                lastModified = "Modified 3h ago",
                isFavorite = false
            ),
            DocumentFile(
                id = "4",
                name = "Budget_Q4.xlsx",
                type = DocumentType.EXCEL,
                sizeBytes = 2_097_152,
                lastModified = "Modified today",
                isFavorite = false
            )
        )
    }

    val favoriteFiles = remember {
        listOf(
            DocumentFile(
                id = "5",
                name = "Brand_Guidelines_Final.pdf",
                type = DocumentType.PDF,
                sizeBytes = 13_107_200,
                lastModified = "Added Dec 12",
                isFavorite = true
            ),
            DocumentFile(
                id = "6",
                name = "Tax_Returns_2023.pdf",
                type = DocumentType.PDF,
                sizeBytes = 870_400,
                lastModified = "Added Nov 05",
                isFavorite = true
            ),
            DocumentFile(
                id = "7",
                name = "Team_Presentation.pptx",
                type = DocumentType.POWERPOINT,
                sizeBytes = 8_388_608,
                lastModified = "Added Oct 20",
                isFavorite = true
            )
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
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add file */ },
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add File")
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
            item {
                SectionHeader(title = "Recent Files")
            }

            items(
                items = recentFiles,
                key = { it.id }
            ) { file ->
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
            item {
                SectionHeader(title = "Favorites")
            }

            items(
                items = favoriteFiles,
                key = { it.id }
            ) { file ->
                DocumentFileItem(
                    file = file,
                    onClick = { onFileClick(file.id) },
                    onFavoriteToggle = { /* TODO */ }
                )
            }

            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
