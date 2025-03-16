package com.leeb.bookreader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leeb.bookreader.ui.components.ControlBar
import com.leeb.bookreader.ui.components.SettingsDialog
import com.leeb.bookreader.viewmodel.BookReaderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: BookReaderViewModel,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onStop: () -> Unit
) {
    val paragraphs = viewModel.paragraphs
    val currentParagraph = viewModel.currentParagraph
    val isSpeaking = viewModel.isSpeaking
    val showSettings = viewModel.showSettings
    val settings = viewModel.settings
    
    // Create a scroll state that we can control programmatically
    val scrollState = rememberLazyListState()
    
    // Get the status bar height
    val statusBarInsets = WindowInsets.statusBars
    val statusBarHeight = with(LocalDensity.current) { statusBarInsets.getTop(this).toDp() }
    
    // Auto-scroll to current paragraph when it changes
    LaunchedEffect(currentParagraph) {
        if (paragraphs.isNotEmpty() && currentParagraph < paragraphs.size) {
            scrollState.animateScrollToItem(currentParagraph)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(settings.backgroundColor))
    ) {
        // Add a semi-transparent status bar background for better icon visibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarHeight)
                .background(Color.White.copy(alpha = 0.2f))
        )
        
        Scaffold(
            modifier = Modifier.padding(top = statusBarHeight), // Add padding for status bar
            bottomBar = {
                // Control bar at the bottom
                ControlBar(
                    isSpeaking = isSpeaking,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onStop = onStop,
                    onSettings = { viewModel.showSettings = true }
                )
            },
            containerColor = Color(settings.backgroundColor),
            contentWindowInsets = WindowInsets(0, 0, 0, 0) // We're handling insets manually
        ) { padding ->
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(settings.backgroundColor)),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(
                    start = 8.dp,
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 16.dp
                )
            ) {
                itemsIndexed(paragraphs) { index, paragraph ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                // When a paragraph is clicked, update the current paragraph
                                viewModel.currentParagraph = index
                                
                                // If we're already speaking, continue speaking from the new paragraph
                                if (viewModel.isSpeaking) {
                                    viewModel.speak()
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (index == currentParagraph)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                Color(settings.backgroundColor)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (index == currentParagraph) 8.dp else 0.dp
                        ),
                        border = if (index == currentParagraph) {
                            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else null
                    ) {
                        Text(
                            paragraph,
                            fontSize = settings.fontSize.sp,
                            color = if (index == currentParagraph) 
                                MaterialTheme.colorScheme.onBackground 
                            else 
                                Color(settings.fontColor),
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            fontWeight = if (index == currentParagraph) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
    
    if (showSettings) {
        SettingsDialog(
            settings = settings,
            onDismiss = { viewModel.showSettings = false },
            onUrlChange = { viewModel.updateUrl(it) },
            onFontSizeChange = { viewModel.updateFontSize(it) },
            onFontColorChange = { viewModel.updateFontColor(it) },
            onBackgroundColorChange = { viewModel.updateBackgroundColor(it) },
            onLoadContent = { viewModel.loadContent() }
        )
    }
} 