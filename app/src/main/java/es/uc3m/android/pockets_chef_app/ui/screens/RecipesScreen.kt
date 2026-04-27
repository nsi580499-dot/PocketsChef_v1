package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.navigation.NavGraph
import es.uc3m.android.pockets_chef_app.ui.components.RecipeCard
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme
import es.uc3m.android.pockets_chef_app.ui.viewmodel.RecipeViewModel

@Composable
fun RecipesScreen(
    navController: NavController,
    viewModel: RecipeViewModel = viewModel()
) {
    // Observamos las recetas directamente del StateFlow del ViewModel
    val recipesList by viewModel.recipesState.collectAsState()

    // Filtramos localmente para respuesta inmediata en la UI
    val displayedRecipes = remember(recipesList, viewModel.showFavoritesOnly, viewModel.searchQuery) {
        val base = if (viewModel.showFavoritesOnly) recipesList.filter { it.isFavorite } else recipesList
        if (viewModel.searchQuery.isBlank()) base
        else base.filter { it.title.contains(viewModel.searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Elegant Unified Header
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
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.recipe_collection_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.searchQuery = it },
                    placeholder = { Text(stringResource(R.string.search_recipes_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }

        TabRow(
            selectedTabIndex = if (viewModel.showFavoritesOnly) 1 else 0,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[if (viewModel.showFavoritesOnly) 1 else 0]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = !viewModel.showFavoritesOnly,
                onClick = { viewModel.showFavoritesOnly = false },
                text = { Text(stringResource(R.string.all_recipes_tab), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = viewModel.showFavoritesOnly,
                onClick = { viewModel.showFavoritesOnly = true },
                text = { Text(stringResource(R.string.favorites_tab), fontWeight = FontWeight.Bold) }
            )
        }

        if (displayedRecipes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Si la lista está realmente vacía (no hay nada en Firestore)
                if (recipesList.isEmpty()) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = if (viewModel.showFavoritesOnly) stringResource(R.string.no_favorites_message) 
                               else stringResource(R.string.no_recipes_found_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onFavoriteToggle = { viewModel.toggleFavorite(recipe) },
                        onClick = {
                            navController.navigate(NavGraph.RecipeDetail.createRoute(recipe.id))
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipesScreenPreview() {
    PocketsChefTheme { RecipesScreen(navController = rememberNavController()) }
}
