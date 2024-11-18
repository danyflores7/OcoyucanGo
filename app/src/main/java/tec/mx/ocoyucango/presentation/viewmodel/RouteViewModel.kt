package tec.mx.ocoyucango.presentation.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tec.mx.ocoyucango.data.model.Route
import tec.mx.ocoyucango.utils.captureMapSnapshot
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val TAG = "RouteViewModel"

class RouteViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Estados del recorrido
    var isRouteStarted by mutableStateOf(false)
        private set

    var pathPoints = mutableStateListOf<LatLng>()
        private set

    var distanceTraveled by mutableDoubleStateOf(0.0) // en metros

    private var routeStartTime by mutableStateOf<Long?>(null)

    var routeDuration by mutableLongStateOf(0L) // en milisegundos
        private set

    private var mapBitmap by mutableStateOf<Bitmap?>(null)

    var googleMap: GoogleMap? = null

    private var routeJob: kotlinx.coroutines.Job? = null

    // Propiedad para almacenar la posición de la cámara
    var cameraPosition by mutableStateOf<LatLng?>(null)
        private set

    private var locationUpdatesActive = false

    fun startLocationUpdates() {
        if (locationUpdatesActive) return

        // Verificar permisos de ubicación
        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            Log.e(TAG, "Permisos de ubicación no otorgados. No se puede iniciar las actualizaciones de ubicación.")
            return
        }

        // Iniciar actualizaciones de ubicación
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        ).build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )
            locationUpdatesActive = true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException al solicitar actualizaciones de ubicación: ${e.message}", e)
            locationUpdatesActive = false
        }
    }

    fun stopLocationUpdates() {
        if (!locationUpdatesActive) return

        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationUpdatesActive = false
    }


    // Callback de ubicación
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                // Siempre actualizar la posición de la cámara
                cameraPosition = latLng

                if (isRouteStarted) {
                    // Agregar puntos al recorrido y calcular distancia
                    pathPoints.add(latLng)
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

                    // Dibujar polilínea en el mapa
                    googleMap?.let { map ->
                        map.clear()
                        map.addPolyline(
                            com.google.android.gms.maps.model.PolylineOptions()
                                .addAll(pathPoints)
                                .color(android.graphics.Color.RED)
                                .width(5f)
                        )
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



    fun startRoute() {
        if (isRouteStarted) {
            Log.w(TAG, "El recorrido ya está iniciado")
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "Usuario no autenticado")
            return
        }

        // Verificar permisos de ubicación
        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            Log.e(TAG, "Permisos de ubicación no otorgados. No se puede iniciar el recorrido.")
            return
        }

        isRouteStarted = true
        distanceTraveled = 0.0
        routeDuration = 0L
        pathPoints.clear()
        routeStartTime = System.currentTimeMillis()

        // Iniciar actualizaciones de ubicación
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        ).build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException al solicitar actualizaciones de ubicación: ${e.message}", e)
            isRouteStarted = false
            return
        }

        // Iniciar timer de 4 horas
        routeJob = viewModelScope.launch {
            while (isRouteStarted && routeDuration < TimeUnit.HOURS.toMillis(4)) {
                delay(1000L)
                routeDuration = System.currentTimeMillis() - (routeStartTime ?: System.currentTimeMillis())
                Log.d(TAG, "Duración del recorrido: ${formatDuration(routeDuration)}")
            }
            if (isRouteStarted) {
                // Si el tiempo límite se alcanza, detener el recorrido
                stopRoute(googleMap)
            }
        }
    }

    fun stopRoute(googleMap: GoogleMap?) {
        if (!isRouteStarted) {
            Log.w(TAG, "El recorrido no está iniciado")
            return
        }

        isRouteStarted = false
        routeJob?.cancel()
        routeStartTime = null

        fusedLocationClient.removeLocationUpdates(locationCallback)

        // Guardar el recorrido en Firestore
        viewModelScope.launch {
            saveRoute(googleMap)
        }
    }

    /**
     * Función suspendida para guardar el recorrido completo
     */
    private suspend fun saveRoute(googleMap: GoogleMap?) {
        if (googleMap == null) {
            Log.e(TAG, "GoogleMap es null, no se puede capturar la pantalla")
            saveRouteWithoutImage()
            return
        }

        // Capturar la captura de pantalla del mapa
        mapBitmap = captureMapSnapshot(googleMap)
        if (mapBitmap == null) {
            Log.e(TAG, "Captura de pantalla del mapa fallida")
            saveRouteWithoutImage()
            return
        } else {
            Log.d(TAG, "Captura de pantalla del mapa exitosa")
        }

        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val route = Route(
                distancia = distanceTraveled.toInt(), // Convertir Double a Int
                duracion = TimeUnit.MILLISECONDS.toSeconds(routeDuration).toInt(), // Mapear correctamente
                fin = Timestamp.now(),
                foto = "" // Se actualizará después de subir la imagen
            )
            Log.d(TAG, "Creando objeto Route: $route")

            // Subir la captura de pantalla a Firebase Storage
            mapBitmap?.let { bitmap ->
                Log.d(TAG, "Iniciando subida de la imagen al Storage")
                val storage = Firebase.storage
                val storageRef = storage.reference
                val imageRef =
                    storageRef.child("route_images/${user.uid}/${System.currentTimeMillis()}.png")

                // Convertir Bitmap a ByteArray
                val baos = java.io.ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val data = baos.toByteArray()

                // Subir la imagen
                imageRef.putBytes(data)
                    .addOnSuccessListener {
                        Log.d(TAG, "Imagen subida exitosamente")
                        // Obtener la URL de descarga
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            Log.d(TAG, "URL de la imagen obtenida: $uri")
                            // Actualizar el objeto Route con la URL de la imagen
                            val updatedRoute =
                                route.copy(foto = uri.toString())

                            // Guardar el Route actualizado en Firestore dentro de la subcolección 'rutas'
                            firestore.collection("Usuarios")
                                .document(user.uid)
                                .collection("rutas")
                                .add(updatedRoute)
                                .addOnSuccessListener {
                                    Log.i(
                                        TAG,
                                        "Recorrido guardado exitosamente con ID: ${it.id}"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.e(
                                        TAG,
                                        "Error al guardar el recorrido: ${e.message}",
                                        e
                                    )
                                }
                        }.addOnFailureListener { e ->
                            Log.e(
                                TAG,
                                "Error al obtener la URL de la imagen: ${e.message}",
                                e
                            )
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error al subir la imagen: ${e.message}", e)
                    }
            } ?: run {
                // Si no se pudo capturar el mapa, guardar el Route sin imagen
                Log.e(
                    TAG,
                    "mapBitmap es null, guardando el recorrido sin imagen"
                )
                saveRouteWithoutImage()
            }
        } ?: run {
            Log.e(TAG, "Usuario no autenticado al intentar guardar el recorrido")
        }
    }

    private fun saveRouteWithoutImage() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "Usuario no autenticado al intentar guardar el recorrido")
            return
        }

        val route = Route(
            distancia = distanceTraveled.toInt(), // Conversión a Int
            duracion = TimeUnit.MILLISECONDS.toSeconds(routeDuration).toInt(), // Esto ya está bien
            fin = Timestamp.now(),
            foto = "" // Se actualizará después de subir la imagen
        )


        firestore.collection("Usuarios")
            .document(currentUser.uid)
            .collection("rutas")
            .add(route)
            .addOnSuccessListener {
                Log.i(
                    TAG,
                    "Recorrido guardado exitosamente con ID: ${it.id}"
                )
            }
            .addOnFailureListener { e ->
                Log.e(
                    TAG,
                    "Error al guardar el recorrido: ${e.message}",
                    e
                )
            }
    }

    /**
     * Formatea la duración del recorrido de milisegundos a un formato legible (HH:mm:ss).
     */
    fun formatDuration(durationMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60

        return String.format(Locale.getDefault(),"%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        routeJob?.cancel()
    }
}
