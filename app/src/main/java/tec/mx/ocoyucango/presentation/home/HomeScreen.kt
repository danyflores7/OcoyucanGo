// HomeScreen.kt
package tec.mx.ocoyucango.presentation.home

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import tec.mx.ocoyucango.presentation.common.BottomNavigationBar
import tec.mx.ocoyucango.presentation.common.TopBar
import tec.mx.ocoyucango.ui.theme.Green
import java.util.concurrent.TimeUnit
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import tec.mx.ocoyucango.data.model.Route
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth


private const val TAG = "HomeScreen"

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission") // Ya que verificamos los permisos
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Estado para manejar si los permisos se han otorgado
    val allPermissionsGranted = locationPermissionState.allPermissionsGranted
    Log.d(TAG, "Permisos otorgados: $allPermissionsGranted")

    // Solicitar permisos al iniciar
    LaunchedEffect(Unit) {
        Log.d(TAG, "Solicitando permisos de ubicación")
        locationPermissionState.launchMultiplePermissionRequest()
    }

    // Estado para la ubicación del usuario
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // Estado para el control de la cámara del mapa
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f) // Posición inicial genérica
    }

    // Estado para saber si el recorrido está iniciado
    var isRouteStarted by remember { mutableStateOf(false) }

    // Lista mutable para los puntos del recorrido
    val pathPoints = remember { mutableStateListOf<LatLng>() }

    // Estados para distancia y duración
    var distanceTraveled by remember { mutableStateOf(0.0) } // en metros
    var routeStartTime by remember { mutableStateOf<Long?>(null) }
    var routeDuration by remember { mutableStateOf(0L) } // en milisegundos

    // Callback de ubicación
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    userLocation = latLng
                    Log.d(TAG, "Ubicación actualizada: $latLng")

                    if (isRouteStarted) {
                        pathPoints.add(latLng)
                        Log.d(TAG, "Añadido punto al recorrido: $latLng")

                        if (pathPoints.size > 1) {
                            val prevLocation = pathPoints[pathPoints.size - 2]
                            val results = FloatArray(1)
                            Location.distanceBetween(
                                prevLocation.latitude,
                                prevLocation.longitude,
                                latLng.latitude,
                                latLng.longitude,
                                results
                            )
                            distanceTraveled += results[0]
                            Log.d(TAG, "Distancia recorrida actual: $distanceTraveled metros")
                        }
                    }
                } ?: run {
                    Log.e(TAG, "No se recibió ninguna ubicación en la actualización")
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                Log.d(TAG, "Disponibilidad de ubicación: ${locationAvailability.isLocationAvailable}")
            }
        }
    }

    // Iniciar o detener actualizaciones de ubicación
    LaunchedEffect(isRouteStarted, allPermissionsGranted) {
        if (allPermissionsGranted) {
            if (isRouteStarted) {
                Log.d(TAG, "Iniciando recorrido")
                routeStartTime = System.currentTimeMillis()
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 2000L
                ).build()
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    context.mainLooper
                )
            } else {
                Log.d(TAG, "Deteniendo recorrido")
                fusedLocationClient.removeLocationUpdates(locationCallback)
                routeStartTime = null
            }
        } else {
            Log.w(TAG, "Permisos de ubicación no otorgados")
        }
    }

    // Actualizar duración cada segundo
    LaunchedEffect(routeStartTime) {
        while (routeStartTime != null) {
            routeDuration = System.currentTimeMillis() - routeStartTime!!
            Log.d(TAG, "Duración del recorrido: ${formatDuration(routeDuration)}")
            delay(1000L)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(title = "Mapa", navController = navController)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Verificar permisos de ubicación
            if (allPermissionsGranted) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = true),
                    onMapLoaded = {
                        Log.d(TAG, "Mapa cargado exitosamente")
                    },
                    onMapClick = { latLng ->
                        Log.d(TAG, "Mapa clickeado en: $latLng")
                    },
                ) {
                    // Dibujar recorrido
                    if (pathPoints.isNotEmpty()) {
                        Polyline(
                            points = pathPoints.toList(),
                            color = Color.Red,
                            width = 5f
                        )
                        Log.d(TAG, "Polyline dibujada con ${pathPoints.size} puntos")
                    }
                }

                // Mover cámara a la ubicación del usuario
                LaunchedEffect(userLocation) {
                    userLocation?.let {
                        Log.d(TAG, "Actualizando cámara a la ubicación del usuario: $it")
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(it, 15f),
                            durationMs = 1000
                        )
                    }
                }
            } else {
                // Mostrar mensaje si los permisos no han sido otorgados
                Log.w(TAG, "Permisos de ubicación denegados")
                Text(
                    text = "Permisos de ubicación denegados. Por favor, actívalos en la configuración.",
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Mostrar distancia y duración cuando el recorrido está iniciado
            if (isRouteStarted) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                ) {
                    Text("Distancia recorrida: ${String.format("%.2f", distanceTraveled / 1000)} km")
                    Text("Duración: ${formatDuration(routeDuration)}")
                }
            }

            // Botón para iniciar/detener recorrido
            Button(
                onClick = {
                    Log.d(TAG, "Botón presionado: ${if (isRouteStarted) "Detener" else "Iniciar"} recorrido")
                    if (isRouteStarted) {
                        // Detener recorrido
                        isRouteStarted = false

                        // Obtener el UID del usuario actual
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        currentUser?.let { user ->

                            // Crear objeto Route
                            val route = Route(
                                userId = user.uid,
                                distanceMeters = distanceTraveled,
                                durationSeconds = TimeUnit.MILLISECONDS.toSeconds(routeDuration),
                                endTime = Timestamp.now()
                            )
                            Log.d(TAG, "Creando objeto Route: $route")

                            // Guardar en Firestore
                            Firebase.firestore.collection("routes")
                                .add(route)
                                .addOnSuccessListener {
                                    Log.i(TAG, "Recorrido guardado exitosamente con ID: ${it.id}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error al guardar el recorrido: ${e.message}", e)
                                }
                        } ?: run {
                            Log.e(TAG, "Usuario no autenticado al intentar guardar el recorrido")
                        }
                    } else {
                        // Iniciar recorrido
                        isRouteStarted = true
                        distanceTraveled = 0.0
                        routeDuration = 0L
                        pathPoints.clear()
                        Log.d(TAG, "Recorrido iniciado: distancia y duración reseteadas")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = if (isRouteStarted) "Detener" else "Iniciar recorrido")
            }
        }

        BottomNavigationBar(navController = navController)
    }
}

fun formatDuration(durationMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

