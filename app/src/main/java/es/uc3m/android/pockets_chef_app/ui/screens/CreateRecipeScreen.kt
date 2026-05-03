package es.uc3m.android.pockets_chef_app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.data.model.Ingredient
import es.uc3m.android.pockets_chef_app.data.model.RecipeStep
import es.uc3m.android.pockets_chef_app.ui.viewmodel.RecipeViewModel

private const val MAX_INGREDIENTS = 20
private const val MAX_STEPS = 15

private val recipeCategories = listOf(
    "Breakfast", "Main", "Dessert", "Snack", "Drink"
)

@Composable
fun CreateRecipeScreen(
    navController: NavController,
    viewModel: RecipeViewModel = viewModel()
) {
    val createSuccess by viewModel.createRecipeSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(createSuccess) {
        if (createSuccess) {
            viewModel.clearCreateRecipeSuccess()
            navController.popBackStack()
        }
    }

    CreateRecipeScreenContent(
        errorMessage = errorMessage,
        onBackClick = { navController.popBackStack() },
        onUploadClick = { title, description, duration, servings, category, ingredients, steps, isPublic, imageUri ->
            viewModel.createRecipeWithOptionalImage(
                title = title,
                description = description,
                duration = duration,
                servings = servings,
                category = category,
                ingredients = ingredients,
                steps = steps,
                isPublic = isPublic,
                imageUri = imageUri
            )
        }
    )
}

@Composable
fun CreateRecipeScreenContent(
    errorMessage: String?,
    onBackClick: () -> Unit,
    onUploadClick: (
        title: String,
        description: String,
        duration: String,
        servings: Int,
        category: String,
        ingredients: List<Ingredient>,
        steps: List<RecipeStep>,
        isPublic: Boolean,
        imageUri: Uri?
    ) -> Unit
) {
    val context = LocalContext.current

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var duration by rememberSaveable { mutableStateOf("") }
    var servings by rememberSaveable { mutableStateOf("1") }
    var category by rememberSaveable { mutableStateOf("Breakfast") }
    var isPublic by rememberSaveable { mutableStateOf(true) }
    var categoryMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val ingredients = remember { mutableStateListOf(Ingredient("", "")) }
    val steps = remember { mutableStateListOf(RecipeStep(1, "")) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            selectedImageUri = cameraImageUri.toString()
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createImageUri(context)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // Gallery launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri?.toString()
    }

    val cleanIngredients = ingredients.filter {
        it.name.isNotBlank() && it.amount.isNotBlank()
    }

    val cleanSteps = steps.filter {
        it.description.isNotBlank()
    }.mapIndexed { index, item ->
        item.copy(order = index + 1)
    }

    val titleError =
        if (title.trim().isBlank()) stringResource(R.string.title_required) else null
    val descriptionError =
        if (description.trim().length < 10) stringResource(R.string.description_min_length) else null
    val durationError =
        if (duration.trim().isBlank()) stringResource(R.string.duration_required) else null
    val servingsInt = servings.toIntOrNull()
    val servingsError =
        if (servingsInt == null || servingsInt < 1) stringResource(R.string.servings_min_value) else null
    val ingredientsError =
        if (cleanIngredients.isEmpty()) stringResource(R.string.ingredient_required) else null
    val stepsError =
        if (cleanSteps.isEmpty()) stringResource(R.string.step_required) else null

    val isFormValid =
        titleError == null && descriptionError == null && durationError == null &&
                servingsError == null && ingredientsError == null && stepsError == null

    Scaffold(
        topBar = {
            ElegantHeader(
                title = stringResource(R.string.create_recipe_title),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.title_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = titleError != null,
                supportingText = { if (titleError != null) Text(titleError) }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                isError = descriptionError != null,
                supportingText = { if (descriptionError != null) Text(descriptionError) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text(stringResource(R.string.duration_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = durationError != null,
                    supportingText = { if (durationError != null) Text(durationError) }
                )
                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.servings_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = servingsError != null,
                    supportingText = { if (servingsError != null) Text(servingsError) }
                )
            }

            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.category_label)) },
                trailingIcon = {
                    IconButton(onClick = { categoryMenuExpanded = true }) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = { categoryMenuExpanded = false }
            ) {
                recipeCategories.forEach { item ->
                    val categoryResId = when (item) {
                        "Breakfast" -> R.string.category_breakfast
                        "Main"      -> R.string.category_main
                        "Dessert"   -> R.string.category_dessert
                        "Snack"     -> R.string.category_snack
                        "Drink"     -> R.string.category_drink
                        else        -> R.string.category_all
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(categoryResId)) },
                        onClick = {
                            category = item
                            categoryMenuExpanded = false
                        }
                    )
                }
            }

            // Image picker section
            Text(text = "Recipe image", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Camera")
                }
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gallery")
                }
            }

            selectedImageUri?.let { uriString ->
                AsyncImage(
                    model = uriString,
                    contentDescription = "Selected recipe image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isPublic, onCheckedChange = { isPublic = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.public_recipe_label))
            }

            Text(text = stringResource(R.string.ingredients_title), style = MaterialTheme.typography.titleMedium)

            ingredients.forEachIndexed { index, ingredient ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.ingredient_count_label, index + 1),
                                modifier = Modifier.weight(1f)
                            )
                            if (ingredients.size > 1) {
                                IconButton(onClick = { ingredients.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                                }
                            }
                        }
                        OutlinedTextField(
                            value = ingredient.name,
                            onValueChange = { ingredients[index] = ingredient.copy(name = it) },
                            label = { Text(stringResource(R.string.ingredient_name_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ingredient.amount,
                            onValueChange = { ingredients[index] = ingredient.copy(amount = it) },
                            label = { Text(stringResource(R.string.ingredient_amount_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            if (ingredientsError != null) {
                Text(text = ingredientsError, color = MaterialTheme.colorScheme.error)
            }

            if (ingredients.size < MAX_INGREDIENTS) {
                TextButton(onClick = { ingredients.add(Ingredient("", "")) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.add_ingredient_btn))
                }
            }

            Text(text = stringResource(R.string.steps_title), style = MaterialTheme.typography.titleMedium)

            steps.forEachIndexed { index, step ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.step_label, index + 1),
                                modifier = Modifier.weight(1f)
                            )
                            if (steps.size > 1) {
                                IconButton(onClick = {
                                    steps.removeAt(index)
                                    steps.indices.forEach { i ->
                                        steps[i] = steps[i].copy(order = i + 1)
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
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

            if (stepsError != null) {
                Text(text = stepsError, color = MaterialTheme.colorScheme.error)
            }

            if (steps.size < MAX_STEPS) {
                TextButton(onClick = { steps.add(RecipeStep(steps.size + 1, "")) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.add_step_btn))
                }
            }

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        onUploadClick(
                            title.trim(),
                            description.trim(),
                            duration.trim(),
                            servingsInt ?: 1,
                            category.trim(),
                            cleanIngredients,
                            cleanSteps,
                            isPublic,
                            selectedImageUri?.let { Uri.parse(it) }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text(stringResource(R.string.upload_recipe_btn))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}