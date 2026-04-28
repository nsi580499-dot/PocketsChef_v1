package es.uc3m.android.pockets_chef_app.ui.screens

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.uc3m.android.pockets_chef_app.ui.viewmodel.UserProfileViewModel
import es.uc3m.android.pockets_chef_app.ui.util.cookingLevels

private fun isValidPhotoUrl(url: String): Boolean {
    return url.isBlank() || Patterns.WEB_URL.matcher(url).matches()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: UserProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var level by rememberSaveable { mutableStateOf("Beginner") }
    var photoUrl by rememberSaveable { mutableStateOf("") }
    var diet by rememberSaveable { mutableStateOf("") }
    var favoriteCuisine by rememberSaveable { mutableStateOf("") }

    var levelExpanded by rememberSaveable { mutableStateOf(false) }
    var hasInitialized by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(profile) {
        if (!hasInitialized && profile.uid.isNotBlank()) {
            name = profile.displayName
            description = profile.bio
            level = profile.cookingLevel.ifBlank { "Beginner" }
            photoUrl = profile.profileImageUrl
            diet = profile.dietaryPreferences.joinToString(", ")
            favoriteCuisine = profile.favoriteCuisine
            hasInitialized = true
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.clearSaveSuccess()
            navController.popBackStack()
        }
    }

    val nameError = if (name.trim().isBlank()) "Name is required" else null
    val descriptionError =
        if (description.trim().length < 10) "Description must be at least 10 characters" else null
    val photoUrlError =
        if (!isValidPhotoUrl(photoUrl.trim())) "Enter a valid URL" else null

    val isFormValid =
        nameError == null &&
                descriptionError == null &&
                photoUrlError == null

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Edit profile",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                supportingText = { if (nameError != null) Text(nameError) }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                isError = descriptionError != null,
                supportingText = { if (descriptionError != null) Text(descriptionError) }
            )

            ExposedDropdownMenuBox(
                expanded = levelExpanded,
                onExpandedChange = { levelExpanded = !levelExpanded }
            ) {
                OutlinedTextField(
                    value = level,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cooking level") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { levelExpanded = true }
                )

                ExposedDropdownMenu(
                    expanded = levelExpanded,
                    onDismissRequest = { levelExpanded = false }
                ) {
                    cookingLevels.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                level = item
                                levelExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = photoUrl,
                onValueChange = { photoUrl = it },
                label = { Text("Photo URL") },
                modifier = Modifier.fillMaxWidth(),
                isError = photoUrlError != null,
                supportingText = { if (photoUrlError != null) Text(photoUrlError) }
            )

            OutlinedTextField(
                value = diet,
                onValueChange = { diet = it },
                label = { Text("Diet preferences (comma separated)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = favoriteCuisine,
                onValueChange = { favoriteCuisine = it },
                label = { Text("Favourite cuisine") },
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    if (isFormValid) {
                        viewModel.saveProfile(
                            name = name.trim(),
                            description = description.trim(),
                            level = level.trim(),
                            photoUrl = photoUrl.trim(),
                            diet = diet.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            favoriteCuisine = favoriteCuisine.trim()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Save changes")
            }
        }
    }
}