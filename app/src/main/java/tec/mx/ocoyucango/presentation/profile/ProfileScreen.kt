// ProfileScreen.kt
package tec.mx.ocoyucango.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import tec.mx.ocoyucango.presentation.common.TopBar
import tec.mx.ocoyucango.R
import tec.mx.ocoyucango.presentation.common.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth


@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopBar(title = "Perfil", navController = navController)

        Column(
            modifier = Modifier
                .weight(1f) // Ocupa el espacio restante
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Imagen de perfil
            Image(
                painter = painterResource(id = R.drawable.usuario),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Información del usuario
            Text(
                text = user?.displayName ?: "Nombre no disponible",
                fontSize = 24.sp
            )
            Text(
                text = "Correo: ${user?.email ?: "No disponible"}",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón de cerrar sesión
            Button(
                onClick = {
                    // Acción para cerrar sesión
                    auth.signOut()
                    // Por ejemplo, auth.signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cerrar Sesión", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        BottomNavigationBar(navController = navController)
    }
}
