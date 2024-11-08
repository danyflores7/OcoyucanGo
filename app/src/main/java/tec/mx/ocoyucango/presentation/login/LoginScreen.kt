package tec.mx.ocoyucango.presentation.login

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import tec.mx.ocoyucango.presentation.viewmodel.RouteViewModel
import tec.mx.ocoyucango.presentation.viewmodel.SpeciesViewModel
import tec.mx.ocoyucango.ui.theme.Green

@Composable
fun LoginScreen(
    navController: NavHostController,
    auth: FirebaseAuth,
    routeViewModel: RouteViewModel,
    speciesViewModel: SpeciesViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "OcoyucanGo",
            color = Green,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Inicio de sesión",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo o número de teléfono") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Default.VisibilityOff
                else
                    Icons.Default.Visibility

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Mostrar contraseña")
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "¿Olvidaste tu contraseña?",
            color = Green,
            modifier = Modifier
                .align(Alignment.End)
                .clickable {
                    // Navegar a pantalla de recuperación de contraseña
                }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Navegar a la pantalla principal
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                        Log.i("OcoyucanGo", "Inicio de sesión exitoso")
                    } else {
                        errorMessage = "Error en inicio de sesión. Verifica tus credenciales."
                        Log.e("OcoyucanGo", "Error en inicio de sesión: ${task.exception?.message}")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Green)
        ) {
            Text(text = "Iniciar sesión", color = MaterialTheme.colorScheme.onPrimary)
        }
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "¿No tienes una cuenta? Registrarse",
            color = Green,
            modifier = Modifier.clickable {
                navController.navigate("signup")
            }
        )
    }
}
