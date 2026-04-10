package es.uc3m.android.pockets_chef_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import es.uc3m.android.pockets_chef_app.navigation.NavGraph
import es.uc3m.android.pockets_chef_app.navigation.bottomNavItems
import es.uc3m.android.pockets_chef_app.ui.screens.*
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition { false }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketsChefTheme {
                PocketsChefApp()
            }
        }
    }
}

@Composable
fun PocketsChefApp() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide BottomBar
    val showBottomBar = currentRoute != NavGraph.Login.route && 
                        currentRoute != NavGraph.Signup.route &&
                        currentRoute != NavGraph.CookAI.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                PocketsChefBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        PocketsChefNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun PocketsChefBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(NavGraph.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(imageVector = screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) }
            )
        }
    }
}

@Composable
fun PocketsChefNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavGraph.Login.route,
        modifier = modifier
    ) {
        // --- LOGIN ROUTE ---
        composable(NavGraph.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavGraph.Home.route) {
                        popUpTo(NavGraph.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(NavGraph.Signup.route)
                }
            )
        }

        // --- SIGN UP ROUTE ---
        composable(NavGraph.Signup.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(NavGraph.Home.route) {
                        popUpTo(NavGraph.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // --- MAIN APP ROUTES ---
        composable(NavGraph.Home.route)    { HomeScreen(navController) }
        composable(NavGraph.Recipes.route) { RecipesScreen(navController) }
        composable(NavGraph.Pantry.route)  { PantryScreen(navController) }
        composable(NavGraph.Map.route)     { MapScreen(navController) }
        composable(NavGraph.Profile.route) { ProfileScreen(navController) }
        composable(NavGraph.CookAI.route)  { CookAIScreen(navController) }
    }
}
