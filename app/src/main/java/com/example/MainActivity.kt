package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CineViewModel
import com.example.ui.components.CineHome
import com.example.ui.components.Premium4KVideoPlayer
import com.example.ui.components.ProfileSelectionScreen
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fully edge-to-edge full bleed screen
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val viewModel: CineViewModel = viewModel()
                val currentScreen by viewModel.navScreen.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BlackBackground
                ) {
                    Crossfade(
                        targetState = currentScreen,
                        label = "screen_navigation"
                    ) { screen ->
                        when (screen) {
                            "profiles" -> ProfileSelectionScreen(viewModel = viewModel)
                            "home_screen" -> CineHome(viewModel = viewModel)
                            "player" -> Premium4KVideoPlayer(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
