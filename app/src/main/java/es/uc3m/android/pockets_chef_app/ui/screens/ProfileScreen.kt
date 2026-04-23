package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.ui.components.ProfileStat
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme
import es.uc3m.android.pockets_chef_app.ui.viewmodel.AuthViewModel
import es.uc3m.android.pockets_chef_app.ui.viewmodel.OtherChefViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    otherChefViewModel: OtherChefViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val myUid = authViewModel.getCurrentUserUid() ?: ""
    val userProfile by otherChefViewModel.chefProfile.collectAsState()
    val myRecipes by otherChefViewModel.chefRecipes.collectAsState()

    LaunchedEffect(myUid) {
        if (myUid.isNotEmpty()) {
            otherChefViewModel.loadChef(myUid)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Elegant Header with Profile Info
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
                .padding(top = 48.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.profile_icon_desc),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 4.dp
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.padding(6.dp).size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = userProfile?.displayName ?: "Chef",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = userProfile?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(stringResource(R.string.stats_recipes_cooked), myRecipes.size.toString())
                VerticalDivider(modifier = Modifier.height(40.dp).padding(horizontal = 8.dp))
                ProfileStat("Followers", userProfile?.followersCount?.toString() ?: "0")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Me
            ProfileSectionTitle(stringResource(R.string.about_me_section))
            Text(
                text = userProfile?.bio?.ifEmpty { stringResource(R.string.chef_bio_placeholder) } ?: stringResource(R.string.chef_bio_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Cooking Level
            ProfileSectionTitle(stringResource(R.string.cooking_level_section))
            val currentLevel = userProfile?.cookingLevel ?: "Beginner"
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val levels = listOf(
                    "Beginner" to R.string.cooking_level_beginner,
                    "Intermediate" to R.string.cooking_level_intermediate,
                    "Pro Chef" to R.string.cooking_level_pro
                )
                levels.forEach { (level, resId) ->
                    FilterChip(
                        selected = currentLevel == level,
                        onClick = { /* TODO: Update level in Firestore */ },
                        label = { Text(stringResource(resId)) },
                        leadingIcon = if (currentLevel == level) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Log out")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    PocketsChefTheme { ProfileScreen(navController = rememberNavController()) }
}
