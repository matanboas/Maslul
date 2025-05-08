package com.matanboas.maslul.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matanboas.maslul.models.Trail
import com.matanboas.maslul.network.Media
import com.matanboas.maslul.ui.components.DifficultyIcon
import com.matanboas.maslul.ui.components.ImageCarousel
import com.matanboas.maslul.ui.components.InfoRow
import com.matanboas.maslul.ui.components.SuitabilityIcons
import com.matanboas.maslul.utils.translateSeasons
import com.matanboas.maslul.utils.translateText
import java.io.File

@Composable
fun TrailDetailScreen(
    trail: Trail,
    onDismiss: () -> Unit
) {
    // Obtain context and create Media instance for image caching
    val context = LocalContext.current
    val media = remember { Media(File(context.cacheDir, "maslul_image_cache")) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with trail name and close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .weight(1f),
                    text = trail.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "סגור",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(28.dp)
                            .weight(0.1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Display ImageCarousel if there are image URLs
            if (trail.imageUrls.isNotEmpty()) {
                ImageCarousel(
                    imageUrls = trail.imageUrls,
                    media = media,
                    isVisible = true // Detail screen is fully visible
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Trail details card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DifficultyIcon(difficulty = trail.difficulty)
                        Spacer(modifier = Modifier.width(16.dp))
                        SuitabilityIcons(access = trail.access)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow(
                        icon = Icons.Default.Straighten,
                        label = "אורך",
                        value = "${trail.lengthKm} ק\"מ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    InfoRow(
                        icon = Icons.Default.Map,
                        label = "אזור",
                        value = translateText(trail.region),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    InfoRow(
                        icon = Icons.Default.DonutLarge,
                        label = "סוג מסלול",
                        value = translateText(trail.trailType),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    InfoRow(
                        icon = Icons.Default.Money,
                        label = "כניסה",
                        value = translateText(trail.entranceFee),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    InfoRow(
                        icon = Icons.Default.CalendarToday,
                        label = "עונות",
                        value = translateSeasons(trail.visitingSeasons),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    InfoRow(
                        icon = Icons.Default.Park,
                        label = "שמורת טבע",
                        value = translateText(trail.natureReserve),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Description card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = trail.description,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}