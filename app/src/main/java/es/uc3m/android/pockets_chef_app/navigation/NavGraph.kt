package es.uc3m.android.pockets_chef_app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavGraph(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home    : NavGraph("home",    "Home",    Icons.Default.Home)
    data object Recipes : NavGraph("recipes", "Recipes", Icons.Default.Book)
    data object Pantry  : NavGraph("pantry",  "Pantry",  Icons.Default.Inventory2)
    data object Map     : NavGraph("map",     "Map",     Icons.Default.Map)
    data object Profile : NavGraph("profile", "Profile", Icons.Default.Person)
    data object Login   : NavGraph("login",   "Login",   Icons.Default.Person)
    data object Signup  : NavGraph("signup",  "Signup",  Icons.Default.Person)
}

val bottomNavItems = listOf(
    NavGraph.Home,
    NavGraph.Recipes,
    NavGraph.Pantry,
    NavGraph.Map,
    NavGraph.Profile
)
