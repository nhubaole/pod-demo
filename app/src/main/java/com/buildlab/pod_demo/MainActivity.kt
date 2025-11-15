package com.buildlab.pod_demo

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.buildlab.pod_demo.ui.theme.PoddemoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.min
import kotlin.math.roundToInt

data class DraggableImage(
    val id: String,
    val uri: Uri,
    var offset: Offset,
    val size: IntSize = IntSize(200, 200),
    var scale: Float = 1f,
    var rotation: Float = 0f
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PoddemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShirtColorizerScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ShirtColorizerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var shirtColor by remember { mutableStateOf(Color(0xFF4CAF50)) } // Default green color
    var isFrontView by remember { mutableStateOf(true) } // Toggle between front and back
    var images by remember { mutableStateOf(listOf<DraggableImage>()) }
    var selectedImageId by remember { mutableStateOf<String?>(null) }
    var capturePrintArea: (suspend () -> Bitmap?)? by remember { mutableStateOf(null) }

    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val newImage = DraggableImage(
                id = System.currentTimeMillis().toString(),
                uri = it,
                offset = Offset(0f, 0f) // Will be centered on the print area
            )
            images = images + newImage
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            permissionLauncher.launch(permission)
        }
    }

    // Predefined color palette
    val colorPalette = listOf(
        Color(0xFFFFFFFF), // White
        Color(0xFF000000), // Black
        Color(0xFF3D3D3D), // Grey
        Color(0xFFFF5252), // Red
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFFC107), // Amber
        Color(0xFF9C27B0), // Purple
        Color(0xFFFF9800), // Orange
        Color(0xFF00BCD4), // White
        Color(0xFFE91E63), // Pink
        Color(0xFF795548), // Brown
        Color(0xFF607D8B), // Blue Grey
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Print On Demand",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    // Action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Add image button
                        FloatingActionButton(
                            onClick = {
                                if (hasPermission) {
                                    imagePickerLauncher.launch("image/*")
                                } else {
                                    val permission =
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            Manifest.permission.READ_MEDIA_IMAGES
                                        } else {
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        }
                                    permissionLauncher.launch(permission)
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Image",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Save print area button
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val bitmap = capturePrintArea?.invoke()
                                        if (bitmap != null) {
                                            saveImageToGallery(context, bitmap)
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Print area saved to gallery!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to capture print area",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Failed to save: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Save Print Area",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Delete selected image button
                        if (selectedImageId != null) {
                            FloatingActionButton(
                                onClick = {
                                    images = images.filter { it.id != selectedImageId }
                                    selectedImageId = null
                                },
                                containerColor = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Image",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // View toggle buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isFrontView = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFrontView) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Front View")
                    }

                    Button(
                        onClick = { isFrontView = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isFrontView) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Back View")
                    }
                }
            }
        }

        // Shirt display with print area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            ShirtWithPrintArea(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shirtColor = shirtColor,
                isFrontView = isFrontView,
                images = images,
                selectedImageId = selectedImageId,
                onDragImage = { imageId, delta ->
                    images = images.map {
                        if (it.id == imageId) {
                            it.copy(
                                offset = Offset(
                                    it.offset.x + delta.x,
                                    it.offset.y + delta.y
                                )
                            )
                        } else {
                            it
                        }
                    }
                },
                onTransformImage = { imageId, scale, rotation ->
                    images = images.map {
                        if (it.id == imageId) {
                            it.copy(scale = it.scale * scale, rotation = it.rotation + rotation)
                        } else {
                            it
                        }
                    }
                },
                onSelectImage = { imageId ->
                    selectedImageId = imageId
                },
                onSavePrintArea = { callback ->
                    capturePrintArea = callback
                }
            )
        }

        // Color palette
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Shirt Color",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Color grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colorPalette.forEach { color ->
                        ColorSwatch(
                            color = color,
                            isSelected = shirtColor == color,
                            onClick = { shirtColor = color }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShirtWithPrintArea(
    modifier: Modifier = Modifier,
    shirtColor: Color,
    isFrontView: Boolean = true,
    images: List<DraggableImage>,
    selectedImageId: String?,
    onDragImage: (String, Offset) -> Unit,
    onTransformImage: (String, Float, Float) -> Unit,
    onSelectImage: (String) -> Unit,
    onSavePrintArea: ((suspend () -> Bitmap?) -> Unit)? = null
) {
    var printAreaBounds by remember { mutableStateOf<PrintAreaBounds?>(null) }
    val printAreaGraphicsLayer = rememberGraphicsLayer()

    Box(modifier = modifier) {
        // Layer 1: Shirt with color
        ShirtColorizer(
            modifier = Modifier.fillMaxSize(),
            shirtColor = shirtColor,
            isFrontView = isFrontView
        )

        // Layer 2: Print area rectangle (centered on shirt)
        // Print area is approximately 30% width and 40% height of the shirt, centered
        Canvas(modifier = Modifier.fillMaxSize()) {
            val printAreaWidth = size.width * 0.50f
            val printAreaHeight = size.width * 0.50f
            val printAreaLeft = (size.width - printAreaWidth) / 2f
            val printAreaTop = (size.height - printAreaHeight) / 2f

            // Store print area bounds for clipping
            printAreaBounds = PrintAreaBounds(
                left = printAreaLeft,
                top = printAreaTop,
                width = printAreaWidth,
                height = printAreaHeight
            )

            // Draw semi-transparent rectangle border
            drawRect(
                color = Color.Black.copy(alpha = 0.1f),
                topLeft = Offset(printAreaLeft, printAreaTop),
                size = androidx.compose.ui.geometry.Size(printAreaWidth, printAreaHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )

            // Draw corner markers
            val cornerSize = 20f
            val corners = listOf(
                // Top-left
                Offset(printAreaLeft, printAreaTop) to listOf(
                    Offset(printAreaLeft, printAreaTop) to Offset(
                        printAreaLeft + cornerSize,
                        printAreaTop
                    ),
                    Offset(printAreaLeft, printAreaTop) to Offset(
                        printAreaLeft,
                        printAreaTop + cornerSize
                    )
                ),
                // Top-right
                Offset(printAreaLeft + printAreaWidth, printAreaTop) to listOf(
                    Offset(
                        printAreaLeft + printAreaWidth,
                        printAreaTop
                    ) to Offset(printAreaLeft + printAreaWidth - cornerSize, printAreaTop),
                    Offset(
                        printAreaLeft + printAreaWidth,
                        printAreaTop
                    ) to Offset(printAreaLeft + printAreaWidth, printAreaTop + cornerSize)
                ),
                // Bottom-left
                Offset(printAreaLeft, printAreaTop + printAreaHeight) to listOf(
                    Offset(
                        printAreaLeft,
                        printAreaTop + printAreaHeight
                    ) to Offset(printAreaLeft + cornerSize, printAreaTop + printAreaHeight),
                    Offset(printAreaLeft, printAreaTop + printAreaHeight) to Offset(
                        printAreaLeft,
                        printAreaTop + printAreaHeight - cornerSize
                    )
                ),
                // Bottom-right
                Offset(printAreaLeft + printAreaWidth, printAreaTop + printAreaHeight) to listOf(
                    Offset(
                        printAreaLeft + printAreaWidth,
                        printAreaTop + printAreaHeight
                    ) to Offset(
                        printAreaLeft + printAreaWidth - cornerSize,
                        printAreaTop + printAreaHeight
                    ),
                    Offset(
                        printAreaLeft + printAreaWidth,
                        printAreaTop + printAreaHeight
                    ) to Offset(
                        printAreaLeft + printAreaWidth,
                        printAreaTop + printAreaHeight - cornerSize
                    )
                )
            )

            corners.forEach { (_, lines) ->
                lines.forEach { (start, end) ->
                    drawLine(
                        color = Color.Black.copy(alpha = 0.3f),
                        start = start,
                        end = end,
                        strokeWidth = 6f
                    )
                }
            }
        }

        // Layer 3: Draggable images with clipping to print area
        printAreaBounds?.let { bounds ->
            // Create a clipping layer that covers the entire canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        // Record print area content to graphics layer
                        printAreaGraphicsLayer.record(
                            size = IntSize(bounds.width.toInt(), bounds.height.toInt())
                        ) {
                            // Clip and translate to capture only print area
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
                        
                        // Draw the normal clipped content for display
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
                    PrintAreaImage(
                        image = image,
                        isSelected = image.id == selectedImageId,
                        printAreaBounds = bounds,
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
    
    // Provide callback to capture print area
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

data class PrintAreaBounds(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

@Composable
fun ShirtColorizer(
    modifier: Modifier = Modifier,
    shirtColor: Color,
    isFrontView: Boolean = true
) {
    val context = LocalContext.current
    val maskResId = if (isFrontView) R.drawable.front_mask else R.drawable.back_mask
    val outlineResId = if (isFrontView) R.drawable.front_outline else R.drawable.back_outline

    val outline = painterResource(id = outlineResId)

    Box(modifier = modifier) {
        // Layer 1: Mask + fill with color
        Canvas(modifier = Modifier.matchParentSize()) {
            val mask = context.getDrawable(maskResId) ?: return@Canvas

            val intrinsicW = mask.intrinsicWidth.toFloat()
            val intrinsicH = mask.intrinsicHeight.toFloat()
            val canvasW = size.width
            val canvasH = size.height

            // Scale to smallest ratio to avoid distortion
            val scale = min(canvasW / intrinsicW, canvasH / intrinsicH)

            val drawW = intrinsicW * scale
            val drawH = intrinsicH * scale

            val left = (canvasW - drawW) / 2f
            val top = (canvasH - drawH) / 2f
            val right = left + drawW
            val bottom = top + drawH

            with(drawContext.canvas.nativeCanvas) {
                val checkpoint = saveLayer(null, null)

                mask.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                mask.draw(this)

                val paint = android.graphics.Paint().apply {
                    color = shirtColor.toArgb()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        blendMode = android.graphics.BlendMode.SRC_IN
                    } else {
                        @Suppress("DEPRECATION")
                        xfermode =
                            android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                    }
                }
                drawRect(left, top, right, bottom, paint)

                restoreToCount(checkpoint)
            }
        }

        // Layer 2: outline on top
        Image(
            painter = outline,
            contentDescription = null,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Composable
fun PrintAreaImage(
    image: DraggableImage,
    isSelected: Boolean,
    printAreaBounds: PrintAreaBounds,
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
                        // Always apply pan for dragging
                        if (pan != Offset.Zero) {
                            onDrag(pan)
                        }
                        // Apply scale and rotation when they change
                        if (zoom != 1f || rotation != 0f) {
                            onTransform(zoom, rotation)
                        }
                    }
                }
        ) {
            Image(
                painter = rememberAsyncImagePainter(image.uri),
                contentDescription = "Design Image",
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

@Composable
fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

@Composable
fun DragAndDropImageCanvas(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var images by remember { mutableStateOf(listOf<DraggableImage>()) }
    var selectedImageId by remember { mutableStateOf<String?>(null) }
    val graphicsLayer = rememberGraphicsLayer()

    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val newImage = DraggableImage(
                id = System.currentTimeMillis().toString(),
                uri = it,
                offset = Offset(100f, 100f)
            )
            images = images + newImage
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            permissionLauncher.launch(permission)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top bar with controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Drag & Drop Images",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Add image button
                    FloatingActionButton(
                        onClick = {
                            if (hasPermission) {
                                imagePickerLauncher.launch("image/*")
                            } else {
                                val permission =
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        Manifest.permission.READ_MEDIA_IMAGES
                                    } else {
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    }
                                permissionLauncher.launch(permission)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Image")
                    }

                    // Capture snapshot button
                    if (images.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                        saveImageToGallery(context, bitmap)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Snapshot saved to gallery!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Failed to save snapshot: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Capture Snapshot")
                        }
                    }

                    // Delete selected image button
                    if (selectedImageId != null) {
                        FloatingActionButton(
                            onClick = {
                                images = images.filter { it.id != selectedImageId }
                                selectedImageId = null
                            },
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Image")
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .drawWithContent {
                    // Record the content to the graphics layer
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    // Draw the graphics layer
                    drawLayer(graphicsLayer)
                }
        ) {
            // Draw images
            images.forEach { image ->
                DraggableImageItem(
                    image = image,
                    isSelected = image.id == selectedImageId,
                    onDrag = { delta ->
                        images = images.map {
                            if (it.id == image.id) {
                                it.copy(
                                    offset = Offset(
                                        it.offset.x + delta.x,
                                        it.offset.y + delta.y
                                    )
                                )
                            } else {
                                it
                            }
                        }
                    },
                    onTransform = { scale, rotation ->
                        images = images.map {
                            if (it.id == image.id) {
                                it.copy(scale = it.scale * scale, rotation = it.rotation + rotation)
                            } else {
                                it
                            }
                        }
                    },
                    onSelect = { selectedImageId = image.id }
                )
            }

            // Show instruction text when no images
            if (images.isEmpty()) {
                Text(
                    text = "Tap the + button to add images\nDrag to move • Pinch to scale • Rotate with 2 fingers!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DraggableImageItem(
    image: DraggableImage,
    isSelected: Boolean,
    onDrag: (Offset) -> Unit,
    onTransform: (scale: Float, rotation: Float) -> Unit,
    onSelect: () -> Unit
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
                    // Always apply pan for dragging
                    if (pan != Offset.Zero) {
                        onDrag(pan)
                    }
                    // Apply scale and rotation when they change
                    if (zoom != 1f || rotation != 0f) {
                        onTransform(zoom, rotation)
                    }
                }
            }
    ) {
        Image(
            painter = rememberAsyncImagePainter(image.uri),
            contentDescription = "Editable Image",
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

suspend fun saveImageToGallery(context: Context, bitmap: Bitmap) {
    withContext(Dispatchers.IO) {
        val filename = "POD_${System.currentTimeMillis()}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore API for Android 10+
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/PODDemo"
                )
            }

            val resolver = context.contentResolver
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            imageUri?.let { uri ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            } ?: throw IOException("Failed to create MediaStore entry")
        } else {
            // For older Android versions
            @Suppress("DEPRECATION")
            val imagesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ).toString() + "/PODDemo"

            val image = java.io.File(imagesDir)
            if (!image.exists()) {
                image.mkdirs()
            }

            val file = java.io.File(imagesDir, filename)
            file.outputStream().use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            // Notify the media scanner
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DATA, file.absolutePath)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            }
            context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
        }
    }
}