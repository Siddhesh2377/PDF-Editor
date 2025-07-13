package com.example.pdfeditor.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bhuvaneshw.pdf.compose.PdfViewer
import com.bhuvaneshw.pdf.compose.rememberPdfState
import com.bhuvaneshw.pdf.compose.ui.PdfScrollBar
import com.bhuvaneshw.pdf.compose.ui.PdfToolBar
import com.bhuvaneshw.pdf.compose.ui.PdfViewerContainer

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PDFReaderScreen(paddingValues: PaddingValues) {
    val pdfState = rememberPdfState("asset://sample_pdf.pdf")

    PdfViewerContainer(
        modifier = Modifier.padding(paddingValues),
        pdfState = pdfState,
        loadingIndicator = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                LoadingIndicator()
            }
        },
        pdfViewer = {
            PdfViewer(
                pdfState = pdfState,
                modifier = Modifier
            )
        },
        pdfToolBar = {
            PdfToolBar(
                title = "Title",
            )
        },
        pdfScrollBar = { parentSize ->
            PdfScrollBar(
                parentSize = parentSize
            )
        },
    )
}