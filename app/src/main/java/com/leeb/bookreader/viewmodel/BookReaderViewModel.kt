package com.leeb.bookreader.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            tts.language = Locale.US
            true
        } else {
            Log.e(TAG, "TTS initialization failed with status: $status")
            false
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
            } else {
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
    
    private fun saveCurrentParagraph(position: Int) {
        sharedPreferences.edit().putInt(KEY_CURRENT_PARAGRAPH, position).apply()
    }
    
    private fun saveParagraphs(paragraphsList: List<String>) {
        val gson = Gson()
        val json = gson.toJson(paragraphsList)
        sharedPreferences.edit().putString(KEY_PARAGRAPHS, json).apply()
    }
    
    fun loadSettings() {
        val url = sharedPreferences.getString(KEY_URL, "https://dl.dropboxusercontent.com/s/8ndtu5xb7gr6j2p/index.html?dl=0") ?: ""
        val fontSize = sharedPreferences.getFloat(KEY_FONT_SIZE, 18f)
        val fontColor = sharedPreferences.getInt(KEY_FONT_COLOR, Color.WHITE)
        val backgroundColor = sharedPreferences.getInt(KEY_BG_COLOR, Color.BLACK)
        
        settings = AppSettings(url, fontSize, fontColor, backgroundColor)
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
    
    override fun onCleared() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onCleared()
    }
} 