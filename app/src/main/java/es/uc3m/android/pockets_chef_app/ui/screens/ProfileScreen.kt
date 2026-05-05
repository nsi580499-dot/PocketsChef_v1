package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.navigation.NavGraph
import es.uc3m.android.pockets_chef_app.ui.components.FloatingIngredientsBackground
import es.uc3m.android.pockets_chef_app.ui.components.ProfileStat
import es.uc3m.android.pockets_chef_app.ui.components.RecipeCard
import es.uc3m.android.pockets_chef_app.ui.components.UserAvatar
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme
import es.uc3m.android.pockets_chef_app.ui.viewmodel.AuthViewModel
import es.uc3m.android.pockets_chef_app.ui.viewmodel.RecipeViewModel
import es.uc3m.android.pockets_chef_app.ui.viewmodel.UserProfileViewModel
import es.uc3m.android.pockets_chef_app.ui.components.InfoChip

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    userProfileViewModel: UserProfileViewModel = viewModel(),
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val userProfile by userProfileViewModel.profile.collectAsState()
    val myRecipes by recipeViewModel.myRecipes.collectAsState()

    LaunchedEffect(Unit) {
        userProfileViewModel.loadProfile()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingIngredientsBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.0f))
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
                            )
                        ),
                        shape = RoundedCornerShape(bottomEnd = 32.dp, bottomStart = 32.dp)
                    )
                    .padding(top = 48.dp, bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.clickable {
                            navController.navigate(NavGraph.EditProfile.route)
                        }
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.profile_icon_desc),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(20.dp)
                            )
                        }

                        UserAvatar(
                            profileImageUrl = userProfile.profileImageUrl,
                            modifier = Modifier.size(100.dp),
                            iconPadding = 20
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userProfile.displayName.ifBlank { stringResource(R.string.chef_fallback) },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = userProfile.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat(stringResource(R.string.my_recipes), myRecipes.size.toString())
                    VerticalDivider(
                        modifier = Modifier
                            .height(40.dp)
                            .padding(horizontal = 8.dp)
                    )
                    ProfileStat(stringResource(R.string.followers), userProfile.followersCount.toString())
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { navController.navigate(NavGraph.EditProfile.route) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.edit_profile_btn))
                }

                Spacer(modifier = Modifier.height(24.dp))

                ProfileSectionTitle(stringResource(R.string.about_me_section))
                Text(
                    text = userProfile.bio.ifEmpty { stringResource(R.string.chef_bio_placeholder) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileSectionTitle(stringResource(R.string.cooking_level_section))

                Spacer(modifier = Modifier.height(8.dp))

                InfoChip(
                    text = userProfile.cookingLevel.ifBlank { stringResource(R.string.cooking_level_beginner) },
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    textColor = Color.White
                )

                if (userProfile.favoriteCuisine.isNotBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileSectionTitle(stringResource(R.string.fav_cuisine_section))

                    Spacer(modifier = Modifier.height(8.dp))

                    InfoChip(
                        text = userProfile.favoriteCuisine,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        textColor = Color.White
                    )
                }

                if (userProfile.dietaryPreferences.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileSectionTitle(stringResource(R.string.diet_pref_section))

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        userProfile.dietaryPreferences.forEach { preference ->
                            InfoChip(
                                text = preference,
                                backgroundColor = MaterialTheme.colorScheme.tertiary,
                                textColor = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ProfileSectionTitle(stringResource(R.string.my_recipes))
                Spacer(modifier = Modifier.height(12.dp))

                if (myRecipes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_recipes_published),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        myRecipes.forEach { recipe ->
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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        authViewModel.logout()
                        navController.navigate(NavGraph.Login.route) { popUpTo(0) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.logout))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


@Composable
fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    PocketsChefTheme {
        ProfileScreen(navController = rememberNavController())
    }
}
