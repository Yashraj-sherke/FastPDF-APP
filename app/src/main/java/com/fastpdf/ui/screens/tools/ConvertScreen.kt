package com.fastpdf.ui.screens.tools

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fastpdf.data.tools.PdfToImageExporter
import com.fastpdf.ui.components.FilePickerButton
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.AccentGreen
import com.fastpdf.ui.theme.AccentRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var exportedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            selectedUri = it; exportedImages = emptyList(); errorMsg = null
            context.contentResolver.query(it, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) { val ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if (ni >= 0) selectedName = c.getString(ni) }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("PDF to Images", fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Convert PDF pages to high-resolution images.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            FilePickerButton(label = "Select PDF", selectedFileName = selectedName, onClick = { filePicker.launch(arrayOf("application/pdf")) })

            if (isProcessing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = Primary)
                    Spacer(Modifier.height(8.dp)); Text("Exporting pages...", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (exportedImages.isNotEmpty()) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.08f))) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅ Exported ${exportedImages.size} pages as images!", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                type = "image/jpeg"
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(exportedImages))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Images"))
                        }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary), modifier = Modifier.fillMaxWidth()) {
                            Text("Share All Images", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            errorMsg?.let { Text("❌ $it", color = AccentRed) }

            if (!isProcessing && exportedImages.isEmpty() && selectedUri != null) {
                Button(onClick = {
                    isProcessing = true; errorMsg = null
                    scope.launch {
                        val images = withContext(Dispatchers.IO) { PdfToImageExporter.export(context, selectedUri!!) { progress = it } }
                        isProcessing = false
                        if (images.isNotEmpty()) exportedImages = images else errorMsg = "Failed to export pages"
                    }
                }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("Convert to Images", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
