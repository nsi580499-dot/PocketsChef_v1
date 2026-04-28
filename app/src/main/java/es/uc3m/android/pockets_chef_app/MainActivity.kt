package es.uc3m.android.pockets_chef_app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.*
import es.uc3m.android.pockets_chef_app.navigation.NavGraph
import es.uc3m.android.pockets_chef_app.navigation.bottomNavItems
import es.uc3m.android.pockets_chef_app.notifications.ExpiryWorker
import es.uc3m.android.pockets_chef_app.notifications.NotificationHelper
import es.uc3m.android.pockets_chef_app.ui.screens.*
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme
import es.uc3m.android.pockets_chef_app.ui.viewmodel.AuthViewModel
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("PocketsChef", "MainActivity onCreate")

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)

        // Schedule daily expiry check with WorkManager
        scheduleExpiryWorker()

        enableEdgeToEdge()
        setContent {
            PocketsChefTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PocketsChefApp()
                }
            }
        }
    }

    private fun scheduleExpiryWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val expiryWorkRequest = PeriodicWorkRequestBuilder<ExpiryWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ExpiryWorker.TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            expiryWorkRequest
        )
    }
}

@Composable
fun PocketsChefApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    val startDestinationState = androidx.compose.runtime.produceState<String?>(initialValue = null) {
        val uid = authViewModel.getCurrentUserUid()

        value = if (uid == null) {
            NavGraph.Login.route
        } else {
            val result = authViewModel.userRepository.getUserProfile(uid)
            val user = result.getOrNull()

            if (user?.profileCompleted == true) {
                NavGraph.Home.route
            } else {
                NavGraph.CompleteProfile.route
            }
        }
    }

    val startDestination = startDestinationState.value

    if (startDestination == null) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CircularProgressIndicator(
                modifier = Modifier.padding(32.dp)
            )
        }
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != null &&
            currentRoute != NavGraph.Login.route &&
            currentRoute != NavGraph.Signup.route &&
            currentRoute != NavGraph.CompleteProfile.route &&
            currentRoute != NavGraph.CookAI.route &&
            currentRoute != NavGraph.RecipeDetail.route &&
            currentRoute != NavGraph.EditProfile.route &&
            currentRoute != NavGraph.CreateRecipe.route &&
            !currentRoute.startsWith("cooking_steps") &&
            !currentRoute.startsWith("other_chef")

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
            startDestination = startDestination,
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
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
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

        composable(NavGraph.Signup.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(NavGraph.CompleteProfile.route) {
                        popUpTo(NavGraph.Signup.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavGraph.CompleteProfile.route) {
            CompleteProfileScreen(
                onProfileCompleted = {
                    navController.navigate(NavGraph.Home.route) {
                        popUpTo(NavGraph.CompleteProfile.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavGraph.Home.route)    { HomeScreen(navController) }
        composable(NavGraph.Recipes.route) { RecipesScreen(navController) }
        composable(NavGraph.Pantry.route)  { PantryScreen(navController) }
        composable(NavGraph.Map.route)     { MapScreen(navController) }
        composable(NavGraph.Profile.route) { ProfileScreen(navController) }
        composable(NavGraph.CookAI.route)  { CookAIScreen(navController) }
        composable(NavGraph.EditProfile.route) { EditProfileScreen(navController) }
        composable(NavGraph.CreateRecipe.route) { CreateRecipeScreen(navController) }
        composable(
            route = NavGraph.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            RecipeDetailScreen(navController, recipeId)
        }

        composable(
            route = NavGraph.CookingSteps.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            CookingStepsScreen(navController, recipeId)
        }

        composable(
            route = NavGraph.OtherChefProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            OtherChefProfileScreen(navController, userId)
        }
    }
}