// SpeciesScreen.kt
package tec.mx.ocoyucango.presentation.species

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import tec.mx.ocoyucango.presentation.common.BottomNavigationBar
import tec.mx.ocoyucango.presentation.common.TopBar
import tec.mx.ocoyucango.presentation.viewmodel.RouteViewModel
import tec.mx.ocoyucango.presentation.viewmodel.SpeciesViewModel
import coil.compose.rememberAsyncImagePainter
import tec.mx.ocoyucango.R

@Composable
fun SpeciesScreen(
    navController: NavController,
    routeViewModel: RouteViewModel,
    speciesViewModel: SpeciesViewModel
) {
    val speciesList by speciesViewModel.speciesList.collectAsState()
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    val filteredSpeciesList = speciesList.filter { species ->
        species.especie.contains(searchQuery.text, ignoreCase = true) ||
                (species.nombreComun?.any { it.contains(searchQuery.text, ignoreCase = true) } ?: false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopBar(title = "Especies", navController = navController)

        // Campo de texto para la búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar especies") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        if (filteredSpeciesList.isEmpty()) {
            // Mostrar indicador de carga
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Mostrar la lista filtrada de especies
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(filteredSpeciesList) { species ->
                    SpeciesItem(species, navController)
                }
            }
        }

        BottomNavigationBar(navController = navController)
    }
}

@Composable
fun SpeciesItem(species: Species, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                // Navegar a la pantalla de detalles de la especie
                navController.navigate("species_detail/${species.id}")
            }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            if (species.firstImageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(species.firstImageUrl),
                    contentDescription = species.especie,
                    modifier = Modifier.size(64.dp)
                )
                Log.d("SpeciesScreen", "Rendering species: ${species.especie}, Image URL: ${species.firstImageUrl}")

            } else {
                // Imagen de marcador de posición
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.floramarilla),
                    contentDescription = species.especie,
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = species.especie, fontSize = 18.sp)
                // Mostrar el primer nombre común si está disponible
                val nombreComunText = species.nombreComun?.joinToString(", ") ?: "Sin nombre común"
                Text(text = nombreComunText, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

