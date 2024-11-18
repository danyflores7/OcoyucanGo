// CameraComparationScreen.kt
package tec.mx.ocoyucango.presentation.camera

import android.Manifest
import android.graphics.BitmapFactory
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import tec.mx.ocoyucango.BuildConfig
import tec.mx.ocoyucango.data.remote.PlantNetApiClient
import tec.mx.ocoyucango.ui.theme.Green
import tec.mx.ocoyucango.presentation.viewmodel.RouteViewModel
import tec.mx.ocoyucango.presentation.viewmodel.SpeciesViewModel
import java.io.File
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraComparationScreen(
    navController: NavController,
    routeViewModel: RouteViewModel,
    speciesViewModel: SpeciesViewModel,
    speciesId: String // Recibir speciesId como parámetro
) {
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current
    val cameraController = remember { LifecycleCameraController(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Obtener el nombre científico esperado usando speciesId
    val expectedScientificName = speciesViewModel.getSpeciesById(speciesId)?.especie

    // Variables de estado
    var imageCaptured by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var identificationResult by remember { mutableStateOf<String?>(null) }
    var isMatch by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Comparación de Especie") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (permissionState.status.isGranted && imageCaptured == null && isMatch != true) {
                FloatingActionButton(onClick = {
                    val executor = ContextCompat.getMainExecutor(context)
                    takePicture(cameraController, executor) { file ->
                        imageCaptured = file
                    }
                }, containerColor = Green) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Tomar foto",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { paddingValues ->
        if (expectedScientificName == null) {
            // Si el nombre científico esperado no está disponible, mostrar error y regresar
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: Nombre científico esperado no encontrado.")
            }
        } else {
            if (permissionState.status.isGranted) {
                if (imageCaptured == null) {
                    CameraPreview2(
                        cameraController,
                        lifecycleOwner,
                        Modifier.padding(paddingValues)
                    )
                } else {
                    // Contenido después de capturar la imagen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            bitmap = BitmapFactory.decodeFile(imageCaptured!!.absolutePath)
                                .asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (isProcessing) {
                            CircularProgressIndicator()
                            Text(text = "Procesando...")
                        } else if (identificationResult != null) {
                            Text(text = identificationResult!!)
                            Spacer(modifier = Modifier.height(16.dp))
                            if (isMatch == true) {
                                Text(
                                    text = "¡Especie identificada correctamente! Obtuviste 5 puntos",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        // Sumar 5 puntos al total
                                        coroutineScope.launch {
                                            addPointsForSpeciesIdentification(expectedScientificName, 5)
                                        }
                                        // Regresar a SpeciesDetailScreen
                                        navController.popBackStack()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                                ) {
                                    Text(
                                        text = "Regresar",
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            } else {
                                Text(
                                    text = "No coincide con la especie esperada. ¡Sigue intentando!",
                                    color = Color.Red,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        // Reiniciar para tomar otra foto
                                        imageCaptured = null
                                        identificationResult = null
                                        isMatch = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                                ) {
                                    Text(
                                        text = "Tomar otra foto",
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    // Procesar la imagen con la API de Pl@ntNet
                                    isProcessing = true
                                    val file = imageCaptured!!
                                    val apiKey = BuildConfig.PLANTNET_API_KEY

                                    coroutineScope.launch {
                                        val result = identifyPlant(file, apiKey)
                                        isProcessing = false
                                        identificationResult = result
                                        // Comparar los nombres científicos
                                        if (expectedScientificName != null) {
                                            val identifiedScientificName =
                                                getScientificNameFromResult(result)
                                            val normalizedExpectedName =
                                                speciesViewModel.normalizeName(
                                                    expectedScientificName
                                                )
                                            val normalizedIdentifiedName =
                                                speciesViewModel.normalizeName(
                                                    identifiedScientificName
                                                )

                                            val expectedSpeciesPart =
                                                extractSpeciesName(normalizedExpectedName)
                                            val identifiedSpeciesPart =
                                                extractSpeciesName(normalizedIdentifiedName)

                                            isMatch = expectedSpeciesPart == identifiedSpeciesPart
                                        }
                                        // Establecer el resultado de la comparación en el back stack anterior
                                        navController.previousBackStackEntry?.savedStateHandle?.set(
                                            "comparison_result",
                                            isMatch
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Green)
                            ) {
                                Text(
                                    text = "Identificar",
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Permiso de cámara denegado")
                }
            }
        }
    }
}


/**
 * Función para agregar puntos al usuario por la identificación exitosa de una especie.
 *
 * @param speciesName Nombre de la especie identificada.
 * @param pointsToAdd Cantidad de puntos a agregar al total del usuario.
 */
suspend fun addPointsForSpeciesIdentification(speciesName: String, pointsToAdd: Int) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    if (currentUser != null) {
        try {
            // Obtener el documento del usuario
            val userDocRef = firestore.collection("Usuarios").document(currentUser.uid)
            val userSnapshot = userDocRef.get().await()

            // Obtener puntos totales actuales
            var totalPoints = (userSnapshot.get("totalPoints") as? Long)?.toInt() ?: 0
            totalPoints += pointsToAdd

            // Actualizar puntos totales
            userDocRef.update("totalPoints", totalPoints).await()

            // Crear documento en la subcolección `identificaciones`
            val newIdentification = hashMapOf(
                "nombre" to speciesName,
                "fecha" to com.google.firebase.Timestamp.now(),
                "puntos" to pointsToAdd
            )
            userDocRef.collection("identificaciones").add(newIdentification).await()

            Log.d("CameraComparation", "Identificación guardada: $speciesName con $pointsToAdd puntos")

        } catch (e: Exception) {
            Log.e("CameraComparation", "Error al actualizar la identificación y puntos: ${e.message}", e)
        }
    } else {
        Log.e("CameraComparation", "Usuario no autenticado. No se pueden actualizar los puntos.")
    }
}


///**
// * Función para sumar puntos al total del usuario por la identificación exitosa de una especie.
// * Agrega la cantidad de puntos especificada (p. ej., 2).
// *
// * @param pointsToAdd Cantidad de puntos que se agregan al total del usuario.
// */
//suspend fun addPointsForSpeciesIdentification(pointsToAdd: Int) {
//    val firestore = FirebaseFirestore.getInstance()
//    val auth = FirebaseAuth.getInstance()
//    val currentUser = auth.currentUser
//
//    if (currentUser != null) {
//        try {
//            // Obtener los puntos totales actuales del usuario
//            val userDocumentSnapshot = firestore.collection("users").document(currentUser.uid).get().await()
//            val userData = userDocumentSnapshot.data
//            var totalPoints = (userData?.get("totalPoints") as? Long)?.toInt() ?: 0
//            var speciesIdentificationPoints = (userData?.get("speciesIdentificationPoints") as? Long)?.toInt() ?: 0
//
//            // Sumar los puntos
//            totalPoints += pointsToAdd
//            speciesIdentificationPoints += pointsToAdd
//
//            // Actualizar los puntos totales en Firestore
//            firestore.collection("users").document(currentUser.uid)
//                .update(
//                    mapOf(
//                        "totalPoints" to totalPoints,
//                        "speciesIdentificationPoints" to speciesIdentificationPoints
//                    )
//                )
//                .await()
//
//            Log.d("CameraComparation", "Se agregaron $pointsToAdd puntos por identificar una especie. Total: $totalPoints (Especies: $speciesIdentificationPoints)")
//
//        } catch (e: Exception) {
//            Log.e("CameraComparation", "Error al actualizar los puntos del usuario: ${e.message}", e)
//        }
//    } else {
//        Log.e("CameraComparation", "Usuario no autenticado. No se pueden actualizar los puntos.")
//    }
//}

@Composable
fun CameraPreview2(
    cameraController: LifecycleCameraController,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    modifier: Modifier = Modifier
) {
    cameraController.bindToLifecycle(lifecycleOwner)
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            val previewView = PreviewView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
            previewView.controller = cameraController
            previewView
        }
    )
}

private fun takePicture(
    cameraController: LifecycleCameraController,
    executor: java.util.concurrent.Executor,
    onImageCaptured: (File) -> Unit
) {
    val file = File.createTempFile("ocoyucango_image", ".jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
    cameraController.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onImageCaptured(file)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraComparation", "Error al capturar imagen: ${exception.message}", exception)
            }
        }
    )
}

private suspend fun identifyPlant(file: File, apiKey: String): String {
    val apiService = PlantNetApiClient.apiService

    // Crear MultipartBody.Part para la imagen
    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
    val imagePart = MultipartBody.Part.createFormData("images", file.name, requestFile)

    // Crear MultipartBody.Part para el órgano
    val organ = "auto"  // Puedes cambiarlo por "leaf", "flower", etc., si lo deseas
    val organPart = MultipartBody.Part.createFormData("organs", organ)

    // Crear la lista de partes
    val parts = listOf(imagePart, organPart)

    return try {
        val response = apiService.identifyPlant(
            apiKey = apiKey,
            lang = "es", // Enviar "es" para español
            parts = parts
        )

        if (response.isSuccessful) {
            val identificationResponse = response.body()
            if (identificationResponse != null && identificationResponse.results.isNotEmpty()) {
                val bestResult = identificationResponse.results.first()
                val speciesName = bestResult.species.scientificNameWithoutAuthor
                val commonNames = bestResult.species.commonNames?.joinToString(", ") ?: "No disponible"
                val confidence = (bestResult.score * 100).toInt()
                "Especie: $speciesName\nNombres comunes: $commonNames\nConfianza: $confidence%"
            } else {
                "No se pudo identificar la planta."
            }
        } else {
            "Error en la identificación: ${response.errorBody()?.string()}"
        }
    } catch (e: Exception) {
        Log.e("CameraComparation", "Error al identificar la planta: ${e.message}", e)
        "Error al conectar con el servicio de identificación."
    }
}

private fun getScientificNameFromResult(result: String): String {
    // Extraer el nombre científico del resultado
    // Suponiendo que el formato es:
    // "Especie: [scientificName]\nNombres comunes: ...\nConfianza: ...%"
    return result.split("\n").firstOrNull()?.substringAfter("Especie: ")?.trim() ?: ""
}

private fun extractSpeciesName(fullName: String): String {
    return fullName.split(" ").lastOrNull()?.lowercase(Locale.getDefault()) ?: ""
}
