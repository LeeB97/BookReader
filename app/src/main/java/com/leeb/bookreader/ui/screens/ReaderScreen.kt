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
import com.leeb.bookreader.ui.components.SearchBar
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
    
    // Search state
    val showSearch = viewModel.showSearch
    val searchQuery = viewModel.searchQuery
    val searchResults = viewModel.searchResults
    val currentSearchResultIndex = viewModel.currentSearchResultIndex
    
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
            topBar = {
                // Show search bar when search is active
                if (showSearch) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.searchQuery = it },
                        onSearch = { viewModel.searchParagraphs(searchQuery) },
                        onClose = { viewModel.clearSearch() },
                        onNext = { viewModel.navigateToNextSearchResult() },
                        onPrevious = { viewModel.navigateToPreviousSearchResult() },
                        resultCount = searchResults.size,
                        currentResultIndex = currentSearchResultIndex
                    )
                }
            },
            bottomBar = {
                // Control bar at the bottom
                ControlBar(
                    isSpeaking = isSpeaking,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onStop = onStop,
                    onSettings = { viewModel.showSettings = true },
                    onSearch = { viewModel.showSearch = true }
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
                    // Check if this paragraph is a search result
                    val isSearchResult = searchResults.contains(index)
                    
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
                        // Highlight search terms in the text if this is a search result
                        if (isSearchResult && searchQuery.isNotBlank()) {
                            HighlightedText(
                                text = paragraph,
                                searchTerm = searchQuery,
                                fontSize = settings.fontSize,
                                fontColor = if (index == currentParagraph) 
                                    MaterialTheme.colorScheme.onBackground 
                                else 
                                    Color(settings.fontColor),
                                highlightColor = Color.Yellow,
                                fontWeight = if (index == currentParagraph) FontWeight.Bold else FontWeight.Normal
                            )
                        } else {
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
    }
    
    if (showSettings) {
        SettingsDialog(
            settings = settings,
            defualtValues = { viewModel.loadSettings() },
            onDismiss = { viewModel.showSettings = false },
            onUrlChange = { viewModel.updateUrl(it) },
            onFontSizeChange = { viewModel.updateFontSize(it) },
            onFontColorChange = { viewModel.updateFontColor(it) },
            onBackgroundColorChange = { viewModel.updateBackgroundColor(it) },
            onLoadContent = { viewModel.loadContent() }
        )
    }
}

@Composable
fun HighlightedText(
    text: String,
    searchTerm: String,
    fontSize: Float,
    fontColor: Color,
    highlightColor: Color,
    fontWeight: FontWeight
) {
    val parts = text.split(searchTerm, ignoreCase = true)
    
    androidx.compose.foundation.text.BasicText(
        text = androidx.compose.ui.text.buildAnnotatedString {
            var currentIndex = 0
            
            for (i in parts.indices) {
                val part = parts[i]
                append(part)
                currentIndex += part.length
                
                // Add highlighted search term if not at the end
                if (i < parts.size - 1) {
                    val startIndex = text.indexOf(searchTerm, currentIndex, ignoreCase = true)
                    val endIndex = startIndex + searchTerm.length
                    val term = text.substring(startIndex, endIndex)
                    
                    pushStyle(
                        androidx.compose.ui.text.SpanStyle(
                            background = highlightColor,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    append(term)
                    pop()
                    
                    currentIndex = endIndex
                }
            }
        },
        style = androidx.compose.ui.text.TextStyle(
            fontSize = fontSize.sp,
            color = fontColor,
            fontWeight = fontWeight
        ),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    )
} 