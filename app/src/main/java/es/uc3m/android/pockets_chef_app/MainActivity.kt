package es.uc3m.android.pockets_chef_app


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import es.uc3m.android.pockets_chef_app.navigation.NavGraph
import es.uc3m.android.pockets_chef_app.navigation.bottomNavItems
import es.uc3m.android.pockets_chef_app.notifications.ExpiryWorker
import es.uc3m.android.pockets_chef_app.notifications.NotificationHelper
import es.uc3m.android.pockets_chef_app.ui.screens.CompleteProfileScreen
import es.uc3m.android.pockets_chef_app.ui.screens.CookAIScreen
import es.uc3m.android.pockets_chef_app.ui.screens.CookingStepsScreen
import es.uc3m.android.pockets_chef_app.ui.screens.CreateRecipeScreen
import es.uc3m.android.pockets_chef_app.ui.screens.EditProfileScreen
import es.uc3m.android.pockets_chef_app.ui.screens.HomeScreen
import es.uc3m.android.pockets_chef_app.ui.screens.LoginScreen
import es.uc3m.android.pockets_chef_app.ui.screens.MapScreen
import es.uc3m.android.pockets_chef_app.ui.screens.OtherChefProfileScreen
import es.uc3m.android.pockets_chef_app.ui.screens.PantryScreen
import es.uc3m.android.pockets_chef_app.ui.screens.ProfileScreen
import es.uc3m.android.pockets_chef_app.ui.screens.RecipeDetailScreen
import es.uc3m.android.pockets_chef_app.ui.screens.RecipesScreen
import es.uc3m.android.pockets_chef_app.ui.screens.SignUpScreen
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme
import es.uc3m.android.pockets_chef_app.ui.viewmodel.AuthViewModel
import java.util.concurrent.TimeUnit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import es.uc3m.android.pockets_chef_app.ui.screens.EditRecipeScreen
import es.uc3m.android.pockets_chef_app.ui.screens.NotificationsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        Log.d("PocketsChef", "MainActivity onCreate")

        NotificationHelper.createNotificationChannels(this)
        scheduleExpiryWorker()

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
            currentRoute != NavGraph.Notifications.route &&
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

    val sharedRecipeViewModel: es.uc3m.android.pockets_chef_app.ui.viewmodel.RecipeViewModel = viewModel()

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

        composable(NavGraph.Notifications.route) {
            NotificationsScreen(navController)
        }

        composable(
            route = NavGraph.EditRecipe.route, // This matches "edit_recipe/{recipeId}"
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            // Call the Stateful Edit Screen
            EditRecipeScreen(
                navController = navController,
                recipeId = recipeId,
                viewModel = sharedRecipeViewModel
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
        composable(NavGraph.Recipes.route) {
            RecipesScreen(navController, sharedRecipeViewModel) }
        composable(NavGraph.Pantry.route)  { PantryScreen(navController) }
        composable(NavGraph.Map.route)     { MapScreen(navController) }
        composable(NavGraph.Profile.route) {
            ProfileScreen(navController, recipeViewModel = sharedRecipeViewModel) }
        composable(NavGraph.CookAI.route)  { CookAIScreen(navController) }
        composable(NavGraph.EditProfile.route) { EditProfileScreen(navController) }
        composable(NavGraph.CreateRecipe.route) {
            CreateRecipeScreen(navController, sharedRecipeViewModel) }
        composable(
            route = NavGraph.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            RecipeDetailScreen(navController, recipeId, sharedRecipeViewModel)
        }

        composable(
            route = NavGraph.CookingSteps.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            CookingStepsScreen(navController, recipeId, sharedRecipeViewModel)
        }

        composable(
            route = NavGraph.OtherChefProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            OtherChefProfileScreen(navController, userId, recipeViewModel = sharedRecipeViewModel)
        }
    }
}
