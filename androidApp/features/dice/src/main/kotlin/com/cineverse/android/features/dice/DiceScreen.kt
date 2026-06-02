package com.cineverse.android.features.dice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cineverse.core.designsystem.components.CineVerseButton
import com.cineverse.core.designsystem.components.MovieCard
import com.cineverse.presentation.dice.DiceIntent
import com.cineverse.presentation.dice.DiceViewModel
import org.koin.compose.koinInject

@Composable
fun DiceScreen(
    onNavigateToDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiceViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    // Setup infinite rotation animation when rolling
    val infiniteTransition = rememberInfiniteTransition()
    val rollRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Movie Dice",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Can't decide what to watch? Let the dice choose!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        // Animated Dice section
        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer {
                    if (state.isRolling) {
                        rotationZ = rollRotation
                        rotationX = rollRotation * 0.5f
                        rotationY = rollRotation * 1.5f
                        cameraDistance = 8f * density
                    }
                }
                .clickable(enabled = !state.isRolling) {
                    viewModel.sendIntent(DiceIntent.RollDice)
                },
            contentAlignment = Alignment.Center
        ) {
            // Draw a beautiful custom 3D-like red cinema dice with 5 dots
            val primaryColor = MaterialTheme.colorScheme.primary
            val isDark = MaterialTheme.colorScheme.background.red < 0.5f
            val outlineColor = if (isDark) Color.White else Color.Black

            Canvas(modifier = Modifier.size(120.dp)) {
                // Background cube face
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
                
                // Border outline
                drawRoundRect(
                    color = outlineColor.copy(alpha = 0.3f),
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx())
                )

                // 5 Dots representing side five
                val r = 8.dp.toPx()
                val w = size.width
                val h = size.height
                
                // Center
                drawCircle(color = Color.White, radius = r, center = Offset(w / 2f, h / 2f))
                // Top-Left
                drawCircle(color = Color.White, radius = r, center = Offset(w / 4f, h / 4f))
                // Bottom-Right
                drawCircle(color = Color.White, radius = r, center = Offset(w * 3f / 4f, h * 3f / 4f))
                // Top-Right
                drawCircle(color = Color.White, radius = r, center = Offset(w * 3f / 4f, h / 4f))
                // Bottom-Left
                drawCircle(color = Color.White, radius = r, center = Offset(w / 4f, h * 3f / 4f))
            }
        }

        // Popup recommendation result
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = state.randomMovie != null && !state.isRolling,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f)
            ) {
                state.randomMovie?.let { movie ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clickable { onNavigateToDetails(movie.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your Match!",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.size(90.dp, 135.dp)) {
                                MovieCard(imageUrl = movie.posterPath, onClick = { onNavigateToDetails(movie.id) })
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = movie.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Rating: ★ ${movie.voteAverage}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = state.isRolling,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Selecting a masterpiece...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Trigger roll button
        CineVerseButton(
            text = if (state.isRolling) "Rolling..." else "Roll the Dice",
            onClick = { viewModel.sendIntent(DiceIntent.RollDice) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !state.isRolling
        )
    }
}
