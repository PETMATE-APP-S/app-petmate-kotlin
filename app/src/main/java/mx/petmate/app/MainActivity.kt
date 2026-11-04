package mx.petmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import mx.petmate.app.ui.navigation.PetMateNavHost
import mx.petmate.app.ui.theme.PetMateEdelTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetMateEdelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PetMateNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
