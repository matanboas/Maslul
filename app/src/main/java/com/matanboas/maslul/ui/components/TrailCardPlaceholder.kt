package com.matanboas.maslul.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip

// Shimmer modifier (Composable) for animating a light sweep
typealias ColorsList = List<Color>

@Composable
fun Modifier.simpleShimmer(
    shimmerColors: ColorsList = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ),
    durationMillis: Int = 1000,
    dropOff: Float = 0.5f
): Modifier {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    return this.then(
        Modifier.drawWithContent {
            val widthPx = size.width
            val startX = (translateAnim - dropOff) * widthPx * 1.4f
            val endX = (translateAnim + dropOff) * widthPx * 1.4f

            val brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(startX, 0f),
                end = Offset(endX, 0f)
            )

            drawContent()
            drawRect(brush = brush, blendMode = BlendMode.SrcIn)
        }
    )
}

@Composable
fun TrailCardPlaceholder(
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .simpleShimmer() // shimmer on this Box
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .simpleShimmer()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                )
                                .simpleShimmer()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info Rows Placeholder
            repeat(3) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                            )
                            .simpleShimmer() // shimmer on icon placeholder
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(16.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .simpleShimmer() // shimmer on label placeholder
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                            )
                            .simpleShimmer() // shimmer on value placeholder
                    )
                }
            }
        }
    }
}
