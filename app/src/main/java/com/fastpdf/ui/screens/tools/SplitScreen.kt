package com.fastpdf.ui.screens.tools

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fastpdf.data.tools.PdfSplitter
import com.fastpdf.ui.components.FilePickerButton
import com.fastpdf.ui.components.ToolResultCard
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.AccentRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var selectedSize by remember { mutableStateOf<String?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var pageRange by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var resultUri by remember { mutableStateOf<Uri?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            selectedUri = it
            context.contentResolver.query(it, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) { val ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if (ni >= 0) selectedName = c.getString(ni) }
            }
            scope.launch { pageCount = withContext(Dispatchers.IO) { PdfSplitter.getPageCount(context, it) } }
            resultUri = null; errorMsg = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Split PDF", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent))
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Extract specific pages from a PDF file.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            FilePickerButton(label = "Select PDF", selectedFileName = selectedName, selectedFileSize = if (pageCount > 0) "$pageCount pages" else null, onClick = { filePicker.launch(arrayOf("application/pdf")) })

            if (pageCount > 0) {
                OutlinedTextField(
                    value = pageRange, onValueChange = { pageRange = it },
                    label = { Text("Page ranges") },
                    placeholder = { Text("e.g., 1-3, 5, 7-10") },
                    supportingText = { Text("Total pages: $pageCount") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            if (isProcessing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(Modifier.height(8.dp))
                    Text("Splitting PDF...", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (resultUri != null) {
                ToolResultCard(isVisible = true, message = "PDF split successfully!",
                    onOpen = { resultUri?.let { uri -> context.startActivity(Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }) } },
                    onShare = { resultUri?.let { uri -> context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Share")) } })
            }

            errorMsg?.let { Text("❌ $it", color = AccentRed, style = MaterialTheme.typography.bodyMedium) }

            if (!isProcessing && resultUri == null && selectedUri != null && pageRange.isNotBlank()) {
                Button(onClick = {
                    isProcessing = true; errorMsg = null
                    scope.launch {
                        val uri = withContext(Dispatchers.IO) { PdfSplitter.split(context, selectedUri!!, pageRange) }
                        isProcessing = false
                        if (uri != null) resultUri = uri else errorMsg = "Failed to split PDF. Check page ranges."
                    }
                }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("Split PDF", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
