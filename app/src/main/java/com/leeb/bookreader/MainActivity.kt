package com.leeb.bookreader

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.leeb.bookreader.media.MediaManager
import com.leeb.bookreader.ui.screens.ReaderScreen
import com.leeb.bookreader.ui.theme.BookReaderTheme
import com.leeb.bookreader.viewmodel.BookReaderViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: BookReaderViewModel
    private lateinit var mediaManager: MediaManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the status bar to be transparent
        window.statusBarColor = android.graphics.Color.WHITE
        
        // Configure window to draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Make status bar icons visible against any background
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            // Use light status bar icons for better visibility against dark backgrounds
            controller.isAppearanceLightStatusBars = true
        }
        
        // Check and request notification permission for Android 13+
        checkNotificationPermission()
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[BookReaderViewModel::class.java]
        viewModel.initialize(this)
        
        // Initialize MediaManager
        mediaManager = MediaManager(this)
        mediaManager.initialize(viewModel)
        
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
        } catch (e: Exception) {
            Log.e("MainActivity", "Error registering receiver: ${e.message}")
        }

        setContent {
            BookReaderTheme {
                // Observe speaking state to update media notification
                LaunchedEffect(viewModel.isSpeaking) {
                    Log.d("MainActivity", "Speaking state changed: isSpeaking=${viewModel.isSpeaking}")
                    if (viewModel.isSpeaking) {
                        mediaManager.startMediaService()
                    } else {
                        // Also update notification when stopped speaking
                        mediaManager.updateMediaNotification()
                    }
                }
                
                // Observe current paragraph to update media notification
                LaunchedEffect(viewModel.currentParagraph) {
                    if (viewModel.isSpeaking) {
                        mediaManager.updateMediaNotification()
                    }
                }
                
                ReaderScreen(
                    viewModel = viewModel,
                    onPlayPause = {
                        viewModel.togglePlayPause()
                        // The notification will be updated via the LaunchedEffect above
                    },
                    onNext = {
                        viewModel.nextParagraph()
                        mediaManager.updateMediaNotification()
                    },
                    onPrevious = {
                        viewModel.previousParagraph()
                        mediaManager.updateMediaNotification()
                    },
                    onStop = {
                        viewModel.stop()
                        mediaManager.stopMediaService()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        // Unregister receiver and stop service
        try {
            unregisterReceiver(mediaControlReceiver)
            mediaManager.cleanup()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error unregistering receiver: ${e.message}")
        }
        
        super.onDestroy()
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