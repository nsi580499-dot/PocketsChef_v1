package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme

@Composable
fun ProfileScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {

        Surface(color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.profile_icon_desc),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.my_chef_profile),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.preferences_section),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    var notifications by remember { mutableStateOf(true) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.push_notifications_label), style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = notifications, onCheckedChange = { notifications = it })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    var expiryAlerts by remember { mutableStateOf(true) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.expiry_alerts_label), style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = expiryAlerts, onCheckedChange = { expiryAlerts = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.more_settings_coming_soon),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    PocketsChefTheme { ProfileScreen(navController = rememberNavController()) }
}
