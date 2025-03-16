package com.leeb.bookreader.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
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
    
    // Get the current context for Toast
    val context = LocalContext.current
    
    // Search state
    val showSearch = viewModel.showSearch
    val searchQuery = viewModel.searchQuery
    val searchResults = viewModel.searchResults
    val currentSearchResultIndex = viewModel.currentSearchResultIndex
    
    // Voice state
    val availableLocales = viewModel.availableLocales
    
    // Create a scroll state that we can control programmatically
    val scrollState = rememberLazyListState()
    
    // Get the status bar height
    val statusBarHeight = with(LocalDensity.current) { 
        WindowInsets.statusBars.getTop(this).toDp() 
    }
    
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
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                .shadow(4.dp)
        )
        
        Scaffold(
            modifier = Modifier.padding(top = statusBarHeight),
            topBar = {
                // Show search bar when search is active with animation
                AnimatedVisibility(
                    visible = showSearch,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
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
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 24.dp)
            ) {
                itemsIndexed(paragraphs) { index, paragraph ->
                    // Check if this paragraph is a search result
                    val isSearchResult = searchResults.contains(index)
                    val isCurrentParagraph = index == currentParagraph
                    
                    ParagraphCard(
                        paragraph = paragraph,
                        isCurrentParagraph = isCurrentParagraph,
                        isSearchResult = isSearchResult,
                        searchQuery = searchQuery,
                        fontSize = settings.fontSize,
                        fontColor = settings.fontColor,
                        backgroundColor = settings.backgroundColor,
                        onClick = {
                            viewModel.currentParagraph = index
                            if (viewModel.isSpeaking) {
                                viewModel.speak()
                            }
                        }
                    )
                }
            }
        }
    }
    
    // Show settings dialog with animation
    AnimatedVisibility(
        visible = showSettings,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        SettingsDialog(
            settings = settings,
            defaultValues = { viewModel.restoreDefaultSettings(context) },
            onDismiss = { viewModel.showSettings = false },
            onUrlChange = { viewModel.updateUrl(it) },
            onFontSizeChange = { viewModel.updateFontSize(it) },
            onFontColorChange = { viewModel.updateFontColor(it) },
            onBackgroundColorChange = { viewModel.updateBackgroundColor(it) },
            onSpeechRateChange = { viewModel.updateSpeechRate(it) },
            onVoiceLocaleChange = { viewModel.updateVoiceLocale(it) },
            onLoadContent = { viewModel.loadContent() },
            availableLocales = availableLocales
        )
    }
}

/**
 * Displays text with highlighted search terms
 */
@Composable
fun HighlightedText(
    text: String,
    searchTerm: String,
    fontSize: Float,
    fontColor: Color,
    highlightColor: Color,
    fontWeight: FontWeight
) {
    // Skip processing if searchTerm is empty
    if (searchTerm.isBlank()) {
        Text(
            text = text,
            fontSize = fontSize.sp,
            color = fontColor,
            fontWeight = fontWeight,
            lineHeight = (fontSize * 1.4).sp,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )
        return
    }
    
    val parts = text.split(searchTerm, ignoreCase = true)
    
    BasicText(
        text = buildAnnotatedString {
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
                        SpanStyle(
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
        style = TextStyle(
            fontSize = fontSize.sp,
            color = fontColor,
            fontWeight = fontWeight,
            lineHeight = (fontSize * 1.4).sp
        ),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}

/**
 * A card that displays a paragraph of text with animations and highlighting
 */
@Composable
private fun ParagraphCard(
    paragraph: String,
    isCurrentParagraph: Boolean,
    isSearchResult: Boolean,
    searchQuery: String,
    fontSize: Float,
    fontColor: Int,
    backgroundColor: Int,
    onClick: () -> Unit
) {
    // Track if this card is being pressed
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    
    // Animations
    val elevation by animateFloatAsState(
        targetValue = if (isCurrentParagraph) 8f else 1f,
        animationSpec = tween(durationMillis = 300)
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isCurrentParagraph) 1f else 0.9f,
        animationSpec = tween(durationMillis = 200)
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    // Track press state
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release, is PressInteraction.Cancel -> isPressed = false
            }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .alpha(alpha)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentParagraph)
                MaterialTheme.colorScheme.primaryContainer
            else
                Color(backgroundColor)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.dp
        ),
        shape = MaterialTheme.shapes.medium,
        border = if (isCurrentParagraph) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        if (isSearchResult && searchQuery.isNotBlank()) {
            HighlightedText(
                text = paragraph,
                searchTerm = searchQuery,
                fontSize = fontSize,
                fontColor = if (isCurrentParagraph) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    Color(fontColor),
                highlightColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                fontWeight = if (isCurrentParagraph) FontWeight.Bold else FontWeight.Normal
            )
        } else {
            Text(
                text = paragraph,
                fontSize = fontSize.sp,
                color = if (isCurrentParagraph) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    Color(fontColor),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                fontWeight = if (isCurrentParagraph) FontWeight.Bold else FontWeight.Normal,
                lineHeight = (fontSize * 1.4).sp
            )
        }
    }
} 