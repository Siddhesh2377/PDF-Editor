package com.example.pdfeditor.compose

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdfeditor.R
import com.example.pdfeditor.viewModel.FieldItem
import com.example.pdfeditor.viewModel.ItemType
import com.example.pdfeditor.viewModel.PdfEditorViewModel
import com.example.pdfeditor.viewModel.createRawPdfInDownloads
import com.example.pdfeditor.viewModel.writeRawPdf

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PdfEditorScreen(
    paddingValues: PaddingValues, vm: PdfEditorViewModel = viewModel()
) {
    val ctx = LocalContext.current

    Column(
        Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        Row(Modifier.padding(8.dp)) {
            Button(onClick = { vm.addTextField() }) {
                Text("Add Text Field")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.addImageField(R.drawable.sample) }) {
                Text("Add Image Field")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                createRawPdfInDownloads(ctx)?.let { out ->
                    writeRawPdf(ctx, out, vm.fields)
                    Toast.makeText(ctx, "Raw PDF saved!", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Save Raw PDF")
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFEFEFEF))
        ) {
            vm.fields.forEach { field ->
                when (field.type) {
                    ItemType.TEXT -> DraggableTextField(field, onMove = { x, y ->
                        vm.updateField(field.id, newX = x, newY = y)
                    }) { text ->
                        vm.updateField(field.id, newData = text)
                    }

                    ItemType.IMAGE -> DraggableImageField(field) { x, y ->
                        vm.updateField(field.id, newX = x, newY = y)
                    }
                }
            }
        }
    }
}

@Composable
fun DraggableTextField(
    field: FieldItem, onMove: (Float, Float) -> Unit, onTextChange: (String) -> Unit
) {
    TextField(
        value = field.data as String,
        onValueChange = onTextChange,
        textStyle = TextStyle(fontSize = 14.sp),
        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White),
        modifier = Modifier
            .offset { IntOffset(field.offsetX.toInt(), field.offsetY.toInt()) }
            .pointerInput(field.id) {
                detectDragGestures { change, drag ->
                    change.consume()
                    onMove(field.offsetX + drag.x, field.offsetY + drag.y)
                }
            }
            .background(Color.White)
            .padding(4.dp))
}

@Composable
fun DraggableImageField(
    field: FieldItem, onMove: (Float, Float) -> Unit
) {
    val resId = field.data as? Int ?: return
    Image(
        painter = painterResource(resId),
        contentDescription = null,
        modifier = Modifier
            .offset { IntOffset(field.offsetX.toInt(), field.offsetY.toInt()) }
            .pointerInput(field.id) {
                detectDragGestures { change, drag ->
                    change.consume()
                    onMove(field.offsetX + drag.x, field.offsetY + drag.y)
                }
            }
            .background(Color.White)
            .padding(8.dp))
}
