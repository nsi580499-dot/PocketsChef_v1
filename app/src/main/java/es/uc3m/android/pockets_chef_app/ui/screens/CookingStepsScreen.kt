package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.data.model.RecipeStep
import es.uc3m.android.pockets_chef_app.navigation.NavGraph
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme
import es.uc3m.android.pockets_chef_app.ui.viewmodel.RecipeViewModel

// 1. STATEFUL WRAPPER
@Composable
fun CookingStepsScreen(
    navController: NavController,
    recipeId: String,
    viewModel: RecipeViewModel = viewModel()
) {
    val recipesList by viewModel.recipesState.collectAsState()

    val recipe = remember(recipesList, recipeId) {
        recipesList.find { it.id == recipeId }
    }

    val isLoading = recipesList.isEmpty()

    CookingStepsScreenContent(
        recipe = recipe,
        isLoading = isLoading,
        onBackClick = { navController.popBackStack() },
        onHomeClick = {
            navController.navigate(NavGraph.Home.route) {
                popUpTo(NavGraph.Home.route) { inclusive = true }
            }
        },
        onFinishClick = {
            navController.navigate(NavGraph.Home.route) {
                popUpTo(NavGraph.Home.route) { inclusive = true }
            }
        }
    )
}

// 2. STATELESS CONTENT
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingStepsScreenContent(
    recipe: Recipe?,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            ElegantHeader(
                title = recipe?.title ?: stringResource(R.string.browse_recipes),
                subtitle = recipe?.category ?: stringResource(R.string.lets_get_cooking),
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
                    Text(text = stringResource(R.string.recipe_not_found), style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else if (recipe.steps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.steps_not_available), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            val step = recipe.steps[currentStepIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Step Indicator
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.step_progress, step.order, recipe.steps.size),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Step Description
                    Text(
                        text = step.description,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            lineHeight = 36.sp
                        ),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Progress Indicator
                LinearProgressIndicator(
                    progress = { (currentStepIndex + 1).toFloat() / recipe.steps.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(horizontal = 24.dp),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // Navigation Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { if (currentStepIndex > 0) currentStepIndex-- },
                        enabled = currentStepIndex > 0,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.previous))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            if (currentStepIndex < recipe.steps.size - 1) {
                                currentStepIndex++
                            } else {
                                onFinishClick()
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        val isLast = currentStepIndex == recipe.steps.size - 1
                        Text(if (isLast) stringResource(R.string.finish) else stringResource(R.string.next))
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            if (isLast) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

// REUSABLE ELEGANT HEADER COMPONENT
@Composable
fun ElegantHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actionContent: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
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
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Optional Navigation Icon (e.g., Back button)
            if (navigationIcon != null) {
                navigationIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Title & Optional Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            // Optional Action Content (e.g., Home button)
            if (actionContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                ) {
                    actionContent()
                }
            }
        }
    }
}

// 3. PREVIEW
@Preview(showBackground = true)
@Composable
fun CookingStepsScreenPreview() {
    PocketsChefTheme {
        CookingStepsScreenContent(
            recipe = Recipe(
                id = "1",
                title = "Classic Spaghetti Carbonara",
                category = "Italian",
                steps = listOf(
                    RecipeStep(
                        order = 1,
                        description = "Boil a large pot of salted water and cook the spaghetti until al dente."
                    ),
                    RecipeStep(order = 2, description = "While the pasta cooks, fry the guanciale or pancetta in a skillet until crispy."),
                    RecipeStep(order = 3, description = "In a bowl, whisk together the eggs, grated Pecorino Romano, and black pepper.")
                )
            ),
            isLoading = false,
            onBackClick = {},
            onHomeClick = {},
            onFinishClick = {}
        )
    }
}
