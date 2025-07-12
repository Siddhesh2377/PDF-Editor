package com.example.pdfeditor.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStream

class PDFEditorScreenViewModel : ViewModel() {

    private val _pdfText = MutableStateFlow("")
    val pdfText: StateFlow<String> get() = _pdfText

    private val _isPdfLoaded = MutableStateFlow(false)
    val isPdfLoaded: StateFlow<Boolean> get() = _isPdfLoaded

    fun loadPdfFromAssets(context: Context, fileName: String, onResult: (InputStream) -> Unit){
        viewModelScope.launch {
            try {
                val inputStream = context.assets.open(fileName)
                onResult(inputStream)
                _isPdfLoaded.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
