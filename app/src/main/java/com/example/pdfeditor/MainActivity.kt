package com.example.pdfeditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import com.example.pdfeditor.compose.PdfEditorScreen
import com.example.pdfeditor.ui.theme.PDFEditorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PDFEditorTheme {
                Scaffold { innerPadding ->
                    PdfEditorScreen(innerPadding)
                }
            }
        }
    }
}
