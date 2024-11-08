// Species.kt
package tec.mx.ocoyucango.presentation.species

data class Species(
    val id: String,
    val clase: String,
    val especie: String,
    val familia: String,
    val genero: String,
    val nombreComun: List<String>?,
    val orden: String,
    val sinonimo: List<String>?,
    val firstImageUrl: String?
)
