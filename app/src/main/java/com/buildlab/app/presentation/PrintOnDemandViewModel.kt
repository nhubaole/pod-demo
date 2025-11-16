package com.buildlab.app.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildlab.app.presentation._ui.PreviewUiState
import com.buildlab.common.concurrency.AppDispatchers
import com.buildlab.common.concurrency.launchIO
import com.buildlab.repository.PrintOnDemandRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PrintOnDemandViewModel(
    private val repository: PrintOnDemandRepository = PrintOnDemandRepository()
): ViewModel() {

    private val _previewState = MutableStateFlow<PreviewUiState>(PreviewUiState.Idle)
    val previewState: StateFlow<PreviewUiState> = _previewState.asStateFlow()

    private var job: Job? = null

    fun generatePreview(datasetName: String, stickerBitmap: Bitmap) {
        job?.cancel()
        job = viewModelScope.launchIO {
            try {
                _previewState.value = PreviewUiState.Loading

                // Convert Bitmap to file
                val file = withContext(AppDispatchers.IO) {
                    val tmp = File.createTempFile("sticker_", ".png")
                    FileOutputStream(tmp).use {
                        stickerBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    tmp
                }

                // Call backend
                val response = repository.printSticker(
                    datasetName = datasetName,
                    sticker = file,
                    gender = "both"
                )

                // Download images
                val maleBmp = response.maleImageUrl?.let { url ->
                    val bytes = repository.downloadImage(url)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }

                val femaleBmp = response.femaleImageUrl?.let { url ->
                    val bytes = repository.downloadImage(url)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }

                _previewState.value = PreviewUiState.Success(maleBmp, femaleBmp)

            } catch (e: Exception) {
                _previewState.value = PreviewUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetPreview() {
        job?.cancel()
        _previewState.value = PreviewUiState.Idle
    }
}