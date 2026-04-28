package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.navigation.NavGraph
import es.uc3m.android.pockets_chef_app.ui.components.ProfileStat
import es.uc3m.android.pockets_chef_app.ui.components.RecipeCard
import es.uc3m.android.pockets_chef_app.ui.components.UserAvatar
import es.uc3m.android.pockets_chef_app.ui.viewmodel.AuthViewModel
import es.uc3m.android.pockets_chef_app.ui.viewmodel.OtherChefViewModel

@Composable
fun OtherChefProfileScreen(
    navController: NavController,
    userId: String,
    viewModel: OtherChefViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val chef by viewModel.chefProfile.collectAsState()
    val recipes by viewModel.chefRecipes.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val myUid = authViewModel.getCurrentUserUid() ?: ""
    val isOwnProfile = myUid == userId
    val context = LocalContext.current

    // Request notification permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    LaunchedEffect(userId, myUid) {
        if (myUid.isNotEmpty()) {
            viewModel.loadChef(myUid, userId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (chef == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {

                // Gradient header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            ),
                            shape = RoundedCornerShape(bottomEnd = 32.dp, bottomStart = 32.dp)
                        )
                        .padding(top = 48.dp, bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        UserAvatar(
                            profileImageUrl = chef?.profileImageUrl,
                            modifier = Modifier.size(90.dp),
                            iconPadding = 20
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = chef!!.displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = chef!!.cookingLevel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Follow button
                        if (!isOwnProfile) {
                            Button(
                                onClick = { viewModel.toggleFollow(myUid, userId, context) },
                                colors = if (isFollowing) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.onPrimary,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                },
                                shape = RoundedCornerShape(24.dp),
                                border = if (isFollowing) {
                                    androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.onPrimary
                                    )
                                } else null
                            ) {
                                Icon(
                                    imageVector = if (isFollowing) Icons.Default.Check else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isFollowing) "Following" else "Follow Chef")
                            }
                        }
                    }
                }

                // Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStat(
                                stringResource(R.string.stats_recipes_cooked),
                                recipes.size.toString()
                            )
                            VerticalDivider(modifier = Modifier.height(40.dp))
                            ProfileStat("Followers", chef!!.followersCount.toString())
                        }
                    }

                    item {
                        Text(
                            text = "Chef's Recipes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (recipes.isEmpty()) {
                        item {
                            Text(
                                text = "No recipes shared yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(recipes) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onFavoriteToggle = { },
                                onClick = {
                                    navController.navigate(
                                        NavGraph.RecipeDetail.createRoute(recipe.id)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating back button — top left
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        // Floating home button — top right
        IconButton(
            onClick = {
                navController.navigate(NavGraph.Home.route) {
                    popUpTo(NavGraph.Home.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}