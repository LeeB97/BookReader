package com.leeb.bookreader.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
    // Animated elevation for a more dynamic feel
    val elevation by animateDpAsState(
        targetValue = if (isSpeaking) 12.dp else 8.dp,
        animationSpec = tween(durationMillis = 300)
    )
    
    // Animated background color
    val backgroundColor by animateColorAsState(
        targetValue = if (isSpeaking) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        else 
            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        animationSpec = tween(durationMillis = 300)
    )
    
    // Use Surface to add elevation and make the control bar stand out
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(elevation)
            .zIndex(10f), // Ensure it's above other content
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            ControlButton(
                icon = R.drawable.round_skip_previous_24,
                contentDescription = "Previous",
                onClick = onPrevious,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(48.dp)
            )
            
            // Play/Pause button (larger)
            ControlButton(
                icon = if (isSpeaking) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24,
                contentDescription = "Play/Pause",
                onClick = onPlayPause,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp),
                iconSize = 36
            )
            
            // Next button
            ControlButton(
                icon = R.drawable.round_skip_next_24,
                contentDescription = "Next",
                onClick = onNext,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(48.dp)
            )
            
            // Stop button
            ControlButton(
                icon = R.drawable.round_stop_24,
                contentDescription = "Stop",
                onClick = onStop,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(48.dp)
            )
            
            // Search button
            ControlButton(
                icon = R.drawable.round_search_24,
                contentDescription = "Search",
                onClick = onSearch,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(48.dp)
            )
            
            // Settings button
            ControlButton(
                icon = R.drawable.round_settings_24,
                contentDescription = "Settings",
                onClick = onSettings,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun ControlButton(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color,
    backgroundColor: Color = Color.Transparent,
    modifier: Modifier = Modifier,
    iconSize: Int = 28
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painterResource(id = icon),
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize.dp)
            )
        }
    }
} 