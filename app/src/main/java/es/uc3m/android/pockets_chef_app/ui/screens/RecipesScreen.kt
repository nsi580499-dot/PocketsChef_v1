package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme
import es.uc3m.android.pockets_chef_app.ui.viewmodel.RecipeViewModel

@Composable
fun RecipesScreen(
    navController: NavController,
    viewModel: RecipeViewModel = viewModel()
) {
    Column(modifier = Modifier.fillMaxSize()) {

        Surface(color = MaterialTheme.colorScheme.primary) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = stringResource(R.string.recipe_collection_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        TabRow(selectedTabIndex = if (viewModel.showFavoritesOnly) 1 else 0) {
            Tab(
                selected = !viewModel.showFavoritesOnly,
                onClick = { viewModel.showFavoritesOnly = false },
                text = { Text(stringResource(R.string.all_recipes_tab)) }
            )
            Tab(
                selected = viewModel.showFavoritesOnly,
                onClick = { viewModel.showFavoritesOnly = true },
                text = { Text(stringResource(R.string.favorites_tab)) }
            )
        }

        if (viewModel.recipes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (viewModel.showFavoritesOnly) stringResource(R.string.no_favorites_message) 
                           else stringResource(R.string.no_recipes_found_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.recipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onFavoriteToggle = { viewModel.toggleFavorite(recipe) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, onFavoriteToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.recipe_duration_servings, recipe.duration, recipe.servings),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                SuggestionChip(
                    onClick = {},
                    label = { Text(recipe.category, style = MaterialTheme.typography.labelSmall) }
                )
            }
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (recipe.isFavorite) Icons.Default.Favorite
                                  else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(R.string.toggle_favorite_desc),
                    tint = if (recipe.isFavorite) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipesScreenPreview() {
    PocketsChefTheme { RecipesScreen(navController = rememberNavController()) }
}
