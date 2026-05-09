package com.fastpdf.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Panorama
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Water
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastpdf.navigation.Screen as NavScreen
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.ToolBlue
import com.fastpdf.ui.theme.ToolBlueBg
import com.fastpdf.ui.theme.ToolGreen
import com.fastpdf.ui.theme.ToolGreenBg
import com.fastpdf.ui.theme.ToolOrange
import com.fastpdf.ui.theme.ToolOrangeBg
import com.fastpdf.ui.theme.ToolPurple
import com.fastpdf.ui.theme.ToolPurpleBg
import com.fastpdf.ui.theme.ToolRed
import com.fastpdf.ui.theme.ToolRedBg
import com.fastpdf.ui.theme.ToolYellow
import com.fastpdf.ui.theme.ToolYellowBg

/**
 * Action types for tools that don't navigate to a separate screen.
 */
private const val ACTION_SCAN = "action:scan"
private const val ACTION_EDIT = "action:edit"
private const val ACTION_IMPORT = "action:import"
private const val ACTION_CREATE_FOLDER = "action:create_folder"
private const val ACTION_PRINT = "action:print"

/**
 * Data class for tool items — each has a colorful circular icon.
 */
private data class ToolItem(
    val icon: ImageVector,
    val label: String,
    val iconColor: Color,
    val bgColor: Color,
    val route: String = ""
)

/**
 * Tools Screen — Categorized document processing tools.
 *
 * Matching reference UI: 4-column grid with colorful circular icons
 * organized into Convert, Edit, Manage, Other sections.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onScanClick: () -> Unit = {},
    onToolClick: (String) -> Unit = {}
) {
    val context = LocalContext.current

    // ━━━ Convert Tools ━━━
    val convertTools = listOf(
        ToolItem(Icons.Filled.Image, "Image to PDF", ToolRed, ToolRedBg, NavScreen.ImageToPdf.route),
        ToolItem(Icons.Filled.DocumentScanner, "Scan to PDF", ToolGreen, ToolGreenBg, ACTION_SCAN),
        ToolItem(Icons.Filled.Panorama, "PDF to Image", ToolOrange, ToolOrangeBg, NavScreen.Convert.route),
        ToolItem(Icons.Filled.TextFields, "OCR Text", ToolBlue, ToolBlueBg, NavScreen.Ocr.route)
    )

    // ━━━ Edit Tools ━━━
    val editTools = listOf(
        ToolItem(Icons.Filled.Edit, "Edit Text", ToolBlue, ToolBlueBg, ACTION_EDIT),
        ToolItem(Icons.Filled.NoteAdd, "Add Text", ToolOrange, ToolOrangeBg, ACTION_EDIT),
        ToolItem(Icons.Filled.Draw, "Annotate", ToolRed, ToolRedBg, ACTION_EDIT),
        ToolItem(Icons.Filled.Water, "Watermark", ToolPurple, ToolPurpleBg, NavScreen.Watermark.route)
    )

    // ━━━ Manage Tools ━━━
    val manageTools = listOf(
        ToolItem(Icons.Filled.SaveAlt, "Import PDF", ToolBlue, ToolBlueBg, ACTION_IMPORT),
        ToolItem(Icons.Filled.CreateNewFolder, "Create Folder", ToolGreen, ToolGreenBg, ACTION_CREATE_FOLDER),
        ToolItem(Icons.Filled.Delete, "Recycle Bin", ToolRed, ToolRedBg, NavScreen.RecycleBin.route),
        ToolItem(Icons.Filled.Print, "Print", ToolOrange, ToolOrangeBg, ACTION_PRINT)
    )

    // ━━━ Other Tools ━━━
    val otherTools = listOf(
        ToolItem(Icons.Filled.CallMerge, "Merge PDF", ToolYellow, ToolYellowBg, NavScreen.Merge.route),
        ToolItem(Icons.Filled.ContentCut, "Split PDF", ToolGreen, ToolGreenBg, NavScreen.Split.route),
        ToolItem(Icons.Filled.Reorder, "Manage Pages", ToolBlue, ToolBlueBg, NavScreen.PageReorder.route),
        ToolItem(Icons.Filled.Compress, "Compress", ToolOrange, ToolOrangeBg, NavScreen.Compress.route),
        ToolItem(Icons.Filled.Lock, "Lock PDF", ToolRed, ToolRedBg, NavScreen.Protect.route),
        ToolItem(Icons.Filled.LockOpen, "Unlock PDF", ToolGreen, ToolGreenBg, NavScreen.Protect.route)
    )

    /** Handle tool clicks — route-based navigation or action-based logic */
    fun handleToolClick(tool: ToolItem) {
        when (tool.route) {
            ACTION_SCAN -> onScanClick()
            ACTION_EDIT -> {
                Toast.makeText(
                    context,
                    "Open a document first, then use the Edit toolbar",
                    Toast.LENGTH_SHORT
                ).show()
            }
            ACTION_IMPORT -> {
                // Navigate to Files screen where import is available
                onToolClick(NavScreen.Files.route)
            }
            ACTION_CREATE_FOLDER -> {
                Toast.makeText(context, "Folder creation coming soon!", Toast.LENGTH_SHORT).show()
            }
            ACTION_PRINT -> {
                Toast.makeText(
                    context,
                    "Open a document first, then use Share → Print",
                    Toast.LENGTH_SHORT
                ).show()
            }
            "" -> { /* No action */ }
            else -> onToolClick(tool.route)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Drawer */ }) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ━━━ Page Title ━━━
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Tools",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
                )
            }

            // ━━━ Convert Section ━━━
            item(span = { GridItemSpan(maxLineSpan) }) {
                CategoryHeader(title = "Convert")
            }
            items(items = convertTools, key = { it.label }) { tool ->
                CircularToolItem(
                    tool = tool,
                    onClick = { handleToolClick(tool) }
                )
            }

            // ━━━ Edit Section ━━━
            item(span = { GridItemSpan(maxLineSpan) }) {
                CategoryHeader(title = "Edit")
            }
            items(items = editTools, key = { it.label }) { tool ->
                CircularToolItem(
                    tool = tool,
                    onClick = { handleToolClick(tool) }
                )
            }

            // ━━━ Manage Section ━━━
            item(span = { GridItemSpan(maxLineSpan) }) {
                CategoryHeader(title = "Manage")
            }
            items(items = manageTools, key = { it.label }) { tool ->
                CircularToolItem(
                    tool = tool,
                    onClick = { handleToolClick(tool) }
                )
            }

            // ━━━ Other Section ━━━
            item(span = { GridItemSpan(maxLineSpan) }) {
                CategoryHeader(title = "Other")
            }
            items(items = otherTools, key = { it.label }) { tool ->
                CircularToolItem(
                    tool = tool,
                    onClick = { handleToolClick(tool) }
                )
            }

            // Bottom spacing
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

/**
 * Section header for tool categories — "Convert", "Edit", "Manage", "Other"
 */
@Composable
private fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 12.dp)
    )
}

/**
 * Circular tool item matching the reference UI.
 * Colored circle icon above centered label text.
 */
@Composable
private fun CircularToolItem(
    tool: ToolItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular colored background with icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(tool.bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.label,
                tint = tool.iconColor,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tool label
        Text(
            text = tool.label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
