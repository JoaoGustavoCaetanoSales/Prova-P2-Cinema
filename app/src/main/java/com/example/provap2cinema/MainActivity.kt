package com.example.provap2cinema

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.provap2cinema.navigation.NavGraph
import com.example.provap2cinema.ui.theme.ProvaP2CinemaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProvaP2CinemaTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
