// CommonComponents.kt
package tec.mx.ocoyucango.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

@Composable
fun SpeciesDetailTopBar(
    title: String,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícono de retroceso
        Image(
            painter = painterResource(id = R.drawable.arrow_back), // Asegúrate de tener el ícono en tus recursos
            contentDescription = "Regresar",
            modifier = Modifier
                .size(30.dp)
                .clickable {
                    navController.popBackStack()
                }
        )

        Spacer(modifier = Modifier.width(16.dp)) // Espacio entre el ícono de retroceso y el título

        // Título de la pantalla
        Text(
            text = title,
            fontSize = 18.sp,
            color = Green,
            modifier = Modifier.weight(1f)
        )
    }
}
