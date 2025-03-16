package com.leeb.bookreader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leeb.bookreader.R
import com.leeb.bookreader.ui.components.ControlBar
import com.leeb.bookreader.ui.components.SettingsDialog
import com.leeb.bookreader.viewmodel.BookReaderViewModel

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
    
    // Auto-scroll to current paragraph when it changes
    LaunchedEffect(currentParagraph) {
        if (paragraphs.isNotEmpty() && currentParagraph < paragraphs.size) {
            scrollState.animateScrollToItem(currentParagraph)
        }
    }
    
    Scaffold(
        floatingActionButton = {
            ControlBar(
                isSpeaking = isSpeaking,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onStop = onStop,
                onSettings = { viewModel.showSettings = true }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color(settings.backgroundColor)
    ) { padding ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(settings.backgroundColor)),
            horizontalAlignment = Alignment.CenterHorizontally
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
                        defaultElevation = if (index == currentParagraph) 8.dp else 1.dp
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