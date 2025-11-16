package com.buildlab.app.presentation._ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.IntSize

class PrintAreaBounds(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

@Composable
fun PrintAreaView(
    modifier: Modifier = Modifier,
    shirtColor: Color,
    isFrontView: Boolean = true,
    images: List<EditableImageUiState>,
    selectedImageId: String?,
    onDragImage: (String, Offset) -> Unit,
    onTransformImage: (String, Float, Float) -> Unit,
    onSelectImage: (String) -> Unit,
    onSavePrintArea: ((suspend () -> Bitmap?) -> Unit)? = null
) {
    var printAreaBounds by remember { mutableStateOf<PrintAreaBounds?>(null) }
    val printAreaGraphicsLayer = rememberGraphicsLayer()

    Box(modifier = modifier) {
        ShirtFrame(
            modifier = Modifier.fillMaxSize(),
            shirtColor = shirtColor,
            isFrontView = isFrontView
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val printAreaWidth = size.width * 0.50f
            val printAreaHeight = size.width * 0.50f
            val printAreaLeft = (size.width - printAreaWidth) / 2f
            val printAreaTop = (size.height - printAreaHeight) / 2f

            printAreaBounds = PrintAreaBounds(
                left = printAreaLeft,
                top = printAreaTop,
                width = printAreaWidth,
                height = printAreaHeight
            )

            drawRect(
                color = Color.Black.copy(alpha = 0.1f),
                topLeft = Offset(printAreaLeft, printAreaTop),
                size = Size(printAreaWidth, printAreaHeight),
                style = Stroke(width = 3f)
            )
        }

        printAreaBounds?.let { bounds ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        printAreaGraphicsLayer.record(
                            size = IntSize(bounds.width.toInt(), bounds.height.toInt())
                        ) {
                            clipRect(
                                left = 0f,
                                top = 0f,
                                right = bounds.width,
                                bottom = bounds.height
                            ) {
                                translate(-bounds.left, -bounds.top) {
                                    this@drawWithContent.drawContent()
                                }
                            }
                        }

                        clipRect(
                            left = bounds.left,
                            top = bounds.top,
                            right = bounds.left + bounds.width,
                            bottom = bounds.top + bounds.height
                        ) {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                images.forEach { image ->
                    EditableImage(
                        image = image,
                        isSelected = image.id == selectedImageId,
                        onDrag = { delta -> onDragImage(image.id, delta) },
                        onTransform = { scale, rotation ->
                            onTransformImage(
                                image.id,
                                scale,
                                rotation
                            )
                        },
                        onSelect = { onSelectImage(image.id) }
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        onSavePrintArea?.invoke {
            try {
                printAreaGraphicsLayer.toImageBitmap().asAndroidBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }
}