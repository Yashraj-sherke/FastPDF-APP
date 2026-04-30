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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.fastpdf.data.tools.PdfProtector
import com.fastpdf.ui.components.FilePickerButton
import com.fastpdf.ui.components.ToolResultCard
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.AccentRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var allowPrinting by remember { mutableStateOf(true) }
    var allowCopying by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var resultUri by remember { mutableStateOf<Uri?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            selectedUri = it; resultUri = null; errorMsg = null
            context.contentResolver.query(it, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) { val ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if (ni >= 0) selectedName = c.getString(ni) }
            }
        }
    }

    val passwordsMatch = password.isNotEmpty() && password == confirmPassword

    Scaffold(
        topBar = { TopAppBar(title = { Text("Password Protect", fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Encrypt a PDF with a password to protect its content.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            FilePickerButton(label = "Select PDF", selectedFileName = selectedName, onClick = { filePicker.launch(arrayOf("application/pdf")) })

            if (selectedUri != null) {
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                    isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                    supportingText = if (confirmPassword.isNotEmpty() && !passwordsMatch) {{ Text("Passwords don't match") }} else null)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Allow printing", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = allowPrinting, onCheckedChange = { allowPrinting = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Allow copying text", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = allowCopying, onCheckedChange = { allowCopying = it })
                }
            }

            if (isProcessing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { CircularProgressIndicator(color = Primary); Spacer(Modifier.height(8.dp)); Text("Encrypting...") }
            }

            resultUri?.let { uri ->
                ToolResultCard(isVisible = true, message = "PDF encrypted successfully!",
                    onOpen = { context.startActivity(Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }) },
                    onShare = { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Share")) })
            }

            errorMsg?.let { Text("❌ $it", color = AccentRed) }

            if (!isProcessing && resultUri == null && selectedUri != null && passwordsMatch) {
                Button(onClick = {
                    isProcessing = true; errorMsg = null
                    scope.launch {
                        val uri = withContext(Dispatchers.IO) { PdfProtector.protect(context, selectedUri!!, password, allowPrinting, allowCopying) }
                        isProcessing = false
                        if (uri != null) resultUri = uri else errorMsg = "Failed to encrypt PDF"
                    }
                }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("Protect PDF", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
