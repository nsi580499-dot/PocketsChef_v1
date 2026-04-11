package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme

@Composable
fun MapScreen(navController: NavController) {
    val madrid = LatLng(40.416775, -3.703790)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(madrid, 12f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Elegant Unified Header
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
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.nearby_supermarkets),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = stringResource(R.string.find_stores),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    PocketsChefTheme { MapScreen(navController = rememberNavController()) }
}
