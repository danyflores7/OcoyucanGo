// NotificationsScreen.kt
package tec.mx.ocoyucango.presentation.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import tec.mx.ocoyucango.presentation.common.TopBar
import tec.mx.ocoyucango.R
import tec.mx.ocoyucango.presentation.common.BottomNavigationBar
import tec.mx.ocoyucango.presentation.viewmodel.RouteViewModel
import tec.mx.ocoyucango.presentation.viewmodel.SpeciesViewModel

@Composable
fun NotificationsScreen(
    navController: NavController,
    routeViewModel: RouteViewModel,
    speciesViewModel: SpeciesViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopBar(title = "Notificaciones", navController = navController)

        // Lista de notificaciones
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            items(notificationList) { notification ->
                NotificationItem(notification)
            }
        }
        BottomNavigationBar(navController = navController)
    }
}

// Modelo de datos para notificaciones
data class Notification(val title: String, val message: String, val date: String)

// Lista de notificaciones (ejemplo)
val notificationList = listOf(
    Notification("¡Listo!", "Tu ruta está lista para verse.", "22 ago 2024, 7:15 p.m"),
    Notification("¡Que siga la buena racha!", "Buen trabajo en tu última actividad.", "22 ago 2024, 7:15 p.m"),
    // Añade más notificaciones aquí
)

@Composable
fun NotificationItem(notification: Notification) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = notification.title, fontSize = 18.sp)
                Text(text = notification.message, fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.Gray)
                Text(text = notification.date, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)
            }
        }
    }
}
