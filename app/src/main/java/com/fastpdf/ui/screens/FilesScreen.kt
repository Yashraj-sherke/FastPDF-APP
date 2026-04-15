package com.fastpdf.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fastpdf.domain.model.DocumentFile
import com.fastpdf.domain.model.DocumentType
import com.fastpdf.ui.components.DocumentFileItem
import com.fastpdf.ui.theme.Primary

/**
 * Files Screen — Browse all documents.
 *
 * Layout matches reference screenshot:
 * - Top bar: Menu + "Files" + Search
 * - Sort dropdown + List/Grid toggle
 * - Scrollable file list with all document types
 * - FAB for adding new files
 *
 * Uses LazyColumn with keys for efficient diffing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    onFileClick: (String) -> Unit
) {
    var sortOption by remember { mutableStateOf("Recent First") }
    var isGridView by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Mock data — all supported document types
    val allFiles = remember {
        listOf(
            DocumentFile("f1", "Quarterly_Report_Q3.pdf", DocumentType.PDF, 4_404_019, "Oct 12, 2023"),
            DocumentFile("f2", "Architecture_Deep_Dive.pptx", DocumentType.POWERPOINT, 13_421_773, "Oct 10, 2023"),
            DocumentFile("f3", "Brand_Guidelines_v2.pdf", DocumentType.PDF, 8_912_896, "Oct 08, 2023"),
            DocumentFile("f4", "User_Interview_Script.docx", DocumentType.WORD, 1_153_434, "Oct 05, 2023"),
            DocumentFile("f5", "Product_Specs_Final.pdf", DocumentType.PDF, 2_516_582, "Sep 29, 2023"),
            DocumentFile("f6", "Contract_Draft_Signed.pdf", DocumentType.PDF, 5_976_883, "Sep 24, 2023"),
            DocumentFile("f7", "Market_Analysis_2024.xlsx", DocumentType.EXCEL, 15_938_355, "Sep 20, 2023"),
            DocumentFile("f8", "Team_Photo.jpg", DocumentType.IMAGE, 3_145_728, "Sep 18, 2023"),
            DocumentFile("f9", "Meeting_Notes.txt", DocumentType.TEXT, 24_576, "Sep 15, 2023"),
            DocumentFile("f10", "Sales_Dashboard.xlsx", DocumentType.EXCEL, 7_340_032, "Sep 12, 2023")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Files",
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
            // ━━━ Sort Bar ━━━
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sort dropdown
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showSortMenu = true }) {
                            Text(
                                text = sortOption,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Sort",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            listOf("Recent First", "Name", "Size").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        sortOption = option
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Grid/List toggle
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = "Toggle View",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ━━━ File List ━━━
            items(
                items = allFiles,
                key = { it.id }
            ) { file ->
                DocumentFileItem(
                    file = file,
                    onClick = { onFileClick(file.id) }
                )
            }

            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
