package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.data.model.User
import es.uc3m.android.pockets_chef_app.ui.viewmodel.AuthViewModel
import es.uc3m.android.pockets_chef_app.ui.viewmodel.OtherChefViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    otherChefViewModel: OtherChefViewModel = viewModel()
) {
    val myUid = authViewModel.getCurrentUserUid() ?: ""
    val userProfile by otherChefViewModel.chefProfile.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var cookingLevel by remember { mutableStateOf("Beginner") }
    val dietaryPreferences = remember { mutableStateListOf<String>() }

    val levels = listOf("Beginner", "Intermediate", "Pro Chef")
    val availableDiets = listOf("Vegetarian", "Vegan", "Gluten-Free", "Keto", "Paleo", "Low Carb")

    LaunchedEffect(myUid) {
        if (myUid.isNotEmpty()) {
            otherChefViewModel.loadChef(myUid, myUid)
        }
    }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            displayName = it.displayName
            bio = it.bio
            cookingLevel = it.cookingLevel
            dietaryPreferences.clear()
            dietaryPreferences.addAll(it.dietaryPreferences)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val updatedUser = userProfile?.copy(
                            displayName = displayName,
                            bio = bio,
                            cookingLevel = cookingLevel,
                            dietaryPreferences = dietaryPreferences.toList()
                        )
                        if (updatedUser != null) {
                            scope.launch {
                                val result = authViewModel.userRepository.updateUserProfile(updatedUser)
                                if (result.isSuccess) {
                                    navController.popBackStack()
                                } else {
                                    snackbarHostState.showSnackbar("Error updating profile")
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Styled Area
            Text(
                text = "Personal Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            HorizontalDivider()

            Text(
                text = "Cooking Journey",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                levels.forEach { level ->
                    FilterChip(
                        selected = cookingLevel == level,
                        onClick = { cookingLevel = level },
                        label = { Text(level) },
                        leadingIcon = if (cookingLevel == level) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            HorizontalDivider()

            Text(
                text = "Dietary Preferences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableDiets.forEach { diet ->
                    val isSelected = dietaryPreferences.contains(diet)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) dietaryPreferences.remove(diet)
                            else dietaryPreferences.add(diet)
                        },
                        label = { Text(diet) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val updatedUser = userProfile?.copy(
                        displayName = displayName,
                        bio = bio,
                        cookingLevel = cookingLevel,
                        dietaryPreferences = dietaryPreferences.toList()
                    )
                    if (updatedUser != null) {
                        scope.launch {
                            val result = authViewModel.userRepository.updateUserProfile(updatedUser)
                            if (result.isSuccess) {
                                navController.popBackStack()
                            } else {
                                snackbarHostState.showSnackbar("Error updating profile")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}
