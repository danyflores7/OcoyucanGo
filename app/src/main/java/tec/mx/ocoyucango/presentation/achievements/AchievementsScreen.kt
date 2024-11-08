// AchievementsScreen.kt
package tec.mx.ocoyucango.presentation.achievements

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import tec.mx.ocoyucango.presentation.common.BottomNavigationBar
import tec.mx.ocoyucango.presentation.common.TopBar
import tec.mx.ocoyucango.R
import tec.mx.ocoyucango.data.model.Route
import tec.mx.ocoyucango.utils.createShareableImage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import tec.mx.ocoyucango.presentation.viewmodel.RouteViewModel
import tec.mx.ocoyucango.presentation.viewmodel.SpeciesViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.Locale


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

    // Fetch routes from Firestore
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            try {
                val snapshot = firestore.collection("routes")
                    .whereEqualTo("userId", currentUser.uid) // Filtrar por userId
                    .get()
                    .await()
                val fetchedRoutes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Route::class.java)
                }
                routes = fetchedRoutes
                isLoading = false
                Log.d("AchievementsScreen", "Rutas recuperadas: ${routes.size}")
            } catch (e: Exception) {
                Log.e("AchievementsScreen", "Error fetching routes: ${e.message}", e)
                isLoading = false
            }
        } else {
            Log.e("AchievementsScreen", "Usuario no autenticado")
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(title = "Logros", navController = navController)

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (routes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tienes logros registrados.")
                }
            } else {
                // Contenido de logros
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(routes) { route ->
                        RouteItem(route)
                    }
                }
            }
        }

        BottomNavigationBar(navController = navController)
    }
}

@Composable
fun RouteItem(route: Route) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val imageLoader = ImageLoader.Builder(context)
        .build()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Imagen del logro desde Firebase Storage usando Coil
                route.imageUrl?.let { url ->
                    Image(
                        painter = rememberAsyncImagePainter(url),
                        contentDescription = "Logro",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 8.dp)
                    )
                } ?: run {
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
                        text = route.endTime?.toDate()?.toString() ?: "Fecha no disponible",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Distancia: ${String.format(Locale.getDefault(), "%.2f", route.distanceMeters / 1000)} km")
            Text(text = "Duración: ${formatDuration(route.durationSeconds * 1000)}")
            Spacer(modifier = Modifier.height(8.dp))
            // Mostrar la imagen del recorrido
            route.imageUrl?.let { url ->
                Image(
                    painter = rememberAsyncImagePainter(url),
                    contentDescription = "Imagen del recorrido",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Icono para compartir
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartir",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                        .clickable {
                            // Acción al hacer clic en el icono de compartir
                            coroutineScope.launch {
                                shareRoute(context, route, imageLoader)
                            }
                        },
                    tint = Color.Blue // Cambia el color si lo deseas
                )
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
 */
suspend fun shareRoute(context: Context, route: Route, imageLoader: ImageLoader) {
    // Cargar la imagen desde la URL como Bitmap usando Coil
    val request = ImageRequest.Builder(context)
        .data(route.imageUrl)
        .allowHardware(false) // Necesario para obtener el Bitmap
        .build()

    try {
        val result = (imageLoader.execute(request) as? SuccessResult)?.drawable?.toBitmap()
        if (result != null) {
            // Crear la imagen compuesta con las superposiciones
            val shareableBitmap = createShareableImage(
                context = context,
                mapBitmap = result,
                distance = String.format(Locale.getDefault(), "%.2f", route.distanceMeters / 1000),  // Agregando Locale.getDefault()
                duration = formatDuration(route.durationSeconds * 1000),
                date = route.endTime?.toDate()?.toString()
            )


            // Guardar el bitmap en el directorio de caché en un contexto de I/O
            val file = withContext(Dispatchers.IO) {
                val cachePath = File(context.cacheDir, "shared_images")
                cachePath.mkdirs() // Asegurar que el directorio exista
                val file = File(cachePath, "shared_image_${System.currentTimeMillis()}.png")

                FileOutputStream(file).use { fos ->
                    shareableBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }

                file // Retornar el archivo
            }

            // Obtener el Uri usando FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Crear el Intent de compartir
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Iniciar la actividad de compartir
            context.startActivity(Intent.createChooser(shareIntent, "Compartir recorrido via"))
        } else {
            Log.e(TAG, "Error al cargar la imagen del recorrido para compartir.")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Exception al compartir recorrido: ${e.message}", e)
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
