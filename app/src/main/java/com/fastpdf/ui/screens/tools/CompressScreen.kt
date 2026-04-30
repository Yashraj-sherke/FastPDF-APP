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
import com.fastpdf.data.tools.PdfCompressor
import com.fastpdf.ui.components.FilePickerButton
import com.fastpdf.ui.components.ToolResultCard
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.AccentRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var selectedSize by remember { mutableStateOf<String?>(null) }
    var originalBytes by remember { mutableLongStateOf(0L) }
    var isProcessing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<PdfCompressor.CompressionResult?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            selectedUri = it; result = null; errorMsg = null
            context.contentResolver.query(it, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME); val si = c.getColumnIndex(OpenableColumns.SIZE)
                    if (ni >= 0) selectedName = c.getString(ni)
                    if (si >= 0) { originalBytes = c.getLong(si); selectedSize = if (originalBytes < 1024 * 1024) "${originalBytes / 1024} KB" else "%.1f MB".format(originalBytes / (1024.0 * 1024.0)) }
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Compress PDF", fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Reduce PDF file size while maintaining quality.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            FilePickerButton(label = "Select PDF", selectedFileName = selectedName, selectedFileSize = selectedSize, onClick = { filePicker.launch(arrayOf("application/pdf")) })

            if (isProcessing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(Modifier.height(8.dp))
                    Text("Compressing...", style = MaterialTheme.typography.bodyMedium)
                }
            }

            result?.let { r ->
                ToolResultCard(isVisible = true, message = "Compressed ${r.savedPercent}% smaller!",
                    originalSizeBytes = r.originalBytes, outputSizeBytes = r.compressedBytes,
                    onOpen = { context.startActivity(Intent(Intent.ACTION_VIEW).apply { setDataAndType(r.outputUri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }) },
                    onShare = { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, r.outputUri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Share")) })
            }

            errorMsg?.let { Text("❌ $it", color = AccentRed, style = MaterialTheme.typography.bodyMedium) }

            if (!isProcessing && result == null && selectedUri != null) {
                Button(onClick = {
                    isProcessing = true; errorMsg = null
                    scope.launch {
                        val r = withContext(Dispatchers.IO) { PdfCompressor.compress(context, selectedUri!!) }
                        isProcessing = false
                        if (r != null) result = r else errorMsg = "Failed to compress PDF"
                    }
                }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("Compress PDF", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
