package com.matanboas.maslul.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matanboas.maslul.models.Trail
// Removed: import com.matanboas.maslul.network.MediaManager // No longer used here
import com.matanboas.maslul.network.TrailService
import com.matanboas.maslul.ui.components.TrailCard
import com.matanboas.maslul.ui.components.TrailCardPlaceholder
// Make sure TrailFeed is correctly defined or imported
// import com.matanboas.maslul.ui.components.TrailFeed // If TrailFeed is in components
import com.matanboas.maslul.ui.theme.MaslulTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG_HOME = "HomeScreen"

@Composable
fun HomeScreen(trailService: TrailService, context: Context /* Keep context if TrailFeed needs it */) {
    var trails by remember { mutableStateOf<List<Trail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // If TrailService is always the same instance or created higher up,
    // you might not need to re-instantiate it here.
    // This example assumes trailService is passed in.

    LaunchedEffect(key1 = Unit) {
        Log.d(TAG_HOME, "LaunchedEffect started for fetching trails.")
        isLoading = true
        errorMessage = null

        // Removed: launch(Dispatchers.IO) { MediaManager.cleanupOldImages(context) }
        // Cache cleanup is no longer handled here with the new Media class.
        // If general cache cleanup is needed, it should be a separate mechanism (e.g., WorkManager).

        // Fetch trails (TrailService instance should be provided or created appropriately)
        // val currentTrailService = trailService // Use the passed instance
        // Or if you need to create it here (less ideal if it can be hoisted/DI):
        // val currentTrailService = TrailService() // Firestore is default, no context needed now

        val result = trailService.getTrailsByCollection("ירושלים") // Example collection
        result.fold(
            onSuccess = { trailList ->
                Log.d(TAG_HOME, "Successfully fetched ${trailList.size} trails.")
                trails = trailList
                if (trailList.isEmpty()) {
                    Log.d(TAG_HOME, "No trails found in the collection.")
                    // errorMessage = "לא נמצאו מסלולים באזור זה." // Optional: set message for empty list
                }
            },
            onFailure = { exception ->
                Log.e(TAG_HOME, "Error fetching trails", exception)
                errorMessage = "שגיאה בטעינת המסלולים: ${exception.localizedMessage ?: "בעיה לא ידועה"}"
            }
        )
        isLoading = false
        Log.d(TAG_HOME, "Finished fetching trails. isLoading: $isLoading")
    }

    MaslulTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "שלום, מתן", // Consider making this dynamic if needed
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            isLoading -> "טוען מסלולים..."
                            errorMessage != null -> errorMessage!!
                            trails.isNotEmpty() -> "מסלולים באזור ירושלים" // Make dynamic based on query
                            else -> "לא נמצאו מסלולים. נסה אזור אחר או בדוק מאוחר יותר."
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }

                // Main content section
                if (isLoading) {
                    Log.d(TAG_HOME, "Showing placeholders.")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { // Add spacing for placeholders
                        repeat(3) {
                            TrailCardPlaceholder()
                        }
                    }
                } else if (trails.isNotEmpty()) {
                    Log.d(TAG_HOME, "Showing TrailFeed with ${trails.size} trails.")
                    // Assuming TrailFeed composable exists and is correctly defined to handle a list of Trails
                    // It will also need access to Context for the TrailViewModelFactory if TrailCards are created within it.
                    TrailFeed(trails = trails, context = context) // Pass context along
                } else {
                    // This case is now covered by the text above (isLoading=false, trails empty)
                    // If a more prominent message is needed:
                    if (errorMessage == null) { // Only show "no trails" if no error occurred
                        Text(
                            text = "לא נמצאו מסלולים באזור המבוקש.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                        )
                    }
                }
            }
        }
    }
}
