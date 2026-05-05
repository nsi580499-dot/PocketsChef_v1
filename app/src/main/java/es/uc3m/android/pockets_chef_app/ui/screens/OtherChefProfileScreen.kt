package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import es.uc3m.android.pockets_chef_app.ui.viewmodel.RecipeViewModel

@Composable
fun OtherChefProfileScreen(
    navController: NavController,
    userId: String,
    viewModel: OtherChefViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val chef by viewModel.chefProfile.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val followUiOverride by viewModel.followUiOverride.collectAsState()
    val effectiveIsFollowing = followUiOverride ?: isFollowing
    val followActionInProgress by viewModel.followActionInProgress.collectAsState()
    val followStateLoaded by viewModel.followStateLoaded.collectAsState()
    val allRecipes by recipeViewModel.recipesState.collectAsState()

    val otherChefRecipes = allRecipes.filter { it.authorId == userId }

    val myUid = authViewModel.getCurrentUserUid() ?: ""
    val isOwnProfile = myUid == userId
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
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

                        if (!isOwnProfile) {
                            Button(
                                onClick = {
                                    viewModel.toggleFollow(myUid, userId, context)
                                },
                                enabled = followStateLoaded && !followActionInProgress,
                                colors = if (effectiveIsFollowing) {
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
                                border = if (effectiveIsFollowing) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary)
                                } else null
                            ) {
                                if (!followStateLoaded || followActionInProgress) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = if (effectiveIsFollowing) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (effectiveIsFollowing) Icons.Default.Check else Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = when {
                                        !followStateLoaded -> stringResource(R.string.loading_label)
                                        effectiveIsFollowing -> stringResource(R.string.followed_label)
                                        else -> stringResource(R.string.follow_chef_label)
                                    }
                                )
                            }
                        }
                    }
                }

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
                                otherChefRecipes.size.toString()
                            )
                            VerticalDivider(modifier = Modifier.height(40.dp))
                            ProfileStat(stringResource(R.string.followers), chef!!.followersCount.toString())
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.chef_recipes_title, chef!!.displayName),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (otherChefRecipes.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_recipes_shared),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(otherChefRecipes, key = { it.id }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onFavoriteToggle = { recipeViewModel.toggleFavorite(recipe) },
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

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

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
                contentDescription = stringResource(R.string.home),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
