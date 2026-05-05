package es.uc3m.android.pockets_chef_app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.data.repository.ImageUploadHelper
import es.uc3m.android.pockets_chef_app.ui.components.UserAvatar
import es.uc3m.android.pockets_chef_app.ui.util.cookingLevels
import es.uc3m.android.pockets_chef_app.ui.viewmodel.UserProfileViewModel
import kotlinx.coroutines.launch
import java.io.File

fun createImageUri(context: Context): Uri {
    val imageFile = File(context.cacheDir, "images/captured_${System.currentTimeMillis()}.jpg")
    imageFile.parentFile?.mkdirs()
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var level by rememberSaveable { mutableStateOf("Beginner") }
    var photoUrl by rememberSaveable { mutableStateOf("") }
    var diet by rememberSaveable { mutableStateOf("") }
    var favoriteCuisine by rememberSaveable { mutableStateOf("") }
    var levelExpanded by rememberSaveable { mutableStateOf(false) }
    var hasInitialized by rememberSaveable { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            isUploadingImage = true
            scope.launch {
                val uploadResult = ImageUploadHelper.uploadUri(
                    context, cameraImageUri!!, "profile_images"
                )
                if (uploadResult.isSuccess) {
                    photoUrl = uploadResult.getOrNull() ?: photoUrl
                }
                isUploadingImage = false
            }
        }
    }

    // Camera permission launcher — requests permission then launches camera
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
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isUploadingImage = true
            scope.launch {
                val uploadResult = ImageUploadHelper.uploadUri(context, uri, "profile_images")
                if (uploadResult.isSuccess) {
                    photoUrl = uploadResult.getOrNull() ?: photoUrl
                }
                isUploadingImage = false
            }
        }
    }

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

    val nameError = if (name.trim().isBlank()) stringResource(R.string.name_required) else null
    val descriptionError =
        if (description.trim().length < 10) stringResource(R.string.desc_min_length) else null
    val isFormValid = nameError == null && descriptionError == null

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
                text = stringResource(R.string.edit_profile_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Profile image picker
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = stringResource(R.string.profile_photo_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    )
                } else {
                    UserAvatar(
                        profileImageUrl = null,
                        modifier = Modifier.size(100.dp),
                        iconPadding = 20
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isUploadingImage) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Text(
                        text = stringResource(R.string.uploading_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.camera_label))
                        }
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") }
                        ) {
                            Icon(
                                Icons.Default.Photo,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.gallery_label))
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name_label)) },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                supportingText = { if (nameError != null) Text(nameError) }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.desc_label)) },
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
                    label = { Text(stringResource(R.string.cooking_level_label)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
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
                value = diet,
                onValueChange = { diet = it },
                label = { Text(stringResource(R.string.diet_pref_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = favoriteCuisine,
                onValueChange = { favoriteCuisine = it },
                label = { Text(stringResource(R.string.fav_cuisine_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
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
                enabled = isFormValid && !isUploadingImage
            ) {
                Text(stringResource(R.string.save_changes_btn))
            }
        }
    }
}
