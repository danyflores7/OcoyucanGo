// CommonComponents.kt
package tec.mx.ocoyucango.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import tec.mx.ocoyucango.R
import tec.mx.ocoyucango.ui.theme.Green

@Composable
fun TopBar(
    title: String,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        // verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 30.sp,
            color = Green,
            modifier = Modifier.weight(1f)
        )
        Row {
            Image(
                painter = painterResource(id = R.drawable.foto),
                contentDescription = "Cámara",
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        navController.navigate("camera")
                    }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(id = R.drawable.notificacion),
                contentDescription = "Notificaciones",
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        navController.navigate("notifications")
                    }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(id = R.drawable.usuario),
                contentDescription = "Perfil",
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        navController.navigate("profile")
                    }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Green)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        // verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            iconRes = R.drawable.especie,
            description = "Especies",
            onClick = {
                navController.navigate("species")
            }
        )
        BottomNavItem(
            iconRes = R.drawable.mapa,
            description = "Mapa",
            onClick = {
                navController.navigate("home")
            }
        )
        BottomNavItem(
            iconRes = R.drawable.logros,
            description = "Logros",
            onClick = {
                navController.navigate("achievements")
            }
        )
    }
}

@Composable
fun BottomNavItem(
    iconRes: Int,
    description: String,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = description,
        modifier = Modifier
            .size(40.dp)
            .clickable { onClick() }
    )
}





//package tec.mx.ocoyucango.presentation.common
//
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import tec.mx.ocoyucango.R
//import tec.mx.ocoyucango.ui.theme.Green
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.font.FontWeight
//
//@Composable
//fun TopBar(navController: NavController) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 8.dp),  // Ajuste de padding para mejor distribución
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween // Esto asegura que los íconos estén alineados correctamente
//    ) {
//        // Título de la app
//        Text(
//            text = "OcoyucanGo",
//            color = Green,
//            fontSize = 34.sp,
//            fontWeight = FontWeight.Bold
//        )
//
//        // Contenedor para los íconos en la derecha
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(12.dp)  // Espaciado entre íconos
//        ) {
//            Icon(
//                    painter = painterResource(id = R.drawable.camera2),
//                    contentDescription = "Icono vectorial",
//                    modifier = Modifier.size(34.dp)
//                    .size(30.dp)  // Ajuste de tamaño del ícono
//                    .clickable {
//                        navController.navigate("camera")
//                    },
//
//            )
//            Icon(
//                painter = painterResource(id = R.drawable.circle_notifications_24),
//                contentDescription = "Icono vectorial",
//                modifier = Modifier.size(34.dp)
//                    .size(30.dp)  // Ajuste de tamaño del ícono
//                    .clickable {
//                        navController.navigate("notifications")
//                    },
//            )
//            Icon(
//                painter = painterResource(id = R.drawable.user_24),
//                contentDescription = "Icono vectorial",
//                modifier = Modifier.size(34.dp)
//                    .size(30.dp)  // Ajuste de tamaño del ícono
//                    .clickable {
//                        navController.navigate("profile")
//                    },
//            )
//        }
//    }
//}
//
//
//sealed class BottomNavItem(var title: String, var icon: ImageVector, var route: String) {
//    data object Species : BottomNavItem("Especies", Icons.Default.Nature, "species")
//    data object Map : BottomNavItem("Mapa", Icons.Default.Map, "home")
//    data object Achievements : BottomNavItem("Logros", Icons.Default.EmojiEvents, "achievements")
//}
//
//@Composable
//fun BottomNavigationBar(navController: NavController) {
//    val items = listOf(
//        BottomNavItem.Species,
//        BottomNavItem.Map,
//        BottomNavItem.Achievements
//    )
//
//    NavigationBar(
//        containerColor = MaterialTheme.colorScheme.surface
//    ) {
//        items.forEach { item ->
//            val isSelected = navController.currentDestination?.route == item.route
//            NavigationBarItem(
//                icon = {
//                    Icon(
//                        imageVector = item.icon,
//                        contentDescription = item.title
//                    )
//                },
//                label = { Text(text = item.title) },
//                selected = isSelected,
//                onClick = {
//                    navController.navigate(item.route) {
//                        // Evitar crear múltiples copias de la misma pantalla en el back stack
//                        launchSingleTop = true
//                        // Restaurar el estado cuando se selecciona un item
//                        restoreState = true
//                        // Evitar múltiples instancias del mismo destino
//                        popUpTo(navController.graph.startDestinationId) {
//                            saveState = true
//                        }
//                    }
//                },
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = Green,
//                    selectedTextColor = Green,
//                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            )
//        }
//    }
//}