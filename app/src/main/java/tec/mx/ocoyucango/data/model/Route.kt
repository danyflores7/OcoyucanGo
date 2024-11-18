// data/model/Route.kt
package tec.mx.ocoyucango.data.model

import com.google.firebase.Timestamp

data class Route(
    var id: String = "", // Propiedad mutable para almacenar la ID del documento
    val compartido: Boolean = false, // Indica si el recorrido fue compartido
    val distancia: Int = 0, // Distancia recorrida en metros
    val duracion: Int = 0, // Duración en segundos
    val inicio: Timestamp = Timestamp.now(), // Timestamp del inicio del recorrido
    val fin: Timestamp? = null, // Timestamp del fin del recorrido
    val foto: String = "", // URL de la foto asociada (vacía por defecto)
    val puntos: Int = 0 // Puntos obtenidos en el recorrido
)
