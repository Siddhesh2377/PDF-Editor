package com.example.pdfeditor.compose

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdfeditor.pdfEngine.PdfContentInterpreter
import com.example.pdfeditor.pdfEngine.PdfPageView
import com.example.pdfeditor.pdfEngine.PdfTokenizer
import com.example.pdfeditor.viewModel.PDFEditorScreenViewModel

@Composable
fun PdfEditorScreen(innerPaddingValues: PaddingValues, viewModel: PDFEditorScreenViewModel = viewModel()) {
    val context = LocalContext.current

    // hold your paragraphs
    var paras by remember { mutableStateOf<List<Pair<String, Any>>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.loadPdfFromAssets(context, "sample_one_page.pdf") { input ->
            // read *all* streams into one big string
            val sb = StringBuilder()
            val tokenizer = PdfTokenizer(input)
            while (true) {
                val tok = tokenizer.nextToken() ?: break
                if (tok == "stream") {
                    // adjust length if needed to read the full stream
                    val raw = tokenizer.readStreamBytes(338)
                    sb.append(tokenizer.decompressFlateData(raw)).append("\n")
                }
            }

            paras = PdfContentInterpreter().interpret(sb.toString())
            Log.d("PDF", "Got ${paras.size} paragraphs:")

            Log.d("PDF-RAW", sb.toString())
        }
    }

    if (paras.isEmpty()) {
        Box(
            Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Text("Loading PDF…", color = Color.Black)
        }
    } else {
        PdfPageView(paras, innerPaddingValues)
    }
}

