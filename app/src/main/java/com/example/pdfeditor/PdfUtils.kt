package com.example.pdfeditor

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.text.PDFTextStripper

fun extractTextFromPdf(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val document = PDDocument.load(inputStream)
        val stripper = PDFTextStripper()
        val text = stripper.getText(document)
        document.close()
        text
    } catch (e: Exception) {
        e.printStackTrace()
        "Error loading PDF"
    }
}

fun saveEditedPdfToUri(context: Context, uri: Uri, text: String) {
    try {
        val document = PDDocument()
        val page = PDPage(PDRectangle.LETTER)
        document.addPage(page)

        val contentStream = PDPageContentStream(document, page)
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA, 12f)
        contentStream.setLeading(14.5f)
        contentStream.newLineAtOffset(40f, 750f)

        text.split("\n").forEach {
            contentStream.showText(it)
            contentStream.newLine()
        }

        contentStream.endText()
        contentStream.close()

        context.contentResolver.openOutputStream(uri)?.use { output ->
            document.save(output)
        }

        document.close()
        Toast.makeText(context, "PDF saved successfully!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
