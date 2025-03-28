package com.leeb.bookreader.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.leeb.bookreader.model.AppSettings
import com.leeb.bookreader.model.ReaderState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.*

class BookReaderViewModel : ViewModel(), TextToSpeech.OnInitListener {
    
    // Preferences constants
    companion object {
        const val PREFS_NAME = "BookReaderPrefs"
        const val KEY_URL = "url"
        const val KEY_FONT_SIZE = "fontSize"
        const val KEY_FONT_COLOR = "fontColor"
        const val KEY_BG_COLOR = "bgColor"
        const val KEY_CURRENT_PARAGRAPH = "currentParagraph"
        const val KEY_PARAGRAPHS = "paragraphs"
        const val KEY_SPEECH_RATE = "speechRate"
        const val KEY_VOICE_LOCALE = "voiceLocale"
        const val KEY_FONT_FAMILY = "fontFamily"
        const val KEY_FONT_WEIGHT = "fontWeight"
        const val KEY_HTML_SELECTOR = "htmlSelector"
        
        private const val TAG = "BookReaderViewModel"
    }
    
    // State
    private lateinit var tts: TextToSpeech
    var isTTSReady by mutableStateOf(false)
    val paragraphs = mutableStateListOf<String>()
    var currentParagraph by mutableStateOf(0)
    var isSpeaking by mutableStateOf(false)
    var isPaused by mutableStateOf(false)
    var showSettings by mutableStateOf(false)
    
    // Available voices
    var availableVoices = mutableStateListOf<Voice>()
    var availableLocales = mutableStateListOf<Locale>()
    
    // Search functionality
    var showSearch by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    var searchResults = mutableStateListOf<Int>()
    var currentSearchResultIndex by mutableStateOf(-1)
    
    // Settings
    var settings by mutableStateOf(AppSettings())
    
    // SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences
    
    fun initialize(context: Context) {
        // Initialize SharedPreferences
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Initialize TTS
        tts = TextToSpeech(context, this)
        tts.setOnUtteranceProgressListener(createUtteranceProgressListener())
        
        // Load settings
        loadSettings()
        
        // Load saved paragraphs
        loadSavedParagraphs()
    }
    
    private fun createUtteranceProgressListener(): UtteranceProgressListener {
        return object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // Not needed
            }

            override fun onDone(utteranceId: String?) {
                // Move to the next paragraph when done speaking
                if (isSpeaking && paragraphs.isNotEmpty()) {
                    if (currentParagraph < paragraphs.size - 1) {
                        currentParagraph++
                        // Save current paragraph position when it changes
                        saveCurrentParagraph(currentParagraph)
                        speak()
                    } else {
                        isSpeaking = false
                        Log.d(TAG, "Reached end of paragraphs, stopping playback")
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "Error with utterance: $utteranceId")
                isSpeaking = false
            }
            
            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e(TAG, "Error with utterance: $utteranceId, Error code: $errorCode")
                isSpeaking = false
            }
        }
    }
    
    override fun onInit(status: Int) {
        isTTSReady = if (status == TextToSpeech.SUCCESS) {
            // Set the language from settings
            updateTTSVoice()
            
            // Get available voices
            loadAvailableVoices()
            
            // Set speech rate from settings
            tts.setSpeechRate(settings.speechRate)
            
            true
        } else {
            Log.e(TAG, "TTS initialization failed with status: $status")
            false
        }
    }
    
    private fun loadAvailableVoices() {
        availableVoices.clear()
        availableLocales.clear()
        
        // Get all available voices
        val voices = tts.voices
        if (voices != null) {
            availableVoices.addAll(voices)
            
            // Extract unique locales from voices
            val locales = voices.map { it.locale }.distinct()
            availableLocales.addAll(locales)
        } else {
            // Fallback to default locales if voices not available
            availableLocales.add(Locale.US)
            availableLocales.add(Locale.UK)
            availableLocales.add(Locale.CANADA)
            availableLocales.add(Locale.GERMANY)
            availableLocales.add(Locale.FRANCE)
            availableLocales.add(Locale.ITALY)
            availableLocales.add(Locale.JAPAN)
            availableLocales.add(Locale.KOREA)
            availableLocales.add(Locale.CHINA)
        }
    }
    
    private fun updateTTSVoice() {
        try {
            val locale = Locale.forLanguageTag(settings.voiceLocale)
            val result = tts.setLanguage(locale)
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported: ${locale.displayName}, falling back to US English")
                tts.language = Locale.US
            } else {
                Log.d(TAG, "Set TTS language to: ${locale.displayName}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting TTS language: ${e.message}")
            tts.language = Locale.US
        }
    }
    
    fun speak() {
        if (isTTSReady && paragraphs.isNotEmpty() && currentParagraph < paragraphs.size) {
            val utteranceId = this.hashCode().toString() + System.currentTimeMillis().toString()
            val params = android.os.Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            
            Log.d(TAG, "Speaking paragraph $currentParagraph")
            tts.speak(paragraphs[currentParagraph], TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }
    
    fun togglePlayPause() {
        if (isSpeaking) {
            Log.d(TAG, "Pausing playback")
            tts.stop()
            isSpeaking = false
            isPaused = true
        } else {
            Log.d(TAG, "Starting playback")
            isSpeaking = true
            isPaused = false
            speak()
        }
    }
    
    fun nextParagraph() {
        if (currentParagraph < paragraphs.size - 1) {
            currentParagraph++
            saveCurrentParagraph(currentParagraph)
            if (isSpeaking) {
                tts.stop()
                speak()
            }
        }
    }
    
    fun previousParagraph() {
        if (currentParagraph > 0) {
            currentParagraph--
            saveCurrentParagraph(currentParagraph)
            if (isSpeaking) {
                tts.stop()
                speak()
            }
        }
    }
    
    fun stop() {
        Log.d(TAG, "Stopping playback completely")
        isSpeaking = false
        isPaused = false
        currentParagraph = 0
        saveCurrentParagraph(0)
        tts.stop()
    }
    
    fun loadContent() {
        viewModelScope.launch {
            paragraphs.clear()
            Log.d(TAG, "Loading content with URL: ${settings.url} and HTML selector: ${settings.htmlSelector}")
            val newParagraphs = fetchAndParse(settings.url)
            paragraphs.addAll(newParagraphs)
            currentParagraph = 0
            // Save the new paragraphs
            saveParagraphs(newParagraphs)
            saveCurrentParagraph(0)
        }
    }
    
    private suspend fun fetchAndParse(url: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url).get()
            if (url.endsWith(".txt")) {
                doc.text().split("\n").filter { it.isNotBlank() }
            } else if(settings.htmlSelector.isNotBlank()) {
                Log.d(TAG, "Using HTML selector: ${settings.htmlSelector}")
                val container = doc.selectFirst(settings.htmlSelector)
                
                if (container != null) {
                    Log.d(TAG, "Selector found in document")
                    container.allElements
                        .flatMap { it.textNodes() }
                        .map { it.text().trim() }
                        .filter { it.isNotBlank() }
                } else {
                    Log.d(TAG, "Selector not found, using body instead")
                    doc.body().allElements
                        .flatMap { it.textNodes() }
                        .map { it.text().trim() }
                        .filter { it.isNotBlank() }
                }
            } else {
                Log.d(TAG, "No selector, using entire body")
                doc.body().allElements
                    .flatMap { it.textNodes() }
                    .map { it.text().trim() }
                    .filter { it.isNotBlank() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching content: ${e.message}")
            emptyList()
        }
    }
    
    fun updateUrl(url: String) {
        settings = settings.copy(url = url)
        saveUrl(url)
    }
    
    fun updateFontSize(size: Float) {
        settings = settings.copy(fontSize = size)
        saveFontSize(size)
    }
    
    fun updateFontColor(color: Int) {
        settings = settings.copy(fontColor = color)
        saveFontColor(color)
    }
    
    fun updateBackgroundColor(color: Int) {
        settings = settings.copy(backgroundColor = color)
        saveBackgroundColor(color)
    }
    
    fun updateSpeechRate(rate: Float) {
        settings = settings.copy(speechRate = rate)
        saveSpeechRate(rate)
        
        // Update TTS speech rate
        if (::tts.isInitialized && isTTSReady) {
            tts.setSpeechRate(rate)
        }
    }
    
    fun updateVoiceLocale(localeTag: String) {
        settings = settings.copy(voiceLocale = localeTag)
        saveVoiceLocale(localeTag)
        
        // Update TTS voice
        if (::tts.isInitialized && isTTSReady) {
            updateTTSVoice()
        }
    }
    
    fun updateFontFamily(fontFamily: String) {
        settings = settings.copy(fontFamily = fontFamily)
        saveFontFamily(fontFamily)
    }
    
    fun updateFontWeight(fontWeight: androidx.compose.ui.text.font.FontWeight) {
        settings = settings.copy(fontWeight = fontWeight)
        saveFontWeight(fontWeight)
    }
    
    fun updateHtmlSelector(selector: String) {
        settings = settings.copy(htmlSelector = selector)
        saveHtmlSelector(selector)
    }
    
    fun getCurrentTitle(): String {
        return if (paragraphs.isNotEmpty() && currentParagraph < paragraphs.size) {
            val text = paragraphs[currentParagraph]
            if (text.length > 30) text.substring(0, 30) + "..." else text
        } else {
            "Book Reader"
        }
    }
    
    fun getReaderState(): ReaderState {
        return ReaderState(
            isSpeaking = isSpeaking,
            isPaused = isPaused,
            currentParagraph = currentParagraph,
            paragraphs = paragraphs.toList()
        )
    }
    
    // Methods to save settings to SharedPreferences
    private fun saveUrl(url: String) {
        sharedPreferences.edit().putString(KEY_URL, url).apply()
    }
    
    private fun saveFontSize(size: Float) {
        sharedPreferences.edit().putFloat(KEY_FONT_SIZE, size).apply()
    }
    
    private fun saveFontColor(color: Int) {
        sharedPreferences.edit().putInt(KEY_FONT_COLOR, color).apply()
    }
    
    private fun saveBackgroundColor(color: Int) {
        sharedPreferences.edit().putInt(KEY_BG_COLOR, color).apply()
    }
    
    private fun saveSpeechRate(rate: Float) {
        sharedPreferences.edit().putFloat(KEY_SPEECH_RATE, rate).apply()
    }
    
    private fun saveVoiceLocale(localeTag: String) {
        sharedPreferences.edit().putString(KEY_VOICE_LOCALE, localeTag).apply()
    }
    
    private fun saveFontFamily(fontFamily: String) {
        Log.d(TAG, "Saving font family: $fontFamily")
        sharedPreferences.edit().putString(KEY_FONT_FAMILY, fontFamily).apply()
    }
    
    private fun saveFontWeight(fontWeight: androidx.compose.ui.text.font.FontWeight) {
        Log.d(TAG, "Saving font weight: $fontWeight")
        sharedPreferences.edit().putInt(KEY_FONT_WEIGHT, fontWeight.weight).apply()
    }
    
    private fun saveHtmlSelector(selector: String) {
        Log.d(TAG, "Saving HTML selector: $selector")
        sharedPreferences.edit().putString(KEY_HTML_SELECTOR, selector).apply()
    }
    
    private fun saveCurrentParagraph(position: Int) {
        sharedPreferences.edit().putInt(KEY_CURRENT_PARAGRAPH, position).apply()
    }
    
    private fun saveParagraphs(paragraphsList: List<String>) {
        val gson = Gson()
        val json = gson.toJson(paragraphsList)
        sharedPreferences.edit().putString(KEY_PARAGRAPHS, json).apply()
    }
    
    private fun loadSettings() {
        val url = sharedPreferences.getString(KEY_URL, settings.url) ?: settings.url
        val fontSize = sharedPreferences.getFloat(KEY_FONT_SIZE, settings.fontSize)
        val fontColor = sharedPreferences.getInt(KEY_FONT_COLOR, settings.fontColor)
        val backgroundColor = sharedPreferences.getInt(KEY_BG_COLOR, settings.backgroundColor)
        val speechRate = sharedPreferences.getFloat(KEY_SPEECH_RATE, settings.speechRate)
        val voiceLocale = sharedPreferences.getString(KEY_VOICE_LOCALE, settings.voiceLocale) ?: settings.voiceLocale
        val fontFamily = sharedPreferences.getString(KEY_FONT_FAMILY, settings.fontFamily) ?: settings.fontFamily
        val fontWeightValue = sharedPreferences.getInt(KEY_FONT_WEIGHT, settings.fontWeight.weight)
        val fontWeight = androidx.compose.ui.text.font.FontWeight(fontWeightValue)
        val htmlSelector = sharedPreferences.getString(KEY_HTML_SELECTOR, settings.htmlSelector) ?: settings.htmlSelector
        
        settings = settings.copy(
            url = url,
            fontSize = fontSize,
            fontColor = fontColor,
            backgroundColor = backgroundColor,
            speechRate = speechRate,
            voiceLocale = voiceLocale,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            htmlSelector = htmlSelector
        )
        
        Log.d(TAG, "Settings loaded: $settings")
        
        // Update TTS settings if initialized
        if (::tts.isInitialized && isTTSReady) {
            tts.setSpeechRate(speechRate)
            updateTTSVoice()
        }
    }
    
    fun restoreDefaultSettings(context: Context? = null) {
        val defaultSettings = AppSettings()
        
        // Update all settings to default values
        updateUrl(defaultSettings.url)
        updateFontSize(defaultSettings.fontSize)
        updateFontColor(defaultSettings.fontColor)
        updateBackgroundColor(defaultSettings.backgroundColor)
        updateSpeechRate(defaultSettings.speechRate)
        updateVoiceLocale(defaultSettings.voiceLocale)
        updateFontFamily(defaultSettings.fontFamily)
        updateFontWeight(defaultSettings.fontWeight)
        updateHtmlSelector(defaultSettings.htmlSelector)
        
        // Show toast if context is provided
        context?.let {
            Toast.makeText(it, "Settings restored to defaults", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadSavedParagraphs() {
        val gson = Gson()
        val json = sharedPreferences.getString(KEY_PARAGRAPHS, null)
        if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            val savedParagraphs: List<String> = gson.fromJson(json, type)
            paragraphs.clear()
            paragraphs.addAll(savedParagraphs)
        }
        
        // Load current paragraph position
        currentParagraph = sharedPreferences.getInt(KEY_CURRENT_PARAGRAPH, 0)
    }
    
    fun searchParagraphs(query: String) {
        if (query.isBlank()) {
            searchResults.clear()
            currentSearchResultIndex = -1
            return
        }
        
        searchQuery = query
        searchResults.clear()
        
        // Find all paragraphs containing the search query (case insensitive)
        paragraphs.forEachIndexed { index, paragraph ->
            if (paragraph.contains(query, ignoreCase = true)) {
                searchResults.add(index)
            }
        }
        
        // Reset search result index
        currentSearchResultIndex = if (searchResults.isNotEmpty()) 0 else -1
        
        // Navigate to the first result if found
        if (currentSearchResultIndex >= 0) {
            currentParagraph = searchResults[currentSearchResultIndex]
            saveCurrentParagraph(currentParagraph)
        }
    }
    
    fun navigateToNextSearchResult() {
        if (searchResults.isEmpty()) return
        
        currentSearchResultIndex = (currentSearchResultIndex + 1) % searchResults.size
        currentParagraph = searchResults[currentSearchResultIndex]
        saveCurrentParagraph(currentParagraph)
    }
    
    fun navigateToPreviousSearchResult() {
        if (searchResults.isEmpty()) return
        
        currentSearchResultIndex = if (currentSearchResultIndex <= 0) 
            searchResults.size - 1 
        else 
            currentSearchResultIndex - 1
            
        currentParagraph = searchResults[currentSearchResultIndex]
        saveCurrentParagraph(currentParagraph)
    }
    
    fun clearSearch() {
        searchQuery = ""
        searchResults.clear()
        currentSearchResultIndex = -1
        showSearch = false
    }
    
    override fun onCleared() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onCleared()
    }
} 