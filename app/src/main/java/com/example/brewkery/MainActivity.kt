package com.example.brewkery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.brewkery.ui.navigation.BrewkeryNavGraph
import com.example.brewkery.ui.theme.BrewkeryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrewkeryTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BrewkeryNavGraph()
                }
            }
        }
    }
}
