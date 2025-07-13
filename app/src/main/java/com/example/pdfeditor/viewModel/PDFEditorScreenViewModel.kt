package com.example.pdfeditor.viewModel

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import java.io.OutputStream

// Data model for draggable items

data class FieldItem(
    val id: Int,
    val type: ItemType,
    val data: Any,
    val offsetX: Float,
    val offsetY: Float
)

enum class ItemType { TEXT, IMAGE }

class PdfEditorViewModel : ViewModel() {
    private val _fields = mutableStateListOf<FieldItem>()
    val fields: List<FieldItem> get() = _fields

    init {
        // One initial text field
        _fields.add(FieldItem(0, ItemType.TEXT, "Hello, World!", 100f, 100f))
    }

    fun addTextField() {
        _fields.add(
            FieldItem(
                id = _fields.size,
                type = ItemType.TEXT,
                data = "New Text",
                offsetX = 100f,
                offsetY = 100f
            )
        )
    }

    fun addImageField(resId: Int) {
        _fields.add(
            FieldItem(
                id = _fields.size,
                type = ItemType.IMAGE,
                data = resId,
                offsetX = 100f,
                offsetY = 200f
            )
        )
    }

    fun updateField(
        id: Int,
        newData: Any? = null,
        newX: Float? = null,
        newY: Float? = null
    ) {
        _fields.replaceAll { field ->
            if (field.id == id) field.copy(
                data = newData ?: field.data,
                offsetX = newX ?: field.offsetX,
                offsetY = newY ?: field.offsetY
            ) else field
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun createRawPdfInDownloads(
    context: Context,
    fileName: String = "raw_output.pdf"
): OutputStream? {
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
        put(MediaStore.Downloads.IS_PENDING, 1)
    }
    val uri = resolver.insert(
        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        values
    ) ?: return null
    // Open stream
    val out = resolver.openOutputStream(uri)
    // Mark complete
    values.clear()
    values.put(MediaStore.Downloads.IS_PENDING, 0)
    resolver.update(uri, values, null, null)
    return out
}

/**
 * Writes a valid raw PDF with a single page and multiple text objects, including xref table.
 */
fun writeRawPdf(
    context: Context,
    outputStream: OutputStream,
    fields: List<FieldItem>
) {
    // Build content stream text for all TEXT fields
    val contentSb = StringBuilder()
    contentSb.append("BT\n/F1 12 Tf\n")
    fields.filter { it.type == ItemType.TEXT }.forEach { field ->
        val text = (field.data as? String)?.replace("(", "\\(")?.replace(")", "\\)") ?: ""
        contentSb.append("${field.offsetX.toInt()} ${field.offsetY.toInt()} Td ($text) Tj\n")
    }
    contentSb.append("ET")
    val content = contentSb.toString()

    // Prepare PDF sections
    val sections = mutableListOf<String>()
    sections.add("%PDF-1.4\n")
    sections.add(
        "1 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n"
    )
    sections.add(
        "2 0 obj\n<< /Type /Pages /Count 1 /Kids [3 0 R] >>\nendobj\n"
    )
    sections.add(
        "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 1 0 R >> >> >>\nendobj\n"
    )
    // Content stream object (object 4)
    sections.add(
        buildString {
            append("4 0 obj\n<< /Length ${content.toByteArray(Charsets.US_ASCII).size} >>\nstream\n")
            append(content)
            append("\nendstream\nendobj\n")
        }
    )

    // Build the PDF and record offsets
    val sb = StringBuilder()
    val offsets = mutableListOf<Int>()
    sections.forEach { sec ->
        offsets.add(sb.length)
        sb.append(sec)
    }

    // xref and trailer
    val xrefPos = sb.length
    val objCount = sections.size
    sb.append("xref\n0 ${objCount + 1}\n")
    sb.append("0000000000 65535 f \n")
    offsets.forEach { off ->
        sb.append(String.format("%010d 00000 n \n", off))
    }
    sb.append(
        "trailer << /Size ${objCount + 1} /Root 2 0 R >>\nstartxref\n$xrefPos\n%%EOF"
    )

    // Write to output
    outputStream.use {
        it.write(sb.toString().toByteArray(Charsets.US_ASCII))
    }
}
