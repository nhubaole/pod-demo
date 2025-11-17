package com.buildlab.app.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildlab.app.presentation._ui.PreviewUiState
import com.buildlab.common.concurrency.launchIO
import com.buildlab.repository.PrintOnDemandRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class PrintOnDemandViewModel(
    private val repository: PrintOnDemandRepository = PrintOnDemandRepository()
) : ViewModel() {

    private val _previewState = MutableStateFlow<PreviewUiState>(PreviewUiState.Idle)
    val previewState: StateFlow<PreviewUiState> = _previewState.asStateFlow()

    private val bitmapCache = mutableMapOf<String, Bitmap>()

    private var job: Job? = null

    // Retrieve bitmap from cache
    fun getBitmap(key: String?): Bitmap? = key?.let { bitmapCache[it] }

    fun generatePreview(datasetName: String, stickerBitmap: Bitmap) {
        job?.cancel()
        job = viewModelScope.launchIO {
            try {
                _previewState.value = PreviewUiState.Loading

                // Convert Bitmap to temp file
                val stickerFile = File.createTempFile("sticker_", ".png").apply {
                    FileOutputStream(this).use { out ->
                        stickerBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }

                // Call backend
                val response = repository.printSticker(
                    datasetName = datasetName,
                    sticker = stickerFile,
                    gender = "both"
                )

                // Download images
                val maleKey = response.maleImageUrl?.let { url ->
                    val bytes = repository.downloadImage(url)
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val key = UUID.randomUUID().toString()
                    bitmapCache[key] = bmp
                    key
                }

                val femaleKey = response.femaleImageUrl?.let { url ->
                    val bytes = repository.downloadImage(url)
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val key = UUID.randomUUID().toString()
                    bitmapCache[key] = bmp
                    key
                }

                _previewState.value = PreviewUiState.Success(
                    maleKey = maleKey,
                    femaleKey = femaleKey
                )

            } catch (e: Exception) {
                _previewState.value = PreviewUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetPreview() {
        job?.cancel()
        _previewState.value = PreviewUiState.Idle
        bitmapCache.clear()
    }
}
