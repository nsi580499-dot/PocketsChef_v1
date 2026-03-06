package es.uc3m.android.pockets_chef_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PocketsChefColorScheme = lightColorScheme(
    primary            = PrimaryDark,
    onPrimary          = SurfaceWhite,
    primaryContainer   = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary          = GoldAccent,
    onSecondary        = SurfaceWhite,
    background         = BackgroundGray,
    onBackground       = TextPrimary,
    surface            = SurfaceWhite,
    onSurface          = TextPrimary,
    surfaceVariant     = PrimaryLight,
    error              = ErrorRed,
    onError            = SurfaceWhite,
)

@Composable
fun PocketsChefTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PocketsChefColorScheme,
        content = content
    )
}
