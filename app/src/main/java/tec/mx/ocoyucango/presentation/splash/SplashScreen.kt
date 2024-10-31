package tec.mx.ocoyucango.presentation.splash

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tec.mx.ocoyucango.R

@Composable
fun SplashScreen(navController: NavHostController) {
    // Temporizador de 2 segundos
    Handler(Looper.getMainLooper()).postDelayed({
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }, 2000)

    // Diseño de la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E7D32)), // Color verde sólido
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo), // Asegúrate de tener tu logo en res/drawable
            contentDescription = "Logo OcoyucanGo",
            modifier = Modifier.size(200.dp)
        )
    }
}
