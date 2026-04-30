package com.fastpdf.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastpdf.data.ai.AiService
import com.fastpdf.data.ai.TextExtractor
import com.fastpdf.ui.theme.GradientEnd
import com.fastpdf.ui.theme.GradientStart
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.PrimaryLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * AI message in the chat.
 */
data class AiChatMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false
)

/**
 * AI Chat Bottom Sheet for ReaderScreen.
 *
 * Allows users to ask questions about the currently open document.
 * Uses Gemini AI for intelligent Q&A based on document content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var userInput by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<AiChatMessage>() }
    var documentText by remember { mutableStateOf("") }

    // Extract text from current file on first composition
    LaunchedEffect(Unit) {
        documentText = withContext(Dispatchers.IO) {
            TextExtractor.extractFromCurrentFile(context)
        }
        // Add welcome message
        messages.add(AiChatMessage(
            text = "👋 Hi! I'm your AI assistant. Ask me anything about this document — I can summarize, explain, or find specific info.",
            isUser = false
        ))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp, max = 600.dp)
            .padding(bottom = 8.dp)
    ) {
        // ━━━ Header ━━━
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, null, tint = Primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ask AI", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, "Close", modifier = Modifier.size(22.dp))
            }
        }

        HorizontalDivider()

        // ━━━ API Key Warning ━━━
        if (!AiService.isInitialized) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryLight)
                    .padding(16.dp)
            ) {
                Text(
                    "⚠️ AI not configured. Go to AI Assistant (Home → AI Summary card) to set up your free Gemini API key.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // ━━━ Chat Messages ━━━
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            messages.forEach { msg ->
                ChatBubble(message = msg)
            }
        }

        // Auto-scroll to bottom on new message
        LaunchedEffect(messages.size) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }

        // ━━━ Input Bar ━━━
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about this document...") },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            FilledIconButton(
                onClick = {
                    if (userInput.isNotBlank() && AiService.isInitialized) {
                        val question = userInput.trim()
                        userInput = ""
                        messages.add(AiChatMessage(text = question, isUser = true))
                        messages.add(AiChatMessage(text = "Thinking...", isUser = false, isLoading = true))

                        scope.launch {
                            val answer = withContext(Dispatchers.IO) {
                                AiService.askQuestion(documentText, question)
                            }
                            // Remove loading message and add real response
                            val loadingIndex = messages.indexOfLast { it.isLoading }
                            if (loadingIndex >= 0) messages.removeAt(loadingIndex)
                            messages.add(AiChatMessage(text = answer, isUser = false))
                        }
                    }
                },
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Primary),
                enabled = userInput.isNotBlank() && AiService.isInitialized
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White)
            }
        }

        // Quick suggestions
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Summarize", "Key points", "Explain simply").forEach { suggestion ->
                SuggestionChip(
                    onClick = {
                        userInput = suggestion
                    },
                    label = { Text(suggestion, fontSize = 11.sp) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: AiChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (message.isUser) Primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            if (message.isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Thinking...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }

        if (message.isUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Person, null, tint = Primary, modifier = Modifier.size(14.dp)) }
        }
    }
}
