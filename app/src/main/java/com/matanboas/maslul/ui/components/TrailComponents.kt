package com.matanboas.maslul.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ImageNotSupported // For placeholder when no images
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.matanboas.maslul.R // Ensure R.drawable.placeholder_image_loading and R.drawable.placeholder_image_error exist
import com.matanboas.maslul.models.Trail
import com.matanboas.maslul.network.Media
import com.matanboas.maslul.utils.translateText
import com.matanboas.maslul.ui.theme.*
import java.io.File


// ViewModel to hold and provide the Media utility (from previous correct version)
class TrailViewModel(applicationContext: Context) : ViewModel() {
    val mediaUtil = Media(cacheDir = File(applicationContext.cacheDir, "maslul_image_cache"))
}

// Factory for TrailViewModel to inject context (from previous correct version)
class TrailViewModelFactory(private val applicationContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrailViewModel(applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailCard(
    trail: Trail,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val applicationContext = LocalContext.current.applicationContext
    val trailViewModel: TrailViewModel = viewModel(factory = TrailViewModelFactory(applicationContext))

    var isVisible by remember { mutableStateOf(false) }
    // val view = LocalView.current // Not strictly needed for the simpler visibility check
    // val density = LocalDensity.current // Not strictly needed for the simpler visibility check

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .onGloballyPositioned { layoutCoordinates ->
                val windowPos = layoutCoordinates.positionInWindow()
                val cardHeight = layoutCoordinates.size.height
                // Consider screen height for a more robust check, especially with scrolling lists
                val screenHeight = applicationContext.resources.displayMetrics.heightPixels
                val currentlyVisible = windowPos.y + cardHeight > 0 && windowPos.y < screenHeight && layoutCoordinates.isAttached

                if (isVisible != currentlyVisible) {
                    isVisible = currentlyVisible
                    // Log.d("TrailCardVisibility", "${trail.name} visibility changed to: $isVisible")
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (trail.imageUrls.isNotEmpty()) {
                ImageCarousel(
                    imageUrls = trail.imageUrls,
                    media = trailViewModel.mediaUtil,
                    isVisible = isVisible
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ImageNotSupported, contentDescription = "No images available", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.weight(1f, fill = false),
                    text = trail.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    DifficultyIcon(difficulty = trail.difficulty) // Corrected version will be used
                    Spacer(modifier = Modifier.width(8.dp))
                    SuitabilityIcons(access = trail.access)
                }
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
                icon = Icons.Default.Forest,
                label = "סוג מסלול",
                value = translateText(trail.trailType),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ImageCarousel(imageUrls: List<String>, media: Media, isVisible: Boolean) {
    if (imageUrls.isEmpty()) {
        Log.d("ImageCarousel", "No image URLs provided, carousel will not render content.")
        return
    }

    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
                .clip(RoundedCornerShape(12.dp))
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            imageUrls.forEachIndexed { index, url ->
                var imagePath by remember(url) { mutableStateOf<String?>(null) }
                var isLoadingAttempted by remember(url) { mutableStateOf(false) }
                var errorOccurred by remember(url) { mutableStateOf(false) }

                LaunchedEffect(url, isVisible) {
                    if (url.isNotBlank() && isVisible && !isLoadingAttempted && imagePath == null) {
                        isLoadingAttempted = true
                        errorOccurred = false
                        // Log.d("ImageCarousel", "Attempting to load image: $url")
                        try {
                            imagePath = media.getImage(url)
                            // Log.d("ImageCarousel", "Image loaded for $url: $imagePath")
                        } catch (e: Exception) {
                            Log.e("ImageCarousel", "Error loading image $url: ${e.message}")
                            errorOccurred = true
                        }
                    }
                }

                val itemModifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(8.dp))

                when {
                    isVisible && !isLoadingAttempted && imagePath == null && !errorOccurred ->
                        Box(modifier = itemModifier.simpleShimmer())
                    isVisible && isLoadingAttempted && imagePath == null && !errorOccurred ->
                        Box(modifier = itemModifier.simpleShimmer())
                    imagePath != null && !errorOccurred ->
                        AsyncImage(
                            model = imagePath,
                            contentDescription = "תמונת מסלול ${index + 1}",
                            modifier = itemModifier,
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.placeholder),
                            error = painterResource(id = R.drawable.placeholder)
                        )
                    errorOccurred && isVisible ->
                        Box(
                            modifier = itemModifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.ErrorOutline, // Changed to ErrorOutline for error
                                contentDescription = "שגיאה בטעינת התמונה",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    else ->
                        Box(modifier = itemModifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)))
                }
            }
        }
    }
}

// Local data class to hold display properties for DifficultyIcon
private data class DifficultyDisplayInfo(
    val backgroundColor: Color,
    val iconColor: Color,
    val icon: ImageVector,
    val displayText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultyIcon(difficulty: String) {
    val colorScheme = MaterialTheme.colorScheme

    val displayInfo = when (difficulty) {
        "Easy" -> DifficultyDisplayInfo(
            backgroundColor = EasyGreen.copy(alpha = 0.8f),
            iconColor = colorScheme.onPrimaryContainer, // Adjusted for better contrast with potential green
            icon = Icons.Default.Hiking,
            displayText = "קל"
        )
        "Moderate" -> DifficultyDisplayInfo(
            backgroundColor = ModerateOrange.copy(alpha = 0.8f),
            iconColor = colorScheme.onSecondaryContainer, // Adjusted for better contrast
            icon = Icons.Default.Terrain,
            displayText = "בינוני"
        )
        "Challenging" -> DifficultyDisplayInfo(
            backgroundColor = ChallengingRed.copy(alpha = 0.8f),
            iconColor = colorScheme.onErrorContainer, // Adjusted for better contrast
            icon = Icons.Default.FilterHdr,
            displayText = "מאתגר"
        )
        else -> DifficultyDisplayInfo(
            backgroundColor = colorScheme.surfaceVariant,
            iconColor = colorScheme.onSurfaceVariant,
            icon = Icons.Default.HelpOutline,
            displayText = translateText(difficulty) // Fallback to translated original text
        )
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(translateText(difficulty)) } }, // Full difficulty text in tooltip
        state = rememberTooltipState()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(displayInfo.backgroundColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = displayInfo.icon,
                    contentDescription = null, // Tooltip provides description
                    tint = displayInfo.iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = displayInfo.displayText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = displayInfo.iconColor,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuitabilityIcons(access: List<String>) {
    val colorScheme = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        access.take(2).forEach { type ->
            // Using a local data class for SuitabilityIcon properties for consistency
            data class SuitabilityDisplayInfo(val icon: ImageVector, val tint: Color, val description: String)

            val displayInfo = when (type) {
                "Foot" -> SuitabilityDisplayInfo(Icons.AutoMirrored.Filled.DirectionsWalk, colorScheme.primary, "הליכה רגלית")
                "Bicycles" -> SuitabilityDisplayInfo(Icons.AutoMirrored.Filled.DirectionsBike, colorScheme.secondary, "אופניים")
                "All Vehicles" -> SuitabilityDisplayInfo(Icons.Default.DirectionsCar, colorScheme.tertiary, "רכב פרטי")
                "Off-Road Vehicles" -> SuitabilityDisplayInfo(Icons.Default.RvHookup, colorScheme.error, "רכב שטח 4x4")
                else -> SuitabilityDisplayInfo(Icons.Default.HelpOutline, colorScheme.onSurfaceVariant, translateText(type))
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = { PlainTooltip { Text(displayInfo.description) } },
                state = rememberTooltipState()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(displayInfo.tint.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = displayInfo.icon,
                        contentDescription = null,
                        tint = displayInfo.tint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            color = color,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
