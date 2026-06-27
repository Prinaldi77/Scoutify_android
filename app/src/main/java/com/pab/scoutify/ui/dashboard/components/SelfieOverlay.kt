package com.pab.scoutify.ui.dashboard.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun SelfieOverlay(
    isFaceDetected: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scannerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scannerProgress"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f) // Required for BlendMode.Clear
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Background overlay
        drawRect(color = Color.Black.copy(alpha = 0.5f))

        // Oval dimensions
        val ovalWidth = canvasWidth * 0.7f
        val ovalHeight = canvasHeight * 0.5f
        val left = (canvasWidth - ovalWidth) / 2
        val top = (canvasHeight - ovalHeight) / 2
        
        val ovalRect = Rect(
            offset = Offset(left, top),
            size = Size(ovalWidth, ovalHeight)
        )

        // Clear the oval area
        drawOval(
            color = Color.Transparent,
            topLeft = ovalRect.topLeft,
            size = ovalRect.size,
            blendMode = BlendMode.Clear
        )

        // Draw oval border
        val borderColor = if (isFaceDetected) Color(0xFF2E7D32) else Color.Red
        drawOval(
            color = borderColor,
            topLeft = ovalRect.topLeft,
            size = ovalRect.size,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw animated laser scanning line
        val scanY = top + (ovalHeight * scannerProgress)
        // We calculate the width of the oval at this Y level using the ellipse equation:
        // (x - h)^2 / a^2 + (y - k)^2 / b^2 = 1
        val h = left + ovalWidth / 2
        val k = top + ovalHeight / 2
        val a = ovalWidth / 2
        val b = ovalHeight / 2
        val dy = scanY - k
        val term = 1f - (dy * dy) / (b * b)
        if (term >= 0) {
            val halfLineWidth = a * kotlin.math.sqrt(term)
            drawLine(
                color = borderColor.copy(alpha = 0.8f),
                start = Offset(h - halfLineWidth, scanY),
                end = Offset(h + halfLineWidth, scanY),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Corner indicators (optional, similar to design)
        val cornerSize = 40.dp.toPx()
        val cornerPadding = 20.dp.toPx()
        
        // Top Left
        drawPath(
            path = Path().apply {
                moveTo(left - cornerPadding, top - cornerPadding + cornerSize)
                lineTo(left - cornerPadding, top - cornerPadding)
                lineTo(left - cornerPadding + cornerSize, top - cornerPadding)
            },
            color = borderColor,
            style = Stroke(width = 4.dp.toPx())
        )

        // Top Right
        drawPath(
            path = Path().apply {
                moveTo(left + ovalWidth + cornerPadding - cornerSize, top - cornerPadding)
                lineTo(left + ovalWidth + cornerPadding, top - cornerPadding)
                lineTo(left + ovalWidth + cornerPadding, top - cornerPadding + cornerSize)
            },
            color = borderColor,
            style = Stroke(width = 4.dp.toPx())
        )

        // Bottom Left
        drawPath(
            path = Path().apply {
                moveTo(left - cornerPadding, top + ovalHeight + cornerPadding - cornerSize)
                lineTo(left - cornerPadding, top + ovalHeight + cornerPadding)
                lineTo(left - cornerPadding + cornerSize, top + ovalHeight + cornerPadding)
            },
            color = borderColor,
            style = Stroke(width = 4.dp.toPx())
        )

        // Bottom Right
        drawPath(
            path = Path().apply {
                moveTo(left + ovalWidth + cornerPadding - cornerSize, top + ovalHeight + cornerPadding)
                lineTo(left + ovalWidth + cornerPadding, top + ovalHeight + cornerPadding)
                lineTo(left + ovalWidth + cornerPadding, top + ovalHeight + cornerPadding - cornerSize)
            },
            color = borderColor,
            style = Stroke(width = 4.dp.toPx())
        )
    }
}
