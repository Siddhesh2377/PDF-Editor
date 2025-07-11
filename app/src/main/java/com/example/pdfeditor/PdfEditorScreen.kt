package com.example.pdfeditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast

@Composable
fun PdfEditorScreen() {
    val context = LocalContext.current
    var pdfText by remember { mutableStateOf("") }
    var isPdfLoaded by remember { mutableStateOf(false) }


    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    try {

                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )

                        pdfText = extractTextFromPdf(context, uri)
                        isPdfLoaded = true
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                        Toast.makeText(context, "Permission denied for PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )

    // Launcher to save the edited PDF
    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri: Uri? ->
            if (uri != null) {
                saveEditedPdfToUri(context, uri, pdfText)
            }
        }
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {

            val openPdfIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            }
            pdfPickerLauncher.launch(openPdfIntent)
        }) {
            Text("Upload PDF for Edit")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isPdfLoaded) {
            Text("Edit PDF Text:")
            OutlinedTextField(
                value = pdfText,
                onValueChange = { pdfText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                maxLines = 20
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                savePdfLauncher.launch("EditedPDF_${System.currentTimeMillis()}.pdf")
            }) {
                Text("Save Edited PDF")
            }
        }
    }
}
