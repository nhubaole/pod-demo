package com.buildlab.app.presentation._ui

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


// ----------------------- UI STATE (SAFE) -----------------------

sealed class PreviewUiState {
    object Idle : PreviewUiState()
    object Loading : PreviewUiState()
    data class Success(
        val maleKey: String?,
        val femaleKey: String?
    ) : PreviewUiState()
    data class Error(val message: String) : PreviewUiState()
}


// ----------------------- MAIN CONTENT --------------------------

@Composable
fun PreviewContent(
    previewState: PreviewUiState,
    onReset: () -> Unit,
    onGetBitMap: (String?) -> Bitmap?,
) {
    when (previewState) {

        is PreviewUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is PreviewUiState.Success -> {
            val male = onGetBitMap(previewState.maleKey)
            val female = onGetBitMap(previewState.femaleKey)

            PreviewScreen(
                male = male,
                female = female,
                onBack = onReset
            )
        }

        is PreviewUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    previewState.message,
                    color = Color.Red
                )
            }
        }

        PreviewUiState.Idle -> Unit
    }
}


// ----------------------- FINAL UI SCREEN --------------------------

@Composable
private fun PreviewScreen(
    male: Bitmap?,
    female: Bitmap?,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBack() }
                )

                Text(
                    "Preview",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.size(28.dp))
            }

            PreviewImages(
                male = male,
                female = female,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


// ----------------------- IMAGES LIST --------------------------

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

        PreviewCard(
            label = "Male Preview",
            bitmap = male
        )

        PreviewCard(
            label = "Female Preview",
            bitmap = female
        )
    }
}


// ----------------------- CARD COMPONENT --------------------------

@Composable
private fun PreviewCard(
    label: String,
    bitmap: Bitmap?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = label,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}
