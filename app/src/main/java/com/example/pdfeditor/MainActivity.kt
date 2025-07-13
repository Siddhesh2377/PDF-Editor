package com.example.pdfeditor

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.Scaffold
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.pdfeditor.compose.GeneratePdfScreen
import com.example.pdfeditor.compose.PDFReaderScreen
import com.example.pdfeditor.compose.PdfEditorScreen
import com.example.pdfeditor.ui.theme.PDFEditorTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        setContent {
            PDFEditorTheme {
                Scaffold { innerPadding ->
                    GeneratePdfScreen(innerPadding)
                }
            }
        }
    }
}
