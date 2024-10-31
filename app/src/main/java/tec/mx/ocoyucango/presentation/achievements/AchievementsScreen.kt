// AchievementsScreen.kt
package tec.mx.ocoyucango.presentation.achievements

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import tec.mx.ocoyucango.presentation.common.BottomNavigationBar
import tec.mx.ocoyucango.presentation.common.TopBar
import tec.mx.ocoyucango.R

@Composable
fun AchievementsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopBar(title = "Logros", navController = navController)

        // Contenido de logros
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            items(routeList) { route ->
                RouteItem(route)
            }
        }

        BottomNavigationBar(navController = navController)
    }
}

// Modelo de datos para rutas
data class Route(val name: String, val date: String, val description: String, val distance: String, val duration: String, val achievements: Int)

// Lista de rutas (ejemplo)
val routeList = listOf(
    Route("Carlos", "22 ago 2024 a las 7:15 p.m", "Ruta nocturna", "3.81 km", "32m 28s", 3),
    Route("Carlos", "22 ago 2024 a las 7:15 p.m", "Ruta por la tarde", "3.81 km", "32m 28s", 2),
    // Añade más rutas aquí
)

@Composable
fun RouteItem(route: Route) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.usuario),
                    contentDescription = "Usuario",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = route.name)
                    Text(text = route.date, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = route.description)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Distancia")
                    Text(route.distance)
                }
                Column {
                    Text("Duración")
                    Text(route.duration)
                }
                Column {
                    Text("Logros")
                    Row {
                        repeat(route.achievements) {
                            Icon(
                                painter = painterResource(id = R.drawable.logro),
                                contentDescription = "Logro",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartir",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
