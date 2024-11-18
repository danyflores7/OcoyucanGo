// AchievementsScreen.kt

package tec.mx.ocoyucango.presentation.achievements

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import tec.mx.ocoyucango.R
import tec.mx.ocoyucango.data.model.Route
import tec.mx.ocoyucango.presentation.common.BottomNavigationBar
import tec.mx.ocoyucango.presentation.common.TopBar
import tec.mx.ocoyucango.presentation.viewmodel.RouteViewModel
import tec.mx.ocoyucango.presentation.viewmodel.SpeciesViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun AchievementsScreen(
    navController: NavController,
    routeViewModel: RouteViewModel,
    speciesViewModel: SpeciesViewModel
) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var routes by remember { mutableStateOf<List<Route>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var totalPoints by remember { mutableStateOf(0) }
    var identificationPoints by remember { mutableStateOf(0) } // Puntos por identificaciones

    // Listener para actualizar puntos totales automáticamente desde Firestore
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            val userDocRef = firestore.collection("Usuarios").document(currentUser.uid)

            // Escuchar cambios en el documento del usuario
            userDocRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AchievementsScreen", "Error al escuchar cambios en Firestore: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val userData = snapshot.data
                    totalPoints = (userData?.get("puntos_usuario") as? Long)?.toInt() ?: 0
                }
            }

            try {
                // Obtener rutas
                val routesSnapshot = firestore.collection("Usuarios")
                    .document(currentUser.uid)
                    .collection("rutas")
                    .get()
                    .await()

                val fetchedRoutes = routesSnapshot.documents.mapNotNull { doc ->
                    val route = doc.toObject(Route::class.java)
                    if (route != null) {
                        route.id = doc.id
                    }
                    route
                }
                routes = fetchedRoutes

                // Obtener puntos por identificaciones
                val identificationsSnapshot = firestore.collection("Usuarios")
                    .document(currentUser.uid)
                    .collection("identificaciones")
                    .get()
                    .await()

                identificationPoints = identificationsSnapshot.documents.sumOf {
                    (it.get("puntos") as? Long)?.toInt() ?: 0
                }

                isLoading = false
            } catch (e: Exception) {
                Log.e("AchievementsScreen", "Error al recuperar datos: ${e.message}")
                isLoading = false
            }
        } else {
            Log.e("AchievementsScreen", "Usuario no autenticado")
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title = "Logros", navController = navController)

        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f) // Aquí limitamos el espacio del contenido principal
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (routes.isEmpty() && identificationPoints == 0) {
                Box(
                    modifier = Modifier
                        .weight(1f) // También limitado
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tienes logros registrados.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f) // La lista no ocupa más espacio del necesario
                        .fillMaxWidth()
                ) {
                    item {
                        // Tarjeta con puntos totales y puntos por identificaciones
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "Puntos Totales", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(text = totalPoints.toString(), fontSize = 24.sp, color = Color.Green)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Puntos por Identificaciones: $identificationPoints",
                                    fontSize = 16.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    items(routes) { route ->
                        RouteItem(route)
                    }
                }
            }
        }

        BottomNavigationBar(navController = navController)
    }

}

/**
 * Función para marcar una ruta como compartida en Firestore.
 *
 * @param route Ruta que se va a marcar como compartida.
 */
suspend fun markRouteAsShared(route: Route) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()

    if (currentUser != null) {
        try {
            // Actualizar la ruta en Firestore para marcarla como compartida
            firestore.collection("Usuarios")
                .document(currentUser.uid)
                .collection("rutas")
                .document(route.id)
                .update("compartido", true)
                .await()
            Log.d("AchievementsScreen", "Ruta marcada como compartida en Firestore.")
        } catch (e: Exception) {
            Log.e("AchievementsScreen", "Error al marcar ruta como compartida: ${e.message}", e)
        }
    } else {
        Log.e("AchievementsScreen", "Usuario no autenticado")
    }
}


@Composable
fun RouteItem(route: Route) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val imageLoader = ImageLoader.Builder(context).build()

    // Crear el launcher para manejar el resultado del Intent de compartir
    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                coroutineScope.launch {
                    // Marcar la ruta como compartida en Firestore
                    markRouteAsShared(route)
                }
            } else {
                Log.d("ShareRoute", "El usuario no completó el proceso de compartir.")
            }
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Foto del recorrido, si existe
                if (route.foto.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(route.foto),
                        contentDescription = "Foto del recorrido",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 8.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.usuario),
                        contentDescription = "Usuario",
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Recorrido realizado")
                    Text(
                        text = route.fin?.toDate()?.toString() ?: "Fecha no disponible",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Distancia: ${String.format(Locale.getDefault(), "%.2f", route.distancia / 1000.0)} km")
            Text(text = "Duración: ${formatDuration(route.duracion * 1000L)}")
            Text(text = "Puntos obtenidos en este recorrido: ${route.puntos}")
            Spacer(modifier = Modifier.height(8.dp))
            // Foto del recorrido
            if (route.foto.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(route.foto),
                    contentDescription = "Imagen del recorrido",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (route.compartido) {
                    Text(text = "Recorrido Compartido", color = Color.Gray)
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_share),
                        contentDescription = "Compartir",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                            .clickable {
                                coroutineScope.launch {
                                    shareRoute(context, route, imageLoader, shareLauncher)
                                }
                            },
                        tint = Color.Blue
                    )
                }
            }
        }
    }
}

/**
 * Función para compartir el recorrido en redes sociales.
 *
 * @param context Contexto de la aplicación.
 * @param route Objeto Route que se va a compartir.
 * @param imageLoader Coil ImageLoader para cargar la imagen.
 * @param shareLauncher Launcher para manejar el resultado del Intent de compartir.
 */
suspend fun shareRoute(
    context: Context,
    route: Route,
    imageLoader: ImageLoader,
    shareLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    val request = ImageRequest.Builder(context)
        .data(route.foto)
        .allowHardware(false)
        .build()

    try {
        val result = (imageLoader.execute(request) as? SuccessResult)?.drawable?.toBitmap()
        if (result != null) {
            val file = withContext(Dispatchers.IO) {
                val cachePath = File(context.cacheDir, "shared_images")
                cachePath.mkdirs()
                val file = File(cachePath, "shared_image_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { fos ->
                    result.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                file
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            shareLauncher.launch(Intent.createChooser(shareIntent, "Compartir recorrido vía"))
        } else {
            Log.e("ShareRoute", "Error al cargar la imagen del recorrido.")
        }
    } catch (e: Exception) {
        Log.e("ShareRoute", "Error al compartir recorrido: ${e.message}", e)
    }
}

/**
 * Formatea la duración del recorrido de milisegundos a un formato legible (HH:mm:ss).
 */
fun formatDuration(durationMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60

    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}
