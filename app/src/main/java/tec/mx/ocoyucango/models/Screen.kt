package tec.mx.ocoyucango.models

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Especies : Screen("especies")
    object Mapa : Screen("mapa")
    object Logros : Screen("logros")
    object Notificaciones : Screen("notificaciones")
    object Usuario : Screen("usuario")
}
