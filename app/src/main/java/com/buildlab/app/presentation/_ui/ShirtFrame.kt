package com.buildlab.app.presentation._ui

import android.graphics.Color
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.buildlab.pod_demo.R
import kotlin.math.min

@Composable
fun ShirtFrame(
    modifier: Modifier = Modifier,
    shirtColor: androidx.compose.ui.graphics.Color,
    isFrontView: Boolean = true
) {
    val context = LocalContext.current
    val maskResId = if (isFrontView) R.drawable.front_mask else R.drawable.back_mask
    val outlineResId = if (isFrontView) R.drawable.front_outline else R.drawable.back_outline

    val outline = painterResource(id = outlineResId)

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val mask = context.getDrawable(maskResId) ?: return@Canvas

            val intrinsicW = mask.intrinsicWidth.toFloat()
            val intrinsicH = mask.intrinsicHeight.toFloat()
            val canvasW = size.width
            val canvasH = size.height

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

        Image(
            painter = outline,
            contentDescription = null,
            modifier = Modifier.matchParentSize()
        )
    }
}