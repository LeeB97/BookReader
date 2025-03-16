package com.leeb.bookreader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.leeb.bookreader.R

@Composable
fun ControlBar(
    isSpeaking: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onStop: () -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit
) {
    // Use Surface to add elevation and make the control bar stand out
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(10f), // Ensure it's above other content
        shadowElevation = 8.dp, // Increased elevation for bottom bar
        color = Color.DarkGray.copy(alpha = 0.95f) // Increased opacity for better visibility
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp), // Increased padding for better touch targets
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(48.dp) // Larger touch target
            ) {
                Icon(
                    painterResource(id = R.drawable.round_skip_previous_24),
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp) // Larger icon
                )
            }
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(56.dp) // Larger touch target for primary action
            ) {
                Icon(
                    painterResource(id = if (isSpeaking) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24),
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp) // Larger icon
                )
            }
            IconButton(
                onClick = onNext,
                modifier = Modifier.size(48.dp) // Larger touch target
            ) {
                Icon(
                    painterResource(id = R.drawable.round_skip_next_24),
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp) // Larger icon
                )
            }
            IconButton(
                onClick = onStop,
                modifier = Modifier.size(48.dp) // Larger touch target
            ) {
                Icon(
                    painterResource(id = R.drawable.round_stop_24),
                    contentDescription = "Stop",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp) // Larger icon
                )
            }
            IconButton(
                onClick = onSearch,
                modifier = Modifier.size(48.dp) // Larger touch target
            ) {
                Icon(
                    painterResource(id = R.drawable.round_search_24),
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp) // Larger icon
                )
            }
            IconButton(
                onClick = onSettings,
                modifier = Modifier.size(48.dp) // Larger touch target
            ) {
                Icon(
                    painterResource(id = R.drawable.round_settings_24),
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp) // Larger icon
                )
            }
        }
    }
} 