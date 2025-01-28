package com.example.snake_game_by_ai.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snake_game_by_ai.R
import com.example.snake_game_by_ai.game.Direction
import com.example.snake_game_by_ai.game.SnakeGameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

// Multi-color palette for snake segments
val SNAKE_COLORS = listOf(
    Color(0xFF4CAF50),   // Green
    Color(0xFF2196F3),   // Blue
    Color(0xFFF44336),   // Red
    Color(0xFF9C27B0),   // Purple
    Color(0xFFFF9800),   // Orange
    Color(0xFF009688),   // Teal
    Color(0xFFFF5722)    // Deep Orange
)

// Rainbow color palette for border
val RAINBOW_COLORS = listOf(
    Color.Red,
    Color.Yellow,
    Color.Green,
    Color.Blue,
    Color.Cyan,
    Color.Magenta
)

@Composable
fun DirectionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.5f),
            contentColor = Color.Black
        ),
        modifier = Modifier
            .size(80.dp)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(48.dp)
        )
    }
}

class SoundManager(private val context: Context) {
    private var foodEatSound: MediaPlayer? = null
    private var gameOverSound: MediaPlayer? = null

    init {
        try {
            foodEatSound = MediaPlayer.create(context, R.raw.eat_sound)
            gameOverSound = MediaPlayer.create(context, R.raw.game_over_sound)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playFoodEatSound() {
        try {
            foodEatSound?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playGameOverSound() {
        try {
            gameOverSound?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        foodEatSound?.release()
        gameOverSound?.release()
    }
}

@Composable
fun SnakeGameScreen() {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val gameState = remember { SnakeGameState(context) }
    val coroutineScope = rememberCoroutineScope()

    // Track previous score to detect food eating
    val previousScore = remember { mutableStateOf(0) }

    // Sound trigger for food eating
    LaunchedEffect(gameState.score) {
        if (gameState.score > previousScore.value) {
            soundManager.playFoodEatSound()
            previousScore.value = gameState.score
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Score and Max Score Display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Score: ${gameState.score}",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Max Score: ${gameState.maxScore}",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Game Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawGameBoard(gameState)
            }
        }

        // Control Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color.DarkGray.copy(alpha = 0.7f))
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DirectionButton(
                    icon = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Move Up",
                    onClick = { gameState.changeDirection(Direction.UP) }
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DirectionButton(
                    icon = Icons.Filled.ArrowBack,
                    contentDescription = "Move Left",
                    onClick = { gameState.changeDirection(Direction.LEFT) }
                )
                DirectionButton(
                    icon = Icons.Filled.ArrowForward,
                    contentDescription = "Move Right",
                    onClick = { gameState.changeDirection(Direction.RIGHT) }
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DirectionButton(
                    icon = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Move Down",
                    onClick = { gameState.changeDirection(Direction.DOWN) }
                )
            }
        }
    }

    // Game Loop
    LaunchedEffect(gameState.isGameOver) {
        while (!gameState.isGameOver) {
            delay(200)
            gameState.moveSnake()
        }
    }

    // Game Over Handling
    if (gameState.isGameOver) {
        LaunchedEffect(Unit) {
            soundManager.playGameOverSound()
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Game Over",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Score: ${gameState.score}",
                    color = Color.White,
                    fontSize = 24.sp
                )
                Text(
                    text = "Max Score: ${gameState.maxScore}",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Button(
                    onClick = { 
                        gameState.resetGame() 
                        coroutineScope.launch {
                            soundManager.playGameOverSound()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.3f),
                        contentColor = Color.White
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

private fun DrawScope.drawGameBoard(gameState: SnakeGameState) {
    // Draw single line border around the entire game board without corner blocks
    val borderColor = Color.White.copy(alpha = 0.5f)
    val borderStrokeWidth = 2f
    
    // Top border
    drawLine(
        color = borderColor,
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = borderStrokeWidth
    )
    
    // Bottom border
    drawLine(
        color = borderColor,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = borderStrokeWidth
    )
    
    // Left border
    drawLine(
        color = borderColor,
        start = Offset(0f, 0f),
        end = Offset(0f, size.height),
        strokeWidth = borderStrokeWidth
    )
    
    // Right border
    drawLine(
        color = borderColor,
        start = Offset(size.width, 0f),
        end = Offset(size.width, size.height),
        strokeWidth = borderStrokeWidth
    )

    // Draw rainbow border segments
    val borderWidth = 3  // Increased border width
    val cellWidth = size.width / gameState.gameGridWidth
    val cellHeight = size.height / gameState.gameGridHeight

    // Top border
    for (x in 0 until borderWidth) {
        drawRect(
            color = RAINBOW_COLORS[x % RAINBOW_COLORS.size].copy(alpha = 0.7f),
            topLeft = Offset(x * cellWidth, 0f),
            size = Size(cellWidth, cellHeight)
        )
        drawRect(
            color = RAINBOW_COLORS[x % RAINBOW_COLORS.size].copy(alpha = 0.7f),
            topLeft = Offset(x * cellWidth, (gameState.gameGridHeight - 1) * cellHeight),
            size = Size(cellWidth, cellHeight)
        )
    }

    // Side borders
    for (y in 0 until borderWidth) {
        drawRect(
            color = RAINBOW_COLORS[y % RAINBOW_COLORS.size].copy(alpha = 0.7f),
            topLeft = Offset(0f, y * cellHeight),
            size = Size(cellWidth, cellHeight)
        )
        drawRect(
            color = RAINBOW_COLORS[y % RAINBOW_COLORS.size].copy(alpha = 0.7f),
            topLeft = Offset((gameState.gameGridWidth - 1) * cellWidth, y * cellHeight),
            size = Size(cellWidth, cellHeight)
        )
    }

    // Draw snake segments with smaller size and subtle borders
    gameState.snake.forEachIndexed { index, segment ->
        val color = SNAKE_COLORS[index % SNAKE_COLORS.size]
        val darkerColor = color.copy(
            red = min(color.red * 1.2f, 1f),
            green = min(color.green * 1.2f, 1f),
            blue = min(color.blue * 1.2f, 1f)
        )

        // Reduce segment size (create inset)
        val inset = cellWidth * 0.1f  // 10% inset from cell edges
        drawRect(
            color = color,
            topLeft = Offset(
                segment.position.x * cellWidth + inset,
                segment.position.y * cellHeight + inset
            ),
            size = Size(
                cellWidth - (2 * inset), 
                cellHeight - (2 * inset)
            )
        )
        
        // Add very subtle border
        drawRect(
            color = darkerColor.copy(alpha = 0.3f),
            style = Stroke(width = 1f),
            topLeft = Offset(
                segment.position.x * cellWidth + inset,
                segment.position.y * cellHeight + inset
            ),
            size = Size(
                cellWidth - (2 * inset), 
                cellHeight - (2 * inset)
            )
        )
    }

    // Draw food with smaller circular shape
    val foodSize = cellWidth * 0.6f  // Reduce food size to 60% of cell width
    val foodCenter = Offset(
        gameState.food.position.x * cellWidth + cellWidth / 2f,
        gameState.food.position.y * cellHeight + cellHeight / 2f
    )
    
    drawCircle(
        color = Color.Red,  // Simple red color
        radius = foodSize / 2f,
        center = foodCenter
    )
}
