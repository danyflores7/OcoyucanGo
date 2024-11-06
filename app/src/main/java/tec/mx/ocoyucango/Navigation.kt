// Navigation.kt
package tec.mx.ocoyucango

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import tec.mx.ocoyucango.presentation.achievements.AchievementsScreen
import tec.mx.ocoyucango.presentation.camera.CameraScreen
import tec.mx.ocoyucango.presentation.home.HomeScreen
import tec.mx.ocoyucango.presentation.login.LoginScreen
import tec.mx.ocoyucango.presentation.notifications.NotificationsScreen
import tec.mx.ocoyucango.presentation.profile.ProfileScreen
import tec.mx.ocoyucango.presentation.signup.SignUpScreen
import tec.mx.ocoyucango.presentation.species.SpeciesScreen
import tec.mx.ocoyucango.presentation.splash.SplashScreen

@Composable
fun Navigation(navController: NavHostController, auth: FirebaseAuth) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("login") {
            LoginScreen(navController, auth)
        }
        composable("signup") {
            SignUpScreen(navController, auth)
        }
        // Añade las nuevas rutas
        composable("species") {
            SpeciesScreen(navController)
        }
        composable("achievements") {
            AchievementsScreen(navController)
        }
        composable("camera") {
            CameraScreen()
        }
        composable("notifications") {
            NotificationsScreen(navController)
        }
        composable("profile") {
            ProfileScreen(navController)
        }
        // Ruta de inicio (Home)
        composable("home") {
            HomeScreen(navController)
        }
    }
}
