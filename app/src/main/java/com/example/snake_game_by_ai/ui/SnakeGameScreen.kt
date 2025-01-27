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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snake_game_by_ai.R
import com.example.snake_game_by_ai.game.Direction
import com.example.snake_game_by_ai.game.SnakeGameState
import com.example.snake_game_by_ai.ui.theme.FoodRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

@Composable
fun DirectionButton(
    icon: ImageVector,
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
                val cellSize = size.width / 15f

                // Draw Snake with multi-color segments
                gameState.snake.forEachIndexed { index, segment ->
                    val segmentColor = SNAKE_COLORS[index % SNAKE_COLORS.size]
                    
                    drawRect(
                        color = segmentColor,
                        topLeft = Offset(
                            segment.position.x * cellSize,
                            segment.position.y * cellSize
                        ),
                        size = Size(cellSize, cellSize)
                    )
                }

                // Draw Food
                drawRect(
                    color = FoodRed,
                    topLeft = Offset(
                        gameState.food.position.x * cellSize,
                        gameState.food.position.y * cellSize
                    ),
                    size = Size(cellSize, cellSize)
                )
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
        coroutineScope.launch(Dispatchers.Default) {
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
                        coroutineScope.launch(Dispatchers.Default) {
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
