// MainActivity.kt
package tec.mx.ocoyucango

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import tec.mx.ocoyucango.presentation.viewmodel.RouteViewModel
import tec.mx.ocoyucango.ui.theme.OcoyucanGoTheme
import androidx.activity.viewModels
import com.google.firebase.FirebaseApp
import tec.mx.ocoyucango.presentation.viewmodel.SpeciesViewModel

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private val routeViewModel: RouteViewModel by viewModels()
    private val speciesViewModel: SpeciesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        auth = Firebase.auth

        setContent {
            OcoyucanGoTheme {
                val navController = rememberNavController()
                // Pasar los ViewModels a Navigation
                Navigation(navController, auth, routeViewModel, speciesViewModel)
            }
        }
    }
}
