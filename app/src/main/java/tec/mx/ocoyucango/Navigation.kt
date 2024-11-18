// Navigation.kt
package tec.mx.ocoyucango

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import tec.mx.ocoyucango.presentation.achievements.AchievementsScreen
import tec.mx.ocoyucango.presentation.camera.CameraComparationScreen
import tec.mx.ocoyucango.presentation.camera.CameraScreen
import tec.mx.ocoyucango.presentation.home.HomeScreen
import tec.mx.ocoyucango.presentation.login.LoginScreen
import tec.mx.ocoyucango.presentation.notifications.NotificationsScreen
import tec.mx.ocoyucango.presentation.profile.ProfileScreen
import tec.mx.ocoyucango.presentation.signup.SignUpScreen
import tec.mx.ocoyucango.presentation.species.SpeciesDetailScreen
import tec.mx.ocoyucango.presentation.species.SpeciesScreen
import tec.mx.ocoyucango.presentation.splash.SplashScreen
import tec.mx.ocoyucango.presentation.viewmodel.RouteViewModel
import tec.mx.ocoyucango.presentation.viewmodel.SpeciesViewModel

@Composable
fun Navigation(
    navController: NavHostController,
    auth: FirebaseAuth,
    routeViewModel: RouteViewModel,
    speciesViewModel: SpeciesViewModel
) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController, routeViewModel, speciesViewModel)
        }
        composable("login") {
            LoginScreen(navController, auth, routeViewModel, speciesViewModel)
        }
        composable("signup") {
            SignUpScreen(navController, auth, routeViewModel, speciesViewModel)
        }
        composable("species") {
            SpeciesScreen(navController, routeViewModel, speciesViewModel)
        }

        // Ruta con parámetro speciesId
        composable("species_detail/{speciesId}") { backStackEntry ->
            val speciesId = backStackEntry.arguments?.getString("speciesId")
            speciesId?.let {
                SpeciesDetailScreen(
                    navController = navController,
                    speciesId = it,
                    routeViewModel = routeViewModel,
                    speciesViewModel = speciesViewModel
                )
            }
        }

        composable("achievements") {
            AchievementsScreen(navController, routeViewModel, speciesViewModel)
        }
        composable("camera") {
            CameraScreen(navController, routeViewModel, speciesViewModel)
        }

        // Ruta con parámetro speciesId para comparación de cámara
        composable("camera_comparation/{speciesId}") { backStackEntry ->
            val speciesId = backStackEntry.arguments?.getString("speciesId") ?: ""
            CameraComparationScreen(
                navController = navController,
                routeViewModel = routeViewModel,
                speciesViewModel = speciesViewModel,
                speciesId = speciesId
            )
        }

        composable("notifications") {
            NotificationsScreen(navController, routeViewModel, speciesViewModel)
        }
        composable("profile") {
            ProfileScreen(navController, routeViewModel, speciesViewModel)
        }
        composable("home") {
            HomeScreen(navController, routeViewModel, speciesViewModel)
        }
    }
}
