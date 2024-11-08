// SpeciesViewModel.kt
package tec.mx.ocoyucango.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import tec.mx.ocoyucango.presentation.species.Species
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SpeciesViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _speciesList = MutableStateFlow<List<Species>>(emptyList())
    val speciesList: StateFlow<List<Species>> = _speciesList

    // Añadir un indicador para saber si los datos ya fueron cargados
    private var dataLoaded = false

    init {
        fetchSpecies()
    }

    private fun fetchSpecies() {
        // Verificar si los datos ya fueron cargados
        if (dataLoaded) {
            return
        }
        viewModelScope.launch {
            try {
                Log.d("SpeciesViewModel", "Fetching species from Firestore...")
                val result = firestore.collection("BibliotecaDeEspecies").get().await()
                Log.d("SpeciesViewModel", "Fetched ${result.documents.size} species documents.")
                val speciesList = result.documents.map { document ->
                    val id = document.id
                    val clase = document.getString("clase") ?: ""
                    val especie = document.getString("especie") ?: ""
                    val familia = document.getString("familia") ?: ""
                    val genero = document.getString("género") ?: ""
                    val orden = document.getString("orden") ?: ""

                    // Manejar 'nombre_común' que puede ser String o List<String>
                    val nombreComun = when (val nombreComunField = document.get("nombre_común")) {
                        is String -> listOf(nombreComunField)
                        is List<*> -> nombreComunField.filterIsInstance<String>()
                        else -> null
                    }

                    // Manejar 'sinónimo' que puede ser String o List<String>
                    val sinonimo = when (val sinonimoField = document.get("sinónimo")) {
                        is String -> listOf(sinonimoField)
                        is List<*> -> sinonimoField.filterIsInstance<String>()
                        else -> null
                    }

                    Species(
                        id = id,
                        clase = clase,
                        especie = especie,
                        familia = familia,
                        genero = genero,
                        nombreComun = nombreComun,
                        orden = orden,
                        sinonimo = sinonimo,
                        firstImageUrl = null
                    )
                }

                // Obtener la primera imagen para cada especie
                val speciesListWithImages = speciesList.map { speciesItem ->
                    val imageResult = firestore.collection("BibliotecaDeEspecies")
                        .document(speciesItem.id)
                        .collection("Imágenes")
                        .limit(1)
                        .get()
                        .await()

                    val imageUrl = imageResult.documents.firstOrNull()?.getString("url")
                    //Log.d("SpeciesViewModel", "Species ID: ${speciesItem.id}, Image URL: $imageUrl")
                    speciesItem.copy(firstImageUrl = imageUrl)

                }

                Log.d("SpeciesViewModel", "Species list size: ${speciesListWithImages.size}")
                _speciesList.value = speciesListWithImages

            } catch (e: Exception) {
                Log.e("SpeciesViewModel", "Error fetching species: ${e.message}", e)
            }
        }
    }
}
