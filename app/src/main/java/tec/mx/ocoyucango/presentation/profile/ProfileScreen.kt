// ProfileScreen.kt
package tec.mx.ocoyucango.presentation.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import tec.mx.ocoyucango.presentation.common.BottomNavigationBar
import tec.mx.ocoyucango.presentation.common.TopBar
import tec.mx.ocoyucango.R
import tec.mx.ocoyucango.presentation.viewmodel.RouteViewModel
import tec.mx.ocoyucango.presentation.viewmodel.SpeciesViewModel
import java.io.File
import java.util.UUID

@Composable
fun ProfileScreen(
    navController: NavController,
    routeViewModel: RouteViewModel,
    speciesViewModel: SpeciesViewModel
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    // Estado para manejar la URL de la foto de perfil
    var profileImageUrl by remember { mutableStateOf(user?.photoUrl?.toString()) }

    // Estado para mostrar mensajes de carga o errores
    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }

    // Launcher para seleccionar una imagen
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                // Subir la imagen seleccionada
                uploadProfileImage(it, storage, firestore) { success, message, url ->
                    if (success && url != null) {
                        profileImageUrl = url
                        uploadMessage = "Foto de perfil actualizada exitosamente."
                    } else {
                        uploadMessage = message ?: "Error al subir la foto de perfil."
                    }
                    isUploading = false
                }
                isUploading = true
            }
        }
    )

    // Obtener la última versión del usuario (para reflejar cambios en FirebaseAuth)
    LaunchedEffect(user) {
        user?.reload()?.await()
        profileImageUrl = user?.photoUrl?.toString()
    }

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

            // Imagen de perfil (clicable para seleccionar nueva foto)
            if (profileImageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(profileImageUrl),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable {
                            // Abrir el selector de imágenes
                            launcher.launch("image/*")
                        },
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.usuario),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable {
                            // Abrir el selector de imágenes
                            launcher.launch("image/*")
                        }
                )
            }

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

            // Mostrar mensaje de carga o éxito/error
            uploadMessage?.let { message ->
                Text(
                    text = message,
                    color = if (message.contains("exitosamente")) Color.Green else Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Botón de cerrar sesión
            Button(
                onClick = {
                    // Acción para cerrar sesión
                    auth.signOut()
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

    // Mostrar indicador de carga durante la subida
    if (isUploading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Subiendo...", color = Color.White)
            }
        }
    }
}

/**
 * Función para subir la imagen de perfil a Firebase Storage y actualizar el perfil del usuario.
 *
 * @param uri URI de la imagen seleccionada.
 * @param storage Instancia de FirebaseStorage.
 * @param firestore Instancia de FirebaseFirestore.
 * @param callback Callback para manejar el resultado de la subida.
 */
private fun uploadProfileImage(
    uri: Uri,
    storage: FirebaseStorage,
    firestore: FirebaseFirestore,
    callback: (Boolean, String?, String?) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    if (user == null) {
        callback(false, "Usuario no autenticado.", null)
        return
    }

    // Crear una referencia única para la imagen
    val fileName = "profile_images/${user.uid}/${UUID.randomUUID()}.jpg"
    val storageRef = storage.reference.child(fileName)

    // Subir la imagen
    storageRef.putFile(uri)
        .addOnSuccessListener {
            // Obtener la URL de descarga
            storageRef.downloadUrl
                .addOnSuccessListener { downloadUri ->
                    // Actualizar el perfil del usuario en FirebaseAuth
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUri)
                        .build()

                    user.updateProfile(profileUpdates)
                        .addOnSuccessListener {
                            // Opcional: Guardar la URL en Firestore si deseas mantener un registro adicional
                            firestore.collection("users").document(user.uid)
                                .update("photoUrl", downloadUri.toString())
                                .addOnSuccessListener {
                                    callback(true, null, downloadUri.toString())
                                }
                                .addOnFailureListener { e ->
                                    Log.e("ProfileScreen", "Error al actualizar Firestore: ${e.message}")
                                    callback(true, "Foto de perfil actualizada, pero falló al actualizar Firestore.", downloadUri.toString())
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ProfileScreen", "Error al actualizar el perfil del usuario: ${e.message}")
                            callback(false, "Error al actualizar el perfil del usuario.", null)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileScreen", "Error al obtener la URL de descarga: ${e.message}")
                    callback(false, "Error al obtener la URL de descarga.", null)
                }
        }
        .addOnFailureListener { e ->
            Log.e("ProfileScreen", "Error al subir la imagen: ${e.message}")
            callback(false, "Error al subir la imagen.", null)
        }
}
