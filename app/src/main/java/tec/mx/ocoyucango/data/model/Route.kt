// data/model/Route.kt
package tec.mx.ocoyucango.data.model

import com.google.firebase.Timestamp

data class Route(
    val userId: String = "",
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0L,
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp? = null,
    val imageUrl: String? = null // Nueva propiedad para la URL de la imagen
)
