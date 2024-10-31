// SpeciesScreen.kt
package tec.mx.ocoyucango.presentation.species

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import tec.mx.ocoyucango.presentation.common.BottomNavigationBar
import tec.mx.ocoyucango.presentation.common.TopBar
import tec.mx.ocoyucango.R
@Composable
fun SpeciesScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopBar(title = "Especies", navController = navController)

        // Barra de búsqueda (opcional, puedes implementar la funcionalidad de búsqueda)
        // ...

        // Lista de especies
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            items(speciesList) { species ->
                SpeciesItem(species)
            }
        }

        BottomNavigationBar(navController = navController)
    }
}

// Modelo de datos para especies
data class Species(val imageRes: Int, val name: String, val info: String)

// Lista de especies (ejemplo)
val speciesList = listOf(
    Species(R.drawable.floramarilla, "Clematis campestris", "Información de la planta..."),
    Species(R.drawable.florroja, "Cosmos atrosanguineus", "Información de la planta..."),
    // Añade más especies aquí
)

@Composable
fun SpeciesItem(species: Species) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = species.imageRes),
                contentDescription = species.name,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = species.name, fontSize = 18.sp)
                Text(text = species.info, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
