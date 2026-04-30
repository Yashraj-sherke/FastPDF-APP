package com.fastpdf.ui.screens

import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastpdf.data.ai.AiService
import com.fastpdf.data.ai.TextExtractor
import com.fastpdf.ui.components.FilePickerButton
import com.fastpdf.ui.theme.GradientEnd
import com.fastpdf.ui.theme.GradientStart
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.AccentRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * AI Summary Screen — Summarize any document using Gemini AI.
 *
 * Features:
 * - Pick any text/PDF file
 * - Generate AI summary
 * - Extract key points
 * - Copy results to clipboard
 * - API key setup prompt if not configured
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSummaryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var selectedName by remember { mutableStateOf<String?>(null) }
    var documentText by remember { mutableStateOf("") }
    var summaryResult by remember { mutableStateOf("") }
    var keyPointsResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) } // 0=Summary, 1=Key Points
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var apiKeyInput by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            var mimeType = context.contentResolver.getType(it) ?: ""
            context.contentResolver.query(it, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) { val ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if (ni >= 0) selectedName = c.getString(ni) }
            }
            // Extract text in background
            scope.launch {
                documentText = withContext(Dispatchers.IO) { TextExtractor.extractFromUri(context, it, mimeType) }
            }
            summaryResult = ""; keyPointsResult = ""; errorMsg = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("AI Assistant", fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ━━━ Hero Banner ━━━
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                    .padding(20.dp)
            ) {
                Column {
                    Text("✨ AI Document Intelligence", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Summarize, extract key points, and understand any document instantly.", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, lineHeight = 18.sp)
                }
            }

            // ━━━ API Key Setup (if not initialized) ━━━
            if (!AiService.isInitialized) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🔑 Setup Required", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text("Enter your free Gemini API key from aistudio.google.com", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = apiKeyInput, onValueChange = { apiKeyInput = it },
                            label = { Text("Gemini API Key") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { if (apiKeyInput.isNotBlank()) AiService.initialize(apiKeyInput.trim()) },
                            shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.fillMaxWidth(), enabled = apiKeyInput.isNotBlank()
                        ) { Text("Activate AI", fontWeight = FontWeight.SemiBold) }
                    }
                }
            }

            // ━━━ File Picker ━━━
            FilePickerButton(
                label = "Select Document",
                selectedFileName = selectedName,
                selectedFileSize = if (documentText.isNotEmpty()) "${documentText.split("\\s+".toRegex()).size} words extracted" else null,
                onClick = { filePicker.launch(arrayOf("text/*", "application/pdf")) }
            )

            // ━━━ Action Tabs ━━━
            if (documentText.isNotEmpty() && AiService.isInitialized) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = activeTab == 0, onClick = { activeTab = 0 },
                        label = { Text("Summary") },
                        leadingIcon = { Icon(Icons.Filled.Summarize, null, Modifier.size(16.dp)) })
                    FilterChip(selected = activeTab == 1, onClick = { activeTab = 1 },
                        label = { Text("Key Points") },
                        leadingIcon = { Icon(Icons.Filled.FormatListBulleted, null, Modifier.size(16.dp)) })
                }

                // Generate button
                val currentResult = if (activeTab == 0) summaryResult else keyPointsResult
                if (currentResult.isEmpty() && !isLoading) {
                    Button(
                        onClick = {
                            isLoading = true; errorMsg = null
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    if (activeTab == 0) AiService.summarize(documentText, selectedName ?: "")
                                    else AiService.extractKeyPoints(documentText)
                                }
                                isLoading = false
                                if (result.startsWith("❌")) errorMsg = result
                                else { if (activeTab == 0) summaryResult = result else keyPointsResult = result }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (activeTab == 0) "Generate Summary" else "Extract Key Points", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ━━━ Loading ━━━
            if (isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(Modifier.height(12.dp))
                    Text("AI is analyzing your document...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ━━━ Results ━━━
            val displayResult = if (activeTab == 0) summaryResult else keyPointsResult
            AnimatedVisibility(visible = displayResult.isNotEmpty(), enter = fadeIn()) {
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(if (activeTab == 0) "📝 Summary" else "📋 Key Points", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(displayResult)) }) {
                                Icon(Icons.Filled.ContentCopy, "Copy", tint = Primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(displayResult, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            errorMsg?.let { Text(it, color = AccentRed, style = MaterialTheme.typography.bodyMedium) }

            Spacer(Modifier.height(32.dp))
        }
    }
}
