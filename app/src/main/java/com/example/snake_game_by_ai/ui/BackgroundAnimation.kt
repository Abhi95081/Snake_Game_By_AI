package com.example.snake_game_by_ai.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

fun DrawScope.drawAnimatedGradientBackground(offset: Float) {
    val colors = listOf(
        Color(0xFF1A237E),  // Deep Blue
        Color(0xFF283593),  // Darker Blue
        Color(0xFF3F51B5),  // Bright Blue
        Color(0xFF5C6BC0)   // Light Blue
    )

    // Create a dynamic gradient that shifts based on the offset
    val gradientBrush = Brush.linearGradient(
        colors = colors,
        start = Offset(0f, size.height * (1 - offset)),
        end = Offset(size.width, size.height * offset)
    )

    // Draw the gradient background
    drawRect(
        brush = gradientBrush,
        size = size
    )
}
