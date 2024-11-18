// HomeScreen.kt
package tec.mx.ocoyucango.presentation.home

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.PolylineOptions
import tec.mx.ocoyucango.presentation.common.BottomNavigationBar
import tec.mx.ocoyucango.presentation.common.TopBar
import tec.mx.ocoyucango.ui.theme.Green
import tec.mx.ocoyucango.presentation.viewmodel.RouteViewModel
import tec.mx.ocoyucango.presentation.viewmodel.SpeciesViewModel

private const val TAG = "HomeScreen"

/**
 * Función para manejar el ciclo de vida de MapView dentro de Compose.
 */
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    return mapView
}

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission") // Ya que verificamos los permisos
@Composable
fun HomeScreen(
    navController: NavController,
    routeViewModel: RouteViewModel,
    speciesViewModel: SpeciesViewModel
) {
    val locationPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val allPermissionsGranted = locationPermissionState.allPermissionsGranted
    Log.d(TAG, "Permisos otorgados: $allPermissionsGranted")

    // Solicitar permisos al iniciar
    LaunchedEffect(Unit) {
        Log.d(TAG, "Solicitando permisos de ubicación")
        locationPermissionState.launchMultiplePermissionRequest()

        // Configurar la geocerca
        val latitude = 19.432608 // Coordenadas del área restringida
        val longitude = -99.133209
        val radius = 500f // Radio en metros
        routeViewModel.setupGeofence(latitude, longitude, radius)
    }

    // Obtener referencia a MapView
    val mapView = rememberMapViewWithLifecycle()

    // Definir googleMap
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(title = "Mapa", navController = navController)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (allPermissionsGranted) {
                // Iniciar actualizaciones de ubicación
                DisposableEffect(Unit) {
                    routeViewModel.startLocationUpdates()
                    onDispose {
                        routeViewModel.stopLocationUpdates()
                    }
                }

                AndroidView(
                    factory = { _ ->
                        mapView.apply {
                            getMapAsync { map ->
                                googleMap = map
                                map.uiSettings.isMyLocationButtonEnabled = true
                                map.isMyLocationEnabled = true

                                // Restaurar la posición de la cámara si está disponible
                                routeViewModel.cameraPosition?.let { position ->
                                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
                                }
                            }
                        }
                    },
                    update = { _ ->
                        val pathPoints = routeViewModel.pathPoints.toList()
                        googleMap?.let { map ->
                            if (routeViewModel.isRouteStarted && pathPoints.isNotEmpty()) {
                                map.clear()
                                map.addPolyline(
                                    PolylineOptions()
                                        .addAll(pathPoints)
                                        .color(Color.Red.toArgb())
                                        .width(5f)
                                )
                            }
                        }
                    }
                )

                // Mover cámara a la ubicación del usuario cuando cambia la posición
                LaunchedEffect(routeViewModel.cameraPosition) {
                    routeViewModel.cameraPosition?.let { position ->
                        Log.d(TAG, "Actualizando cámara a la ubicación del usuario: $position")
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
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
            if (routeViewModel.isRouteStarted) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                ) {
                    Text(
                        "Distancia recorrida: ${String.format("%.2f", routeViewModel.distanceTraveled / 1000)} km",
                        color = Color.Black
                    )
                    Text(
                        "Duración: ${routeViewModel.formatDuration(routeViewModel.routeDuration)}",
                        color = Color.Black
                    )
                }
            }

            // Botón para iniciar/detener recorrido
            Button(
                onClick = {
                    if (routeViewModel.isInsideGeofence) {
                        Log.d(
                            TAG,
                            "Botón presionado: ${if (routeViewModel.isRouteStarted) "Detener" else "Iniciar"} recorrido"
                        )
                        if (routeViewModel.isRouteStarted) {
                            // Detener recorrido
                            routeViewModel.stopRoute(googleMap)
                        } else {
                            // Iniciar recorrido
                            routeViewModel.startRoute()
                        }
                    } else {
                        Log.w(TAG, "El usuario no está dentro de la geocerca. No se puede iniciar el recorrido.")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (routeViewModel.isRouteStarted) Color.Red else Green
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = if (routeViewModel.isRouteStarted) "Detener" else "Iniciar recorrido")
            }
        }

        BottomNavigationBar(navController = navController)
    }
}
