// SignUpScreen.kt

package tec.mx.ocoyucango.presentation.signup

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tec.mx.ocoyucango.ui.theme.Green
import com.google.firebase.auth.ktx.userProfileChangeRequest
import tec.mx.ocoyucango.presentation.viewmodel.RouteViewModel
import tec.mx.ocoyucango.presentation.viewmodel.SpeciesViewModel

@Composable
fun SignUpScreen(
    navController: NavHostController,
    auth: FirebaseAuth,
    routeViewModel: RouteViewModel,
    speciesViewModel: SpeciesViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Inicializar Firestore
    val firestore = FirebaseFirestore.getInstance()

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
            text = "Registro",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
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
                    Icons.Filled.VisibilityOff
                else
                    Icons.Default.Visibility

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Mostrar contraseña")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.VisibilityOff
                else
                    Icons.Default.Visibility

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Mostrar contraseña")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (password == confirmPassword) {
                    // Iniciar carga
                    isLoading = true

                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser

                            // Actualizar el perfil del usuario para establecer el displayName
                            val profileUpdates = userProfileChangeRequest {
                                displayName = name
                            }

                            user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileUpdateTask ->
                                if (profileUpdateTask.isSuccessful) {
                                    // Inicializar datos en Firestore
                                    if (user.uid.isNotEmpty()) {
                                        // Crear el documento del usuario
                                        val userDocRef = firestore.collection("Usuarios").document(user.uid)

                                        // Datos del nivel inicial
                                        val nivelInicial = hashMapOf(
                                            "numero_nivel" to 1,
                                            "nombre_nivel" to "Explorador Novato",
                                            "fecha" to Timestamp.now()
                                        )

                                        // Datos del usuario
                                        val userData = hashMapOf(
                                            "nombre" to name,
                                            "correo" to email,
                                            "puntos_usuario" to 0,
                                            "notificaciones_activas" to true,
                                            "nivel" to nivelInicial
                                        )

                                        userDocRef.set(userData)
                                            .addOnSuccessListener {
                                                Log.i("OcoyucanGo", "Datos del usuario inicializados en Firestore.")

                                                // Crear subcolecciones con documento 'plantilla' vacío
                                                // Subcolección 'rutas' con documento 'plantilla'
                                                val rutaPlantilla = hashMapOf(
                                                    "inicio" to Timestamp.now(),
                                                    "fin" to Timestamp.now(),
                                                    "duracion" to 0L,
                                                    "distancia" to 0.0,
                                                    "puntos" to 0,
                                                    "compartido" to false,
                                                    "foto" to ""
                                                )

                                                userDocRef.collection("rutas").document("plantilla")
                                                    .set(rutaPlantilla)
                                                    .addOnSuccessListener {
                                                        Log.d("OcoyucanGo", "Subcolección 'rutas' creada con documento 'plantilla'.")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("OcoyucanGo", "Error al crear subcolección 'rutas': ${e.message}")
                                                    }

                                                // Subcolección 'identificaciones' con documento 'plantilla'
                                                val identificacionPlantilla = hashMapOf(
                                                    "nombre" to "",
                                                    "fecha" to Timestamp.now(),
                                                    "puntos" to 0
                                                )

                                                userDocRef.collection("identificaciones").document("plantilla")
                                                    .set(identificacionPlantilla)
                                                    .addOnSuccessListener {
                                                        Log.d("OcoyucanGo", "Subcolección 'identificaciones' creada con documento 'plantilla'.")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("OcoyucanGo", "Error al crear subcolección 'identificaciones': ${e.message}")
                                                    }

                                                // Navegar a la pantalla principal
                                                navController.navigate("home") {
                                                    popUpTo("signup") { inclusive = true }
                                                }
                                                Log.i("OcoyucanGo", "Registro y actualización de perfil exitosos")
                                                isLoading = false
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("OcoyucanGo", "Error al inicializar datos del usuario en Firestore: ${e.message}")
                                                errorMessage = "Error al inicializar tus datos. Intenta de nuevo."
                                                isLoading = false
                                            }
                                    } else {
                                        Log.e("OcoyucanGo", "UID del usuario vacío.")
                                        errorMessage = "Error en el registro. Intenta de nuevo."
                                        isLoading = false
                                    }
                                } else {
                                    errorMessage = "Error al actualizar el perfil. Intenta de nuevo."
                                    Log.e("OcoyucanGo", "Error al actualizar el perfil: ${profileUpdateTask.exception?.message}")
                                    isLoading = false
                                }
                            }
                        } else {
                            errorMessage = "Error en el registro. Intenta de nuevo."
                            Log.e("OcoyucanGo", "Error en el registro: ${task.exception?.message}")
                            isLoading = false
                        }
                    }
                } else {
                    errorMessage = "Las contraseñas no coinciden."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Green)
        ) {
            Text(text = "Registrarse", color = MaterialTheme.colorScheme.onPrimary)
        }
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "¿Ya tienes una cuenta? Iniciar sesión",
            color = Green,
            modifier = Modifier.clickable {
                navController.navigate("login")
            }
        )
        // Mostrar un indicador de carga si está registrando
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = Green)
        }
    }
}
