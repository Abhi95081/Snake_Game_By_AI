package com.example.snake_game_by_ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.snake_game_by_ai.ui.SnakeGameScreen
import com.example.snake_game_by_ai.ui.theme.Snake_Game_By_AITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            Snake_Game_By_AITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    SnakeGameScreen()
                }
            }
        }
    }
}