// SpeciesDetailScreen.kt
package tec.mx.ocoyucango.presentation.species

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import tec.mx.ocoyucango.presentation.common.TopBar
import tec.mx.ocoyucango.presentation.common.BottomNavigationBar
import java.util.Locale


@Composable
fun SpeciesDetailScreen(navController: NavController, speciesId: String) {

    val firestore = FirebaseFirestore.getInstance()
    var species by remember { mutableStateOf<Species?>(null) }
    var images by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(speciesId) {
        try {
            // Obtener datos de la especie
            val document = firestore.collection("BibliotecaDeEspecies").document(speciesId).get().await()
            val clase = document.getString("clase") ?: ""
            val especie = document.getString("especie") ?: ""
            val familia = document.getString("familia") ?: ""
            val genero = document.getString("género") ?: ""
            // Manejar 'nombre_común' que puede ser String o List<String>
            val nombreComun = when (val nombreComunField = document.get("nombre_común")) {
                is String -> listOf(nombreComunField)
                is List<*> -> nombreComunField.filterIsInstance<String>()
                else -> null
            }
            val orden = document.getString("orden") ?: ""
            // Manejar 'sinónimo' que puede ser String o List<String>
            val sinonimo = when (val sinonimoField = document.get("sinónimo")) {
                is String -> listOf(sinonimoField)
                is List<*> -> sinonimoField.filterIsInstance<String>()
                else -> null
            }


            species = Species(
                id = speciesId,
                clase = clase,
                especie = especie,
                familia = familia,
                genero = genero,
                nombreComun = nombreComun,
                orden = orden,
                sinonimo = sinonimo,
                firstImageUrl = null
            )

            // Obtener imágenes
            val imageResult = firestore.collection("BibliotecaDeEspecies")
                .document(speciesId)
                .collection("Imágenes")
                .get()
                .await()

            val imageUrls = imageResult.documents.mapNotNull { it.getString("url") }
            images = imageUrls

        } catch (e: Exception) {
            Log.e("SpeciesDetailScreen", "Error fetching species details: ${e.message}", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopBar(title = "Detalle de Especie", navController = navController)

        if (species != null) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                // Título de la especie

                Text(
                    text = species!!.especie.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    },
                    fontSize = 28.sp,
                    color = Color(0xFF4CAF50) // Un verde suave
                )

                species!!.nombreComun?.let {
                    Text(
                        text = "(${it.joinToString(", ")})",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Información de clasificación en un estilo de lista
                Text(text = "Familia: ${species!!.familia}", fontSize = 16.sp, color = Color.DarkGray)
                Text(text = "Género: ${species!!.genero}", fontSize = 16.sp, color = Color.DarkGray)
                Text(text = "Clase: ${species!!.clase}", fontSize = 16.sp, color = Color.DarkGray)
                Text(text = "Orden: ${species!!.orden}", fontSize = 16.sp, color = Color.DarkGray)

                // Sinónimos
                species!!.sinonimo?.let {
                    val sinonimosText = it.joinToString(", ")
                    Text(
                        text = "Sinónimo(s): $sinonimosText",
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Título de Galería de imágenes
                Text(
                    text = "Galería de imágenes",
                    fontSize = 20.sp,
                    color = Color(0xFF4CAF50), // Un color verde similar
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Galería de imágenes
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp) // Espacio vertical
                ) {
                    items(images) { imageUrl ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp) // Espacio entre las imágenes
                                .size(350.dp) // Tamaño de cada imagen con borde y sombra incluidas
                                .clip(RoundedCornerShape(12.dp)) // Borde redondeado para toda la imagen
                                .border(10.dp, Color.LightGray, RoundedCornerShape(12.dp)) // Borde de la imagen
                                .shadow(6.dp, shape = RoundedCornerShape(12.dp)) // Sombra alrededor de la imagen
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)) // Asegura que la imagen también tenga el borde redondeado
                            )
                        }
                    }
                }




            }
        } else {
            // Indicador de carga
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        BottomNavigationBar(navController = navController)
    }

}
