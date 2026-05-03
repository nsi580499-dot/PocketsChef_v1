package es.uc3m.android.pockets_chef_app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

private val ingredients = listOf(
    "🧄", "🥕", "🫙", "🧅", "🌿", "🫐", "🥦", "🍋",
    "🧀", "🥚", "🫒", "🌶️", "🥬", "🍄", "🫚", "🧂"
)

private data class IngredientConfig(
    val emoji: String,
    val startX: Float,
    val startY: Float,
    val speed: Float,
    val size: Float,
    val delay: Int
)

@Composable
fun FloatingIngredientsBackground(modifier: Modifier = Modifier) {
    val configs = remember {
        List(14) { i ->
            IngredientConfig(
                emoji = ingredients[i % ingredients.size],
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                speed = 6000f + Random.nextFloat() * 6000f,
                size = 18f + Random.nextFloat() * 14f,
                delay = Random.nextInt(3000)
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        configs.forEach { config ->
            val offsetY by rememberInfiniteTransition(label = config.emoji + config.delay)
                .animateFloat(
                    initialValue = config.startY * 900f,
                    targetValue = -100f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = config.speed.toInt(),
                            delayMillis = config.delay,
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "offsetY"
                )

            val alpha by rememberInfiniteTransition(label = "alpha${config.delay}")
                .animateFloat(
                    initialValue = 0.1f,
                    targetValue = 0.35f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alpha)
            ) {
                Text(
                    text = config.emoji,
                    fontSize = config.size.sp,
                    modifier = Modifier.offset(
                        x = (config.startX * 380).dp,
                        y = offsetY.dp
                    )
                )
            }
        }
    }
}