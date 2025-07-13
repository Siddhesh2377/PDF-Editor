    package com.example.pdfeditor.pdfEngine

    import android.annotation.SuppressLint
    import android.util.Log
    import androidx.compose.foundation.background
    import androidx.compose.foundation.gestures.rememberTransformableState
    import androidx.compose.foundation.gestures.transformable
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.BoxWithConstraints
    import androidx.compose.foundation.layout.PaddingValues
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.offset
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.layout.widthIn
    import androidx.compose.foundation.text.selection.SelectionContainer
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableFloatStateOf
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clipToBounds
    import androidx.compose.ui.geometry.Offset
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.TransformOrigin
    import androidx.compose.ui.graphics.graphicsLayer
    import androidx.compose.ui.platform.LocalDensity
    import androidx.compose.ui.text.SpanStyle
    import androidx.compose.ui.text.buildAnnotatedString
    import androidx.compose.ui.text.font.FontFamily
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import kotlin.math.max
    import kotlin.math.min

    data class PDFText(
        val isNewLine: Boolean,
        val text: String, val x: Float, val y: Float, val fontPt: Float, val font: String
    )

    class PdfContentInterpreter {
        fun interpret(content: String): List<Pair<String, Any>> {
            val items = mutableListOf<Pair<String, Any>>()
            val lines = content.lines()

            // 🔥 Global state carried forward
            var currentFontSize = 12f
            var currentFont = "F1"
            var currentWordSpacing: Float = 0f

            lines.forEach { line ->
                var x = 0f
                var y = 0f
                var text = ""

                // 🔁 Update font globally (if present in this line)
                Regex("""/F(\d+)\s+(\d+(?:\.\d+)?)\s+Tf""").find(line)?.let { match ->
                    currentFont = "F${match.groupValues[1]}"
                    currentFontSize = match.groupValues[2].toFloat()
                }

                // 🔁 Word spacing
                Regex("""(-?\d+(?:\.\d+)?)\s+Tw""").find(line)?.groupValues?.get(1)?.toFloatOrNull()?.let {
                    currentWordSpacing = it
                    items += "WSP" to currentWordSpacing
                }

                if (line.startsWith("BT")) {

                    // 🔁 Position
                    Regex("""(-?\d+(?:\.\d+)?)\s+(-?\d+(?:\.\d+)?)\s+Td""").find(line)?.let { match ->
                        x = match.groupValues[1].toFloat()
                        y = match.groupValues[2].toFloat()
                    }

                    // 🔁 Text
                    Regex("""\((.*?)\)\s*Tj""").find(line)?.let { match ->
                        text = match.groupValues[1]
                    }

                    items += "BT" to PDFText(text.isEmpty(), text, x, y, currentFontSize, currentFont)
                }
            }

            return items
        }
    }


    @SuppressLint("UnusedBoxWithConstraintsScope")
    @Composable
    fun PdfPageView(
        paras: List<Pair<String, Any>>,
        paddingValues: PaddingValues
    ) {
        val pdfW = 595.28f  // A4 width in points
        val pdfH = 841.89f

        val density = LocalDensity.current
        val zoomFactor = remember { mutableStateOf(1.0f) }

        // Gesture detector for pinch zoom
        val scaleState = rememberTransformableState { zoomChange, _, _ ->
            val newZoom = (zoomFactor.value * zoomChange).coerceIn(0.5f, 5f)
            zoomFactor.value = newZoom
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            val boxWidthPx = with(density) { maxWidth.toPx() }
            val boxHeightPx = with(density) { maxHeight.toPx() }

            val fitScale = max(boxWidthPx / pdfW, boxHeightPx / pdfH)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(state = scaleState) // gesture handler
                    .clipToBounds()
            ) {
                SelectionContainer {
                    var currentWordSpacing = 0f

                    paras.forEach { para ->
                        when (para.first) {
                            "BT" -> {
                                val pdfText = para.second as PDFText

                                if (pdfText.isNewLine) return@forEach

                                val xDp = with(density) { (pdfText.x * fitScale * zoomFactor.value).toDp() }
                                val yDp = with(density) { ((pdfH - pdfText.y) * fitScale * zoomFactor.value).toDp() }

                                val scaledFontSp = (pdfText.fontPt * 1.933f * zoomFactor.value).sp

                                // Fixed max width container (soft-wrap)
                                Box(
                                    modifier = Modifier
                                        .offset(x = xDp, y = yDp)
                                        .widthIn(max = with(density) { (pdfW * fitScale).toDp() }) // Fixed width
                                ) {
                                    Text(
                                        text = pdfText.text,
                                        color = Color.White,
                                        fontSize = scaledFontSp,
                                        softWrap = true,
                                        lineHeight = scaledFontSp * 1.2
                                    )
                                }
                            }

                            "WSP" -> {
                                currentWordSpacing = para.second as Float
                            }
                        }
                    }
                }
            }
        }
    }






