package com.matanboas.maslul.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.matanboas.maslul.models.Trail
import com.matanboas.maslul.network.MediaManager
import com.matanboas.maslul.ui.components.TrailCard

@Composable
fun TrailFeed(trails: List<Trail>, context: Context) {
    var selectedTrail by remember { mutableStateOf<Trail?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trails) { trail ->
                TrailCard(
                    trail = trail,
                    onClick = { selectedTrail = trail },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        selectedTrail?.let { trail ->
            TrailDetailScreen(
                trail = trail,
                onDismiss = { selectedTrail = null }
            )
        }
    }
}