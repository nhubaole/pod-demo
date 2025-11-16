package com.buildlab.pod_demo

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

sealed class PreviewUiState {
    object Idle : PreviewUiState()
    object Loading : PreviewUiState()
    data class Success(
        val male: Bitmap?,
        val female: Bitmap?
    ) : PreviewUiState()

    data class Error(val message: String) : PreviewUiState()
}

@Composable
fun PreviewContent(
    previewState: PreviewUiState,
    onReset: () -> Unit,
) {
    when (previewState) {
        is PreviewUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is PreviewUiState.Success -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onReset() }
                        )

                        Text(
                            "Preview",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )

                        Box(modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PreviewImages(
                        previewState.male,
                        previewState.female,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

        }

        is PreviewUiState.Error -> {
            val msg = previewState.message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(msg, color = Color.Red)
            }
        }

        PreviewUiState.Idle -> {}
    }


}

@Composable
private fun PreviewImages(
    male: Bitmap?,
    female: Bitmap?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Row containing both previews
        Column (
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---- Male Preview ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (male != null) {
                    Image(
                        bitmap = male.asImageBitmap(),
                        contentDescription = "Male Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = "Male\nPreview",
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }

            // ---- Female Preview ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (female != null) {
                    Image(
                        bitmap = female.asImageBitmap(),
                        contentDescription = "Female Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = "Female\nPreview",
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }
        }
    }
}
