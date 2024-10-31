// CameraScreen.kt
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import tec.mx.ocoyucango.data.remote.PlantNetApiClient
import tec.mx.ocoyucango.ui.theme.Green
import java.io.File
import tec.mx.ocoyucango.BuildConfig


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(navController: NavController) {
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current
    val cameraController = remember { LifecycleCameraController(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    var imageCaptured by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var identificationResult by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (permissionState.status.isGranted && imageCaptured == null) {
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
        if (permissionState.status.isGranted) {
            if (imageCaptured == null) {
                CameraPreview(cameraController, lifecycleOwner, Modifier.padding(paddingValues))
            } else {
                // Mostrar la imagen capturada y procesarla
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        bitmap = BitmapFactory.decodeFile(imageCaptured!!.absolutePath).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isProcessing) {
                        CircularProgressIndicator()
                        Text(text = "Procesando...")
                    } else if (identificationResult != null) {
                        Text(text = identificationResult!!)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            // Resetear para tomar otra foto
                            imageCaptured = null
                            identificationResult = null
                        }, colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                            Text(text = "Tomar otra foto", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    } else {
                        Button(onClick = {
                            // Procesar la imagen con la API de Pl@ntNet
                            isProcessing = true
                            val file = imageCaptured!!
                            val apiKey = BuildConfig.PLANTNET_API_KEY

                            coroutineScope.launch {
                                val result = identifyPlant(file, apiKey)
                                isProcessing = false
                                identificationResult = result
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                            Text(text = "Identificar", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        } else {
            Text(text = "Permiso de cámara denegado", modifier = Modifier.padding(paddingValues))
        }
    }
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
                Log.e("CameraScreen", "Error al capturar imagen: ${exception.message}", exception)
            }
        }
    )
}


@Composable
fun CameraPreview(
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
        Log.e("CameraScreen", "Error al identificar la planta: ${e.message}", e)
        "Error al conectar con el servicio de identificación."
    }
}