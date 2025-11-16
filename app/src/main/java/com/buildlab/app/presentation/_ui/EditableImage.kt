package com.buildlab.app.presentation._ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlin.math.roundToInt

data class EditableImageUiState(
    val id: String,
    val uri: Uri,
    var offset: Offset,
    val size: IntSize = IntSize(200, 200),
    var scale: Float = 1f,
    var rotation: Float = 0f
)

@Composable
fun EditableImage(
    image: EditableImageUiState,
    isSelected: Boolean,
    onDrag: (Offset) -> Unit,
    onTransform: (scale: Float, rotation: Float) -> Unit,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        image.offset.x.roundToInt(),
                        image.offset.y.roundToInt()
                    )
                }
                .size(image.size.width.dp, image.size.height.dp)
                .pointerInput(image.id) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        onSelect()
                        if (pan != Offset.Zero) {
                            onDrag(pan)
                        }
                        if (zoom != 1f || rotation != 0f) {
                            onTransform(zoom, rotation)
                        }
                    }
                }
        ) {
            Image(
                painter = rememberAsyncImagePainter(image.uri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = image.scale,
                        scaleY = image.scale,
                        rotationZ = image.rotation
                    ),
                contentScale = ContentScale.Fit
            )
        }
    }
}