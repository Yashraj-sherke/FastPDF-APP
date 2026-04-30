package com.fastpdf.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastpdf.data.CurrentFile
import com.fastpdf.data.db.FileHistoryEntity
import com.fastpdf.data.db.FileRepository
import com.fastpdf.domain.model.DocumentType
import com.fastpdf.ui.theme.AccentRed
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.PrimaryLight

/**
 * Document Info Bottom Sheet Content — shows file metadata and quick actions.
 *
 * Phase 10: Replaces the "More" TODO button in ReaderScreen with a rich info sheet.
 */
@Composable
fun DocumentInfoSheet(
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val repository = remember { FileRepository(context) }

    val fileName = CurrentFile.name.ifEmpty { "Document" }
    val fileType = CurrentFile.type
    val mimeType = CurrentFile.mimeType
    val uri = CurrentFile.uri
    val sizeBytes = CurrentFile.sizeBytes

    // Get file entity for access count and dates
    val allFiles by repository.allFiles.collectAsState(initial = emptyList())
    val entity = remember(allFiles, uri) {
        val uriString = uri?.toString() ?: ""
        allFiles.find { it.uriString == uriString }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // ━━━ Header ━━━
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(fileType.tintColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = fileType.icon,
                    contentDescription = null,
                    tint = fileType.tintColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${fileType.displayName} • ${formatSize(sizeBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ━━━ File Details Grid ━━━
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoStatItem(
                value = formatSize(sizeBytes),
                label = "Size"
            )
            InfoStatItem(
                value = entity?.accessCount?.toString() ?: "1",
                label = "Opens"
            )
            InfoStatItem(
                value = fileType.displayName,
                label = "Type"
            )
            InfoStatItem(
                value = mimeType.substringAfter("/", "—").take(8),
                label = "Format"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ━━━ Metadata Rows ━━━
        if (entity != null) {
            InfoDetailRow(
                label = "Last Opened",
                value = formatTimestamp(entity.lastAccessedAt)
            )
            InfoDetailRow(
                label = "First Opened",
                value = formatTimestamp(entity.firstAccessedAt)
            )
            InfoDetailRow(
                label = "MIME Type",
                value = mimeType.ifEmpty { "Unknown" }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(8.dp))

        // ━━━ Quick Actions ━━━
        InfoActionRow(
            icon = Icons.Filled.Share,
            label = "Share",
            tint = Primary
        ) {
            uri?.let {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType.ifEmpty { "*/*" }
                    putExtra(Intent.EXTRA_STREAM, it)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share $fileName"))
            }
        }

        InfoActionRow(
            icon = Icons.Filled.OpenInNew,
            label = "Open With",
            tint = Primary
        ) {
            uri?.let {
                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(it, mimeType.ifEmpty { "*/*" })
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    context.startActivity(Intent.createChooser(openIntent, "Open with"))
                } catch (_: Exception) {}
            }
        }

        InfoActionRow(
            icon = Icons.Filled.ContentCopy,
            label = "Copy File Path",
            tint = Primary
        ) {
            val text = uri?.toString() ?: fileName
            clipboardManager.setText(AnnotatedString(text))
        }

        if (onDelete != null) {
            InfoActionRow(
                icon = Icons.Filled.Delete,
                label = "Move to Recycle Bin",
                tint = AccentRed
            ) {
                onDelete()
                onDismiss()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Sub-Components
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
private fun InfoStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Primary,
            maxLines = 1
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun InfoDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun InfoActionRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Utility Functions
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

private fun formatSize(bytes: Long): String = when {
    bytes <= 0 -> "—"
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    else -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
}

private fun formatTimestamp(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM d, yyyy · h:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}
