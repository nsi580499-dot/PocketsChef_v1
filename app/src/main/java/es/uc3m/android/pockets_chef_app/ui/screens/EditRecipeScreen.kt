package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.data.model.Ingredient
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.data.model.RecipeStep
import es.uc3m.android.pockets_chef_app.navigation.NavGraph
import es.uc3m.android.pockets_chef_app.ui.viewmodel.RecipeViewModel

// 1. STATEFUL WRAPPER
@Composable
fun EditRecipeScreen(
    navController: NavController,
    recipeId: String,
    viewModel: RecipeViewModel = viewModel()
) {
    val recipesList by viewModel.recipesState.collectAsState()
    val updateSuccess by viewModel.updateRecipeSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val deleteSuccess by viewModel.deleteRecipeSuccess.collectAsState()

    val recipe = remember(recipesList, recipeId) {
        recipesList.find { it.id == recipeId }
    }

    LaunchedEffect(updateSuccess, deleteSuccess) {
        if (updateSuccess || deleteSuccess) {
            viewModel.clearUpdateRecipeSuccess()
            viewModel.clearDeleteRecipeSuccess()
            // Navigate back to a safe screen
            navController.navigate(NavGraph.Home.route) {
                popUpTo(NavGraph.Home.route) { inclusive = true }
            }
        }
    }

    if (recipe == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        EditRecipeScreenContent(
            recipe = recipe,
            errorMessage = errorMessage,
            onBackClick = { navController.popBackStack() },
            onUpdateClick = { title, desc, dur, serv, cat, ing, steps, pub ->
                viewModel.updateRecipe(recipeId, title, desc, dur, serv, cat, ing, steps, pub)
            },
            onDeleteClick = { viewModel.deleteRecipe(recipeId) }
        )
    }
}

// 2. STATELESS CONTENT (Pre-filled form)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreenContent(
    recipe: Recipe,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onUpdateClick: (String, String, String, Int, String, List<Ingredient>, List<RecipeStep>, Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    var title by remember { mutableStateOf(recipe.title) }
    var description by remember { mutableStateOf(recipe.description) }
    var duration by remember { mutableStateOf(recipe.duration) }
    var servings by remember { mutableStateOf(recipe.servings.toString()) }
    var category by remember { mutableStateOf(recipe.category) }
    var isPublic by remember { mutableStateOf(recipe.isPublic) }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val ingredients = remember { mutableStateListOf(*recipe.ingredients.toTypedArray()) }
    val steps = remember { mutableStateListOf(*recipe.steps.toTypedArray()) }

    val cleanIngredients = ingredients.filter { it.name.isNotBlank() && it.amount.isNotBlank() }
    val cleanSteps = steps.filter { it.description.isNotBlank() }
        .mapIndexed { index, item -> item.copy(order = index + 1) }

    val isFormValid = title.isNotBlank() && description.isNotBlank() && duration.isNotBlank() &&
            servings.toIntOrNull() != null && cleanIngredients.isNotEmpty() && cleanSteps.isNotEmpty()

    Scaffold(
        topBar = {
            ElegantHeader(
                title = stringResource(R.string.edit_recipe_title),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actionContent = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // RENAME SECTION
            Text(stringResource(R.string.title_label), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.title_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // DESCRIPTION SECTION
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // DURATION AND SERVINGS SECTION
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text(stringResource(R.string.duration_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.servings_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // INGREDIENTS SECTION
            Text(stringResource(R.string.ingredients_title), style = MaterialTheme.typography.titleMedium)
            ingredients.forEachIndexed { index, ingredient ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = ingredient.name,
                        onValueChange = { ingredients[index] = ingredient.copy(name = it) },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.ingredient_name_placeholder)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = ingredient.amount,
                        onValueChange = { ingredients[index] = ingredient.copy(amount = it) },
                        modifier = Modifier.weight(0.5f),
                        label = { Text(stringResource(R.string.ingredient_amount_placeholder)) }
                    )
                    IconButton(onClick = { if (ingredients.size > 1) ingredients.removeAt(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                    }
                }
            }
            TextButton(onClick = { ingredients.add(Ingredient("", "")) }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(stringResource(R.string.add_ingredient_btn))
            }

            // STEPS SECTION
            Text(stringResource(R.string.steps_title), style = MaterialTheme.typography.titleMedium)
            steps.forEachIndexed { index, step ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = stringResource(R.string.step_label, index + 1), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            if (steps.size > 1) {
                                IconButton(onClick = { steps.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        OutlinedTextField(
                            value = step.description,
                            onValueChange = { steps[index] = step.copy(description = it, order = index + 1) },
                            label = { Text(stringResource(R.string.instructions_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            }

            TextButton(onClick = { steps.add(RecipeStep(steps.size + 1, "")) }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.add_step_btn))
            }

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (isFormValid) {
                        onUpdateClick(title, description, duration, servings.toInt(), category, cleanIngredients, cleanSteps, isPublic)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isFormValid
            ) {
                Text(stringResource(R.string.save_changes_btn))
            }

            // PERMANENT DELETE BUTTON
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.delete_recipe_btn))
            }
        }
    } // Scaffold End

    // Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_recipe_title)) },
            text = { Text(stringResource(R.string.delete_recipe_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick()
                    showDeleteConfirm = false
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}
