package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.data.model.Recipe
// import es.uc3m.android.pockets_chef_app.data.model.Ingredient // Ensure this is imported if needed!
import es.uc3m.android.pockets_chef_app.navigation.NavGraph
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme
import es.uc3m.android.pockets_chef_app.ui.viewmodel.RecipeViewModel

// 1. STATEFUL WRAPPER
// Handles ViewModel, Navigation, and finding the specific recipe.
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    recipeId: String,
    viewModel: RecipeViewModel = viewModel()
) {
    val recipesList by viewModel.recipesState.collectAsState()

    val recipe = remember(recipesList, recipeId) {
        recipesList.find { it.id == recipeId }
    }

    val isLoading = recipesList.isEmpty()

    RecipeDetailScreenContent(
        recipe = recipe,
        isLoading = isLoading,
        onBackClick = { navController.popBackStack() },
        onHomeClick = {
            navController.navigate(NavGraph.Home.route) {
                popUpTo(NavGraph.Home.route) { inclusive = true }
            }
        },
        onStartCookingClick = {
            navController.navigate(NavGraph.CookingSteps.createRoute(recipeId))
        }
    )
}

// 2. STATELESS CONTENT
// Renders the UI and passes interactions up via lambdas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreenContent(
    recipe: Recipe?,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onStartCookingClick: () -> Unit
) {
    Scaffold(
        topBar = {
            ElegantHeader(
                title = recipe?.title ?: stringResource(R.string.browse_recipes),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actionContent = {
                    IconButton(onClick = onHomeClick) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = stringResource(R.string.home),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (recipe == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(text = "Recipe not found", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Prevents overlapping with the ElegantHeader
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Image/Color area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = recipe.category,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    // Headline centered with fillMaxWidth()
                    Text(
                        text = recipe.title,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Text(
                        text = recipe.description,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Info Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InfoItem(icon = Icons.Default.Timer, label = recipe.duration)
                        InfoItem(icon = Icons.Default.Restaurant, label = "${recipe.servings} servings")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Ingredients Section
                    Text(
                        text = stringResource(R.string.ingredients_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    recipe.ingredients.forEach { ingredient ->
                        IngredientRow(ingredient.name, ingredient.amount)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Start Cooking Button
                    Button(
                        onClick = onStartCookingClick, // Wired up to the lambda!
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.start_cooking),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun IngredientRow(name: String, amount: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 3. PERFECT PREVIEW
@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview() {
    PocketsChefTheme {
        RecipeDetailScreenContent(
            recipe = Recipe(
                id = "1",
                title = "Classic Spaghetti Carbonara",
                category = "Italian",
                description = "A creamy, rich, and authentic Italian pasta dish made with just a few simple ingredients.",
                duration = "25 min",
                servings = 2,
                ingredients = listOf(
                    // Note: Update to match your actual Ingredient data class
                    es.uc3m.android.pockets_chef_app.data.model.Ingredient("Spaghetti", "200g"),
                    es.uc3m.android.pockets_chef_app.data.model.Ingredient("Guanciale", "100g"),
                    es.uc3m.android.pockets_chef_app.data.model.Ingredient("Pecorino Romano", "50g"),
                    es.uc3m.android.pockets_chef_app.data.model.Ingredient("Large Eggs", "2")
                ),
                steps = emptyList() // Not needed for this preview
            ),
            isLoading = false,
            onBackClick = {},
            onHomeClick = {},
            onStartCookingClick = {}
        )
    }
}