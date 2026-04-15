package com.fastpdf.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fastpdf.ui.components.ProBannerCard
import com.fastpdf.ui.components.ToolCard

/**
 * Data class for tool items in the grid.
 */
private data class ToolItem(
    val icon: ImageVector,
    val label: String
)

/**
 * Tools Screen — Document processing tools grid.
 *
 * Layout matches reference screenshot:
 * - Top bar: Menu + "Digital Atelier" + Search
 * - "Tools" heading + subtitle
 * - 2-column grid of tool cards
 * - "Unlock Pro Tools" banner at bottom
 *
 * Uses LazyVerticalGrid for efficient grid rendering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen() {
    val tools = remember {
        listOf(
            ToolItem(Icons.Filled.CallMerge, "Merge"),
            ToolItem(Icons.Filled.ContentCut, "Split"),
            ToolItem(Icons.Filled.Compress, "Compress"),
            ToolItem(Icons.Filled.Image, "Image to PDF"),
            ToolItem(Icons.Filled.DocumentScanner, "Scan"),
            ToolItem(Icons.Filled.TextFields, "OCR"),
            ToolItem(Icons.Filled.Draw, "Sign"),
            ToolItem(Icons.Filled.SwapHoriz, "Convert")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Digital Atelier",
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ━━━ Header ━━━
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Tools",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Advanced document processing at your fingertips",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ━━━ Tool Grid ━━━
            items(
                items = tools,
                key = { it.label }
            ) { tool ->
                ToolCard(
                    icon = tool.icon,
                    label = tool.label,
                    onClick = { /* TODO: Navigate to tool */ }
                )
            }

            // ━━━ Pro Banner ━━━
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(8.dp))
                ProBannerCard()
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
