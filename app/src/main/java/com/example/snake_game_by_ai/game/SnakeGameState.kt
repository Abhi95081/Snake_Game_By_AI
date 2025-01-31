package com.example.snake_game_by_ai.game

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

data class SnakeSegment(val position: Offset)

class SnakeGameState(
    private val context: Context,
    private val gridWidth: Int = 15,
    private val gridHeight: Int = 25
) {
    val gameGridWidth: Int get() = gridWidth
    val gameGridHeight: Int get() = gridHeight

    private val prefs: SharedPreferences = context.getSharedPreferences("SnakeGamePrefs", Context.MODE_PRIVATE)

    var snake by mutableStateOf(listOf(SnakeSegment(Offset(gridWidth / 2f, gridHeight / 2f))))
    var food by mutableStateOf(generateFood())
    var direction by mutableStateOf(Direction.RIGHT)
    var score by mutableStateOf(0)
    var maxScore by mutableStateOf(0)
    var isGameOver by mutableStateOf(false)

    init {
        // Initialize max score from SharedPreferences
        maxScore = prefs.getInt("max_score", 0)
        Log.d("SnakeGameState", "Initializing game state with grid: $gridWidth x $gridHeight")
        Log.d("SnakeGameState", "Current Max Score: $maxScore")
    }

    private fun updateMaxScore(newScore: Int) {
        val currentMaxScore = prefs.getInt("max_score", 0)
        if (newScore > currentMaxScore) {
            prefs.edit().putInt("max_score", newScore).apply()
            maxScore = newScore
            Log.d("SnakeGameState", "New Max Score: $maxScore")
        }
    }

    fun moveSnake() {
        if (isGameOver) return

        val head = snake.first()
        val newHead = when (direction) {
            Direction.UP -> SnakeSegment(Offset(head.position.x, head.position.y - 1))
            Direction.DOWN -> SnakeSegment(Offset(head.position.x, head.position.y + 1))
            Direction.LEFT -> SnakeSegment(Offset(head.position.x - 1, head.position.y))
            Direction.RIGHT -> SnakeSegment(Offset(head.position.x + 1, head.position.y))
        }

        // Check wall collision
        if (newHead.position.x < 0 || newHead.position.x >= gridWidth ||
            newHead.position.y < 0 || newHead.position.y >= gridHeight) {
            Log.d("SnakeGameState", "Wall collision detected")
            gameOver()
            return
        }

        // Check self-collision
        if (snake.any { it.position == newHead.position }) {
            Log.d("SnakeGameState", "Self-collision detected")
            gameOver()
            return
        }

        // Precise food collision check
        // Check if any part of the snake touches the food
        val isFoodEaten = snake.any { 
            it.position.x.toInt() == food.position.x.toInt() && 
            it.position.y.toInt() == food.position.y.toInt() 
        }

        // Check food collision
        val newSnake = if (isFoodEaten) {
            Log.d("SnakeGameState", "Food Eaten! Current snake length: ${snake.size}")
            score++
            food = generateFood()
            listOf(newHead) + snake  // Grow the snake
        } else {
            val growingSnake = listOf(newHead) + snake
            growingSnake.dropLast(1)  // Maintain snake length
        }

        // Update snake
        snake = newSnake

        // Additional logging
        Log.d("SnakeGameState", "Snake Length: ${snake.size}, Score: $score")
    }

    private fun gameOver() {
        updateMaxScore(score)
        isGameOver = true
    }

    fun resetGame() {
        snake = listOf(SnakeSegment(Offset(gridWidth / 2f, gridHeight / 2f)))
        food = generateFood()
        direction = Direction.RIGHT
        score = 0
        isGameOver = false
        Log.d("SnakeGameState", "Game reset")
    }

    fun changeDirection(newDirection: Direction) {
        // Prevent 180-degree turns and invalid moves
        val isOpposite = (direction == Direction.UP && newDirection == Direction.DOWN) ||
                (direction == Direction.DOWN && newDirection == Direction.UP) ||
                (direction == Direction.LEFT && newDirection == Direction.RIGHT) ||
                (direction == Direction.RIGHT && newDirection == Direction.LEFT)

        // Prevent changing to the same direction or opposite direction
        if (!isOpposite) {
            direction = newDirection
            Log.d("SnakeGameState", "Direction changed to $newDirection")
        } else {
            Log.d("SnakeGameState", "Invalid direction change attempt: $newDirection")
        }
    }

    private fun generateFood(): SnakeSegment {
        // Find all empty grid positions
        val emptyPositions = (0 until gridWidth).flatMap { x ->
            (0 until gridHeight).map { y ->
                Offset(x.toFloat(), y.toFloat())
            }
        }.filterNot { pos -> 
            snake.any { 
                it.position.x.toInt() == pos.x.toInt() && 
                it.position.y.toInt() == pos.y.toInt() 
            }
        }

        return if (emptyPositions.isNotEmpty()) {
            val newFood = SnakeSegment(emptyPositions.random())
            Log.d("SnakeGameState", "New Food Generated at (${newFood.position.x}, ${newFood.position.y})")
            newFood
        } else {
            // Game is essentially won - all grid positions are filled
            Log.d("SnakeGameState", "Game completed - all grid positions filled")
            gameOver()
            SnakeSegment(Offset(-1f, -1f))  // Invalid food position
        }
    }
}
