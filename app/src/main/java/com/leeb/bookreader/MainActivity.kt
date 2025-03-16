package com.leeb.bookreader

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener, MediaControlReceiver.MediaControlListener {

    private lateinit var tts: TextToSpeech
    private var isTTSReady by mutableStateOf(false)
    private var paragraphs = mutableStateListOf<String>()
    private var currentParagraph by mutableStateOf(0)
    private var isSpeaking by mutableStateOf(false)
    private var isPaused by mutableStateOf(false)
    private var showSettings by mutableStateOf(false)
    
    // SharedPreferences for saving app state
    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "BookReaderPrefs"
    private val KEY_URL = "url"
    private val KEY_FONT_SIZE = "fontSize"
    private val KEY_FONT_COLOR = "fontColor"
    private val KEY_BG_COLOR = "bgColor"
    private val KEY_CURRENT_PARAGRAPH = "currentParagraph"
    private val KEY_PARAGRAPHS = "paragraphs"
    
    // Media control receiver
    private val mediaControlReceiver = MediaControlReceiver()
    
    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Toast.makeText(
                this,
                "Notification permission denied. Media controls may not work properly.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val utteranceProgressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {

        }

        override fun onDone(utteranceId: String?) {
            // This is where we'll implement the logic to move to the next paragraph
            if (isSpeaking && paragraphs.isNotEmpty()) {
                if (currentParagraph < paragraphs.size - 1) {
                    currentParagraph++
                    // Save current paragraph position when it changes
                    saveCurrentParagraph(currentParagraph)
                    // Update notification
                    updateMediaNotification()
                } else {
                    isSpeaking = false
                    stopMediaService()
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            Log.e("TTS", "Error with utterance: $utteranceId")
            isSpeaking = false
            stopMediaService()
        }
        override fun onError(utteranceId: String?, errorCode: Int) {
            Log.e("TTS", "Error with utterance: $utteranceId, Error code: $errorCode")
            isSpeaking = false
            stopMediaService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check and request notification permission for Android 13+
        checkNotificationPermission()
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        tts = TextToSpeech(this, this)
        //Set the listener
        tts.setOnUtteranceProgressListener(utteranceProgressListener)
        
        // Register media control receiver
        try {
            val intentFilter = IntentFilter().apply {
                addAction(MediaService.ACTION_PLAY_PAUSE)
                addAction(MediaService.ACTION_NEXT)
                addAction(MediaService.ACTION_PREVIOUS)
                addAction(MediaService.ACTION_STOP)
            }
            
            // Register with RECEIVER_NOT_EXPORTED flag
            registerReceiver(
                mediaControlReceiver, 
                intentFilter,
                Context.RECEIVER_NOT_EXPORTED
            )
            
            MediaControlReceiver.setListener(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error registering receiver: ${e.message}")
        }

        setContent {
            val context = LocalContext.current
            
            // Load saved settings
            val savedUrl = remember { mutableStateOf(sharedPreferences.getString(KEY_URL, "https://dl.dropboxusercontent.com/s/8ndtu5xb7gr6j2p/index.html?dl=0") ?: "") }
            val savedFontSize = remember { mutableStateOf(sharedPreferences.getFloat(KEY_FONT_SIZE, 18f)) }
            val savedFontColor = remember { mutableStateOf(sharedPreferences.getInt(KEY_FONT_COLOR, Color.White.toArgb())) }
            val savedBgColor = remember { mutableStateOf(sharedPreferences.getInt(KEY_BG_COLOR, Color.Black.toArgb())) }
            
            var url by remember { mutableStateOf(savedUrl.value) }
            var fontSize by remember { mutableStateOf(savedFontSize.value.sp) }
            var fontColor by remember { mutableStateOf(Color(savedFontColor.value)) }
            var bgColor by remember { mutableStateOf(Color(savedBgColor.value)) }
            val coroutineScope = rememberCoroutineScope()
            
            // Create a scroll state that we can control programmatically
            val scrollState = rememberLazyListState()
            val cardOffsets = remember { mutableStateMapOf<Int, Int>() }

            // Load saved paragraphs and current position
            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    loadSavedParagraphs()
                    currentParagraph = sharedPreferences.getInt(KEY_CURRENT_PARAGRAPH, 0)
                }
            }

            Scaffold(
                floatingActionButton = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray.copy(alpha = 0.8f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { 
                            if (currentParagraph > 0) {
                                currentParagraph--
                                saveCurrentParagraph(currentParagraph)
                                updateMediaNotification()
                            }
                        }) {
                            Icon(
                                painterResource(id = R.drawable.round_skip_previous_24), 
                                contentDescription = "Previous",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = {
                            if (isSpeaking) {
                                tts.stop()
                                isSpeaking = false
                                isPaused = true
                                updateMediaNotification()
                            } else {
                                isSpeaking = true
                                startMediaService()
                            }
                        }) {
                            Icon(
                                painterResource(id = if (isSpeaking) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24),
                                contentDescription = "Play/Pause",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { 
                            if (currentParagraph < paragraphs.size - 1) {
                                currentParagraph++
                                saveCurrentParagraph(currentParagraph)
                                updateMediaNotification()
                            }
                        }) {
                            Icon(
                                painterResource(id = R.drawable.round_skip_next_24), 
                                contentDescription = "Next",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { onStopClicked() }) {
                            Icon(
                                painterResource(id = R.drawable.round_stop_24), 
                                contentDescription = "Stop",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { showSettings = true }) {
                            Icon(
                                painterResource(id = R.drawable.round_settings_24), 
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }
                },
                floatingActionButtonPosition = FabPosition.End,
                containerColor = bgColor
            ) { padding ->

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(bgColor),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyColumn(
                        state = scrollState,  // Use LazyListState
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .background(bgColor),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        itemsIndexed(paragraphs) { index, paragraph ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        // When a paragraph is clicked, update the current paragraph
                                        currentParagraph = index
                                        saveCurrentParagraph(index)
                                        // If speaking, stop current and start new paragraph
                                        if (isSpeaking) {
                                            tts.stop()
                                            // TTS will automatically start speaking the new paragraph due to the LaunchedEffect
                                        }
                                        // Update notification if needed
                                        updateMediaNotification()
                                    }
                                    .onGloballyPositioned { coordinates ->
                                        val yOffset = coordinates.positionInRoot().y.toInt()
                                        cardOffsets[index] = yOffset
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (index == currentParagraph)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        bgColor
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (index == currentParagraph) 8.dp else 1.dp
                                ),
                                border = if (index == currentParagraph) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else null
                            ) {
                                Text(
                                    paragraph,
                                    fontSize = fontSize,
                                    color = if (index == currentParagraph) MaterialTheme.colorScheme.onBackground else fontColor,
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

            LaunchedEffect(currentParagraph, isTTSReady, isSpeaking) {
                if (isTTSReady && isSpeaking && paragraphs.isNotEmpty() && currentParagraph < paragraphs.size) {
                    val utteranceId = this.hashCode().toString() + System.currentTimeMillis().toString()
                    val params = Bundle()
                    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                    tts.speak(paragraphs[currentParagraph], TextToSpeech.QUEUE_FLUSH, params, utteranceId)

                    scrollState.animateScrollToItem(currentParagraph)
                    
                    // Start or update media service
                    if (isSpeaking) {
                        startMediaService()
                    }
                }
            }

            if (showSettings) {
                Dialog(onDismissRequest = { showSettings = false }, properties = DialogProperties()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(
                            value = url, 
                            onValueChange = { 
                                url = it
                                saveUrl(url)
                            }, 
                            placeholder = { Text("Enter URL", color = Color.Gray) }, 
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            coroutineScope.launch {
                                paragraphs.clear()
                                val newParagraphs = fetchAndParse(url)
                                paragraphs.addAll(newParagraphs)
                                currentParagraph = 0
                                // Save the new paragraphs
                                saveParagraphs(newParagraphs)
                                saveCurrentParagraph(0)
                            }
                        }) { Text("Load Content") }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Font Size:", color = Color.White)
                            Slider(
                                value = fontSize.value, 
                                onValueChange = { 
                                    fontSize = it.sp
                                    saveFontSize(it)
                                }, 
                                valueRange = 14f..30f
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Font Color:", color = Color.White)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.White)
                                        .clickable { 
                                            fontColor = Color.White
                                            saveFontColor(fontColor)
                                        }
                                        .border(
                                            width = 2.dp,
                                            color = if (fontColor == Color.White) Color.Yellow else Color.Transparent
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.Black)
                                        .clickable { 
                                            fontColor = Color.Black
                                            saveFontColor(fontColor)
                                        }
                                        .border(
                                            width = 2.dp,
                                            color = if (fontColor == Color.Black) Color.Yellow else Color.Transparent
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.Gray)
                                        .clickable { 
                                            fontColor = Color.Gray
                                            saveFontColor(fontColor)
                                        }
                                        .border(
                                            width = 2.dp,
                                            color = if (fontColor == Color.Gray) Color.Yellow else Color.Transparent
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.Yellow)
                                        .clickable { 
                                            fontColor = Color.Yellow
                                            saveFontColor(fontColor)
                                        }
                                        .border(
                                            width = 2.dp,
                                            color = if (fontColor == Color.Yellow) Color.White else Color.Transparent
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.Green)
                                        .clickable { 
                                            fontColor = Color.Green
                                            saveFontColor(fontColor)
                                        }
                                        .border(
                                            width = 2.dp,
                                            color = if (fontColor == Color.Green) Color.Yellow else Color.Transparent
                                        )
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Background Color:", color = Color.White)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.Black)
                                        .clickable { 
                                            bgColor = Color.Black
                                            saveBackgroundColor(bgColor)
                                        }
                                        .border(
                                            width = 2.dp,
                                            color = Color.Transparent
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.White)
                                        .clickable { 
                                            bgColor = Color.White
                                            saveBackgroundColor(bgColor)
                                        }
                                        .border(
                                            width = 2.dp,
                                            color = Color.Transparent
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.DarkGray)
                                        .clickable { 
                                            bgColor = Color.DarkGray
                                            saveBackgroundColor(bgColor)
                                        }
                                        .border(
                                            width = 2.dp,
                                            color = Color.White
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.Blue.copy(alpha = 0.7f))
                                        .clickable { 
                                            bgColor = Color.Blue.copy(alpha = 0.7f)
                                            saveBackgroundColor(bgColor)
                                        }
                                        .border(
                                            width = 2.dp,
                                            color = Color.Transparent
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color(0xFF2D2D2D))
                                        .clickable { 
                                            bgColor = Color(0xFF2D2D2D)
                                            saveBackgroundColor(bgColor)
                                        }
                                        .border(
                                            width = 2.dp,
                                            color = Color.Transparent
                                        )
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { showSettings = false }) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    override fun onInit(status: Int) {
        isTTSReady = if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            true
        } else {
            Toast.makeText(this, "TTS Initialization Failed", Toast.LENGTH_SHORT).show()
            false
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        
        // Unregister receiver and stop service
        try {
            unregisterReceiver(mediaControlReceiver)
            MediaControlReceiver.removeListener()
            stopMediaService()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error unregistering receiver: ${e.message}")
        }
        
        super.onDestroy()
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
            emptyList()
        }
    }
    
    // Methods to save settings to SharedPreferences
    private fun saveUrl(url: String) {
        sharedPreferences.edit().putString(KEY_URL, url).apply()
    }
    
    private fun saveFontSize(size: Float) {
        sharedPreferences.edit().putFloat(KEY_FONT_SIZE, size).apply()
    }
    
    private fun saveFontColor(color: Color) {
        sharedPreferences.edit().putInt(KEY_FONT_COLOR, color.toArgb()).apply()
    }
    
    private fun saveBackgroundColor(color: Color) {
        sharedPreferences.edit().putInt(KEY_BG_COLOR, color.toArgb()).apply()
    }
    
    private fun saveCurrentParagraph(position: Int) {
        sharedPreferences.edit().putInt(KEY_CURRENT_PARAGRAPH, position).apply()
    }
    
    private fun saveParagraphs(paragraphsList: List<String>) {
        val gson = Gson()
        val json = gson.toJson(paragraphsList)
        sharedPreferences.edit().putString(KEY_PARAGRAPHS, json).apply()
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
    }
    
    // Media service methods
    private fun startMediaService() {
        try {
            val intent = Intent(this, MediaService::class.java).apply {
                putExtra(MediaService.EXTRA_IS_PLAYING, isSpeaking)
                putExtra(MediaService.EXTRA_TITLE, getCurrentTitle())
            }
            ContextCompat.startForegroundService(this, intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error starting media service: ${e.message}")
            Toast.makeText(this, "Error starting media service", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateMediaNotification() {
        if (isSpeaking) {
            try {
                val intent = Intent(this, MediaService::class.java).apply {
                    putExtra(MediaService.EXTRA_IS_PLAYING, isSpeaking)
                    putExtra(MediaService.EXTRA_TITLE, getCurrentTitle())
                }
                ContextCompat.startForegroundService(this, intent)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error updating media notification: ${e.message}")
            }
        }
    }
    
    private fun stopMediaService() {
        try {
            stopService(Intent(this, MediaService::class.java))
        } catch (e: Exception) {
            Log.e("MainActivity", "Error stopping media service: ${e.message}")
        }
    }
    
    private fun getCurrentTitle(): String {
        return if (paragraphs.isNotEmpty() && currentParagraph < paragraphs.size) {
            val text = paragraphs[currentParagraph]
            if (text.length > 30) text.substring(0, 30) + "..." else text
        } else {
            "Book Reader"
        }
    }
    
    // MediaControlListener implementation
    override fun onPlayPauseClicked() {
        if (isSpeaking) {
            tts.stop()
            isSpeaking = false
            isPaused = true
        } else {
            isSpeaking = true
        }
        updateMediaNotification()
    }
    
    override fun onNextClicked() {
        if (currentParagraph < paragraphs.size - 1) {
            currentParagraph++
            saveCurrentParagraph(currentParagraph)
            if (isSpeaking) {
                tts.stop()
                // TTS will automatically start speaking the new paragraph due to the LaunchedEffect
            }
            updateMediaNotification()
        }
    }
    
    override fun onPreviousClicked() {
        if (currentParagraph > 0) {
            currentParagraph--
            saveCurrentParagraph(currentParagraph)
            if (isSpeaking) {
                tts.stop()
                // TTS will automatically start speaking the new paragraph due to the LaunchedEffect
            }
            updateMediaNotification()
        }
    }
    
    override fun onStopClicked() {
        isSpeaking = false
        isPaused = false
        currentParagraph = 0;
        tts.stop()
        stopMediaService()
    }

    private fun checkNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                Log.d("MainActivity", "Notification permission already granted")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // Explain to the user why we need the permission
                Toast.makeText(
                    this,
                    "Notification permission is needed for media controls",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            else -> {
                // Request the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}