package com.example.query_overflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.query_overflow.chatbot.ChatbotRoomSelectionViewModelFactory
import com.example.query_overflow.chatbot.ChatbotViewModelFactory
import com.example.query_overflow.ui.theme.QueryOverflowTheme

val firebaseHelper = FirebaseHelper()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = ChatbotViewModelFactory(
            (application as QueryOverflowApp).repository
        )

        val roomViewModelFactory = ChatbotRoomSelectionViewModelFactory(
            (application as QueryOverflowApp).repository
        )

        setContent {
            val navController = rememberNavController()
            QueryOverflowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(navController, viewModelFactory, roomViewModelFactory)
                }
            }
        }
    }
}