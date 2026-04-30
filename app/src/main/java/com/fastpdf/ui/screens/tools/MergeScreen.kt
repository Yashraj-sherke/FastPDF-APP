package com.fastpdf.ui.screens.tools

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fastpdf.data.tools.PdfMerger
import com.fastpdf.ui.components.ToolResultCard
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.AccentRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SelectedPdf(val uri: Uri, val name: String, val size: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedFiles = remember { mutableStateListOf<SelectedPdf>() }
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var resultUri by remember { mutableStateOf<Uri?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            var name = "PDF File"
            var size = ""
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val ni = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val si = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (ni >= 0) name = cursor.getString(ni) ?: name
                    if (si >= 0) {
                        val bytes = cursor.getLong(si)
                        size = if (bytes < 1024 * 1024) "${bytes / 1024} KB" else "%.1f MB".format(bytes / (1024.0 * 1024.0))
                    }
                }
            }
            selectedFiles.add(SelectedPdf(uri, name, size))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Merge PDFs", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Select 2 or more PDF files to merge into one document.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
            }

            itemsIndexed(selectedFiles) { index, file ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1}.", fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.width(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(file.name, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                            if (file.size.isNotEmpty()) Text(file.size, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { selectedFiles.removeAt(index) }) {
                            Icon(Icons.Filled.Delete, "Remove", tint = AccentRed, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { filePicker.launch(arrayOf("application/pdf")) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Add, null, tint = Primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Add PDF Files", color = Primary, fontWeight = FontWeight.SemiBold)
                }
            }

            if (isProcessing) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = Primary)
                        Spacer(Modifier.height(8.dp))
                        Text("Merging ${selectedFiles.size} files...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (resultUri != null) {
                item {
                    ToolResultCard(
                        isVisible = true,
                        message = "Merged ${selectedFiles.size} PDFs successfully!",
                        onOpen = { resultUri?.let { uri -> val i = Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }; context.startActivity(i) } },
                        onShare = { resultUri?.let { uri -> val i = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }; context.startActivity(Intent.createChooser(i, "Share")) } }
                    )
                }
            }

            if (errorMsg != null) {
                item { Text("❌ $errorMsg", color = AccentRed, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(8.dp)) }
            }

            if (!isProcessing && resultUri == null && selectedFiles.size >= 2) {
                item {
                    Button(
                        onClick = {
                            isProcessing = true; errorMsg = null
                            scope.launch {
                                val uri = withContext(Dispatchers.IO) { PdfMerger.merge(context, selectedFiles.map { it.uri }) { progress = it } }
                                isProcessing = false
                                if (uri != null) resultUri = uri else errorMsg = "Failed to merge PDFs"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) { Text("Merge ${selectedFiles.size} PDFs", fontWeight = FontWeight.SemiBold) }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
