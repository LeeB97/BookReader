package com.leeb.bookreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.leeb.bookreader.R

@Composable
fun ControlBar(
    isSpeaking: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onStop: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray.copy(alpha = 0.8f))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                painterResource(id = R.drawable.round_skip_previous_24),
                contentDescription = "Previous",
                tint = Color.White
            )
        }
        IconButton(onClick = onPlayPause) {
            Icon(
                painterResource(id = if (isSpeaking) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24),
                contentDescription = "Play/Pause",
                tint = Color.White
            )
        }
        IconButton(onClick = onNext) {
            Icon(
                painterResource(id = R.drawable.round_skip_next_24),
                contentDescription = "Next",
                tint = Color.White
            )
        }
        IconButton(onClick = onStop) {
            Icon(
                painterResource(id = R.drawable.round_stop_24),
                contentDescription = "Stop",
                tint = Color.White
            )
        }
        IconButton(onClick = onSettings) {
            Icon(
                painterResource(id = R.drawable.round_settings_24),
                contentDescription = "Settings",
                tint = Color.White
            )
        }
    }
} 