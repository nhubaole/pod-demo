package com.buildlab.app.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.ACTION_REQUEST_PERMISSIONS
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.EXTRA_PERMISSIONS
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buildlab.app.presentation._ui.ColorSwatch
import com.buildlab.app.presentation._ui.ColorSwatchUiState
import com.buildlab.app.presentation._ui.EditableImageUiState
import com.buildlab.app.presentation._ui.PreviewContent
import com.buildlab.app.presentation._ui.PreviewUiState
import com.buildlab.app.presentation._ui.PrintAreaView
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private companion object {
        private const val REQUEST_READ_STORAGE_PERMISSION = 1
    }

    private var permissionLauncher: ActivityResultLauncher<Int>? = null
    private var isPermissionsReady by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        registerPermissionLauncher()
        forceRequirementsToUse()

        setContent {
            val viewModel: PrintOnDemandViewModel = viewModel()

            PrintOnDemandScreen(
                viewModel.previewState.collectAsStateWithLifecycle(),
                onGenerateImage = { bitmap -> viewModel.generatePreview("data1", bitmap) },
                onGetBitMap = { key -> viewModel.getBitmap(key) },
                onReset = { viewModel.resetPreview() }
            )
        }
    }

    private fun registerPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
            object : ActivityResultContract<Int, Int>() {
                var type = 0

                override fun createIntent(context: Context, input: Int): Intent {
                    type = input
                    when (input) {
                        REQUEST_READ_STORAGE_PERMISSION -> {
                            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Intent(
                                    ACTION_REQUEST_PERMISSIONS
                                ).apply {
                                    putExtra(
                                        EXTRA_PERMISSIONS,
                                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                                    )
                                }
                            } else {
                                Intent(
                                    ACTION_REQUEST_PERMISSIONS
                                ).apply {
                                    putExtra(
                                        EXTRA_PERMISSIONS,
                                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    )
                                }
                            }
                        }

                        else -> throw Exception("Unknown type to request.")
                    }
                }

                override fun parseResult(resultCode: Int, intent: Intent?): Int {
                    return type
                }
            }
        ) {
            forceRequirementsToUse()
        }
    }

    private fun isReadStorageNotAccessible(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PERMISSION_GRANTED
        } else {
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED
        }
    }

    private fun requestReadStoragePermission() {
        permissionLauncher?.launch(REQUEST_READ_STORAGE_PERMISSION)
    }

    private fun onReadyToUse() {
        isPermissionsReady = true
    }

    private fun forceRequirementsToUse() {
        if (isReadStorageNotAccessible()) {
            requestReadStoragePermission()
            return
        }

        onReadyToUse()
    }
}

@Composable
fun PrintOnDemandScreen(
    statePreview: State<PreviewUiState>,
    onGenerateImage: (Bitmap) -> Unit,
    onGetBitMap: (String?) -> Bitmap?,
    onReset: () -> Unit,
) {
    val previewUiState by statePreview

    var isFrontView by remember { mutableStateOf(true) }
    var images by remember { mutableStateOf(listOf<EditableImageUiState>()) }
    var selectedImageId by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    var capturePrintArea: (suspend () -> Bitmap?) by remember { mutableStateOf({ null }) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val newImage = EditableImageUiState(
                id = System.currentTimeMillis().toString(),
                uri = it,
                offset = Offset(0f, 0f)
            )
            images = images + newImage
        }
    }

    var colorSwatches by remember {
        mutableStateOf(
            arrayOf(
                ColorSwatchUiState(Color(0xFFFFFFFF), true), // White
                ColorSwatchUiState(Color(0xFF000000), false), // Black
                ColorSwatchUiState(Color(0xFF3D3D3D), false), // Grey
                ColorSwatchUiState(Color(0xFFFF5252), false), // Red
                ColorSwatchUiState(Color(0xFF2196F3), false), // Blue
                ColorSwatchUiState(Color(0xFF4CAF50), false), // Green
                ColorSwatchUiState(Color(0xFFFFC107), false), // Amber
                ColorSwatchUiState(Color(0xFF9C27B0), false), // Purple
                ColorSwatchUiState(Color(0xFFFF9800), false), // Orange
                ColorSwatchUiState(Color(0xFF00BCD4), false), // Cyan
                ColorSwatchUiState(Color(0xFFE91E63), false), // Pink
                ColorSwatchUiState(Color(0xFF795548), false), // Brown
                ColorSwatchUiState(Color(0xFF607D8B), false), // Blue Grey
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
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

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FloatingActionButton(
                            onClick = {
                                imagePickerLauncher.launch("image/*")
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

                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    val bitmap = capturePrintArea()
                                    if (bitmap != null) {
                                        onGenerateImage(bitmap)
                                    }
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Add Image",
                                modifier = Modifier.size(20.dp)
                            )
                        }

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

                ViewSideToggle(
                    isFrontView = isFrontView,
                    onViewChange = { isFront ->
                        isFrontView = isFront
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            PrintAreaView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shirtColor = colorSwatches.find { it.isSelected }?.color ?: Color.White,
                isFrontView = isFrontView,
                images = images,
                selectedImageId = selectedImageId,
                onDragImage = { imageId, delta ->
                    images = handleImageDrag(images, imageId, delta)
                },
                onTransformImage = { imageId, scale, rotation ->
                    images = handleImageTransform(images, imageId, scale, rotation)
                },
                onSelectImage = { imageId ->
                    selectedImageId = imageId
                },
                onSavePrintArea = { provider ->
                    capturePrintArea = provider
                }

            )
        }

        ColorPalette(
            colorSwatches,
            onColorSelected = { color ->
                colorSwatches = colorSwatches.map {
                    if (it == color) {
                        it.copy(isSelected = true)
                    } else {
                        it.copy(isSelected = false)
                    }
                }.toTypedArray()
            }
        )
    }

    PreviewContent(
        previewUiState,
        onGetBitMap = onGetBitMap,
        onReset = {
            images = listOf()
            selectedImageId = null
            onReset()
        },
    )
}

@Composable
private fun ViewSideToggle(
    isFrontView: Boolean,
    onViewChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onViewChange(true) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFrontView) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text("Front View")
        }

        Button(
            onClick = { onViewChange(false) },
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

@Composable
private fun ColorPalette(
    colorSwatches: Array<ColorSwatchUiState>,
    onColorSelected: (ColorSwatchUiState) -> Unit
) {
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colorSwatches.forEach { color ->
                    ColorSwatch(
                        color,
                        onClick = {
                            onColorSelected(color)
                        }
                    )
                }
            }
        }
    }
}

private fun handleImageDrag(
    images: List<EditableImageUiState>,
    imageId: String,
    delta: Offset
): List<EditableImageUiState> {
    return images.map {
        if (it.id == imageId) {
            it.copy(
                offset = Offset(
                    it.offset.x + delta.x,
                    it.offset.y + delta.y
                )
            )
        } else it
    }
}

private fun handleImageTransform(
    images: List<EditableImageUiState>,
    imageId: String,
    scale: Float,
    rotation: Float
): List<EditableImageUiState> {
    return images.map {
        if (it.id == imageId) {
            it.copy(
                scale = it.scale * scale,
                rotation = it.rotation + rotation
            )
        } else it
    }
}