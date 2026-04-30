package com.fastpdf.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fastpdf.data.tools.OcrEngine
import com.fastpdf.ui.components.FilePickerButton
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.AccentGreen
import com.fastpdf.ui.theme.AccentRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var extractedText by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            selectedUri = it; extractedText = null; errorMsg = null
            context.contentResolver.query(it, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) { val ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if (ni >= 0) selectedName = c.getString(ni) }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("OCR — Extract Text", fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Extract text from scanned PDFs or images using AI.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            FilePickerButton(label = "Select PDF or Image", selectedFileName = selectedName, onClick = { filePicker.launch(arrayOf("application/pdf", "image/*")) })

            if (isProcessing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = Primary)
                    Spacer(Modifier.height(8.dp)); Text("Extracting text...", style = MaterialTheme.typography.bodyMedium)
                }
            }

            extractedText?.let { text ->
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Extracted Text", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                            IconButton(onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("OCR Text", text))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }) { Icon(Icons.Filled.ContentCopy, "Copy", tint = Primary, modifier = Modifier.size(20.dp)) }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(text = if (text.length > 2000) text.take(2000) + "..." else text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        if (text.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("${text.split("\\s+".toRegex()).size} words extracted", style = MaterialTheme.typography.labelSmall, color = AccentGreen, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            errorMsg?.let { Text("❌ $it", color = AccentRed) }

            if (!isProcessing && extractedText == null && selectedUri != null) {
                val isPdf = selectedName?.endsWith(".pdf", ignoreCase = true) == true
                Button(onClick = {
                    isProcessing = true; errorMsg = null
                    scope.launch {
                        val text = withContext(Dispatchers.IO) { OcrEngine.extractText(context, selectedUri!!, isPdf) { progress = it } }
                        isProcessing = false
                        if (text != null) extractedText = text else errorMsg = "Failed to extract text"
                    }
                }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("Extract Text", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
