package es.uc3m.android.pockets_chef_app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

data class CircleConfig(
    val offsetX: Float,
    val offsetY: Float,
    val size: Float,
    val duration: Int,
    val delay: Int
)

private val circleConfigs = listOf(
    CircleConfig(offsetX = -0.3f, offsetY = -0.1f, size = 280f, duration = 3000, delay = 0),
    CircleConfig(offsetX = 0.6f,  offsetY = -0.2f, size = 200f, duration = 3500, delay = 500),
    CircleConfig(offsetX = -0.1f, offsetY = 0.6f,  size = 240f, duration = 4000, delay = 1000),
    CircleConfig(offsetX = 0.7f,  offsetY = 0.5f,  size = 180f, duration = 2800, delay = 300),
    CircleConfig(offsetX = 0.3f,  offsetY = 0.2f,  size = 160f, duration = 3200, delay = 700),
)

@Composable
fun PulsingCirclesBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        circleConfigs.forEach { config ->
            val scale by rememberInfiniteTransition(label = "scale${config.delay}").animateFloat(
                initialValue = 0.7f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = config.duration,
                        delayMillis = config.delay,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            val alpha by rememberInfiniteTransition(label = "alpha${config.delay}").animateFloat(
                initialValue = 0.04f,
                targetValue = 0.10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = config.duration,
                        delayMillis = config.delay,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alpha)
            ) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = (config.offsetX * 400).dp,
                            y = (config.offsetY * 800).dp
                        )
                        .size(config.size.dp)
                        .scale(scale)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}