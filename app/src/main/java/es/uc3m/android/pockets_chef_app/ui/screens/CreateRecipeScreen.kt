package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.uc3m.android.pockets_chef_app.data.model.Ingredient
import es.uc3m.android.pockets_chef_app.data.model.RecipeStep
import es.uc3m.android.pockets_chef_app.ui.viewmodel.RecipeViewModel
import androidx.compose.runtime.saveable.rememberSaveable

private const val MAX_INGREDIENTS = 20
private const val MAX_STEPS = 15

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    navController: NavController,
    viewModel: RecipeViewModel = viewModel()
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var duration by rememberSaveable { mutableStateOf("") }
    var servings by rememberSaveable { mutableStateOf("1") }
    var category by rememberSaveable { mutableStateOf("") }
    var isPublic by rememberSaveable { mutableStateOf(true) }

    val ingredients = rememberSaveable {
        mutableStateListOf(
            Ingredient("", "")
        )
    }

    val steps = rememberSaveable {
        mutableStateListOf(
            RecipeStep(1, "")
        )
    }

    val createSuccess by viewModel.createRecipeSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(createSuccess) {
        if (createSuccess) {
            viewModel.clearCreateRecipeSuccess()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create recipe") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it },
                    label = { Text("Servings") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row {
                Checkbox(
                    checked = isPublic,
                    onCheckedChange = { isPublic = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Public recipe")
            }

            Text("Ingredients", style = MaterialTheme.typography.titleMedium)

            ingredients.forEachIndexed { index, ingredient ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row {
                            Text(
                                text = "Ingredient ${index + 1}",
                                modifier = Modifier.weight(1f)
                            )
                            if (ingredients.size > 1) {
                                IconButton(onClick = { ingredients.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = ingredient.name,
                            onValueChange = {
                                ingredients[index] = ingredient.copy(name = it)
                            },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = ingredient.amount,
                            onValueChange = {
                                ingredients[index] = ingredient.copy(amount = it)
                            },
                            label = { Text("Amount") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (ingredients.size < MAX_INGREDIENTS) {
                TextButton(
                    onClick = { ingredients.add(Ingredient("", "")) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add ingredient")
                }
            }

            Text("Steps", style = MaterialTheme.typography.titleMedium)

            steps.forEachIndexed { index, step ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row {
                            Text(
                                text = "Step ${index + 1}",
                                modifier = Modifier.weight(1f)
                            )
                            if (steps.size > 1) {
                                IconButton(onClick = {
                                    steps.removeAt(index)
                                    steps.indices.forEach { i ->
                                        steps[i] = steps[i].copy(order = i + 1)
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = step.description,
                            onValueChange = {
                                steps[index] = step.copy(description = it, order = index + 1)
                            },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            }

            if (steps.size < MAX_STEPS) {
                TextButton(
                    onClick = { steps.add(RecipeStep(steps.size + 1, "")) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add step")
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val cleanIngredients = ingredients.filter {
                        it.name.isNotBlank() && it.amount.isNotBlank()
                    }
                    val cleanSteps = steps.filter {
                        it.description.isNotBlank()
                    }.mapIndexed { index, item ->
                        item.copy(order = index + 1)
                    }

                    if (
                        title.isNotBlank() &&
                        description.isNotBlank() &&
                        duration.isNotBlank() &&
                        category.isNotBlank() &&
                        cleanIngredients.isNotEmpty() &&
                        cleanSteps.isNotEmpty()
                    ) {
                        viewModel.createRecipe(
                            title = title.trim(),
                            description = description.trim(),
                            duration = duration.trim(),
                            servings = servings.toIntOrNull() ?: 1,
                            category = category.trim(),
                            ingredients = cleanIngredients,
                            steps = cleanSteps,
                            isPublic = isPublic
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upload recipe")
            }
        }
    }
}