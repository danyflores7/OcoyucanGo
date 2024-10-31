// HomeScreen.kt
package tec.mx.ocoyucango.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tec.mx.ocoyucango.presentation.common.BottomNavigationBar
import tec.mx.ocoyucango.presentation.common.TopBar
import tec.mx.ocoyucango.ui.theme.Green
import tec.mx.ocoyucango.R


@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(title = "Mapa", navController = navController)

        // Contenido de la pantalla
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Imagen del mapa o contenido del mapa
            Image(
                painter = painterResource(id = R.drawable.maparecorrido),
                contentDescription = "Mapa",
                modifier = Modifier.fillMaxSize()
            )

            // Botón para iniciar/terminar recorrido
            Button(
                onClick = {
                    // Acción para iniciar o detener recorrido
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Iniciar recorrido")
            }
        }

        BottomNavigationBar(navController = navController)
    }
}
