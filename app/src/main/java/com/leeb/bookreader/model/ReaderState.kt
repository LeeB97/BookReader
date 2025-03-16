package com.leeb.bookreader.model

data class ReaderState(
    val isSpeaking: Boolean = false,
    val isPaused: Boolean = false,
    val currentParagraph: Int = 0,
    val paragraphs: List<String> = emptyList()
) 