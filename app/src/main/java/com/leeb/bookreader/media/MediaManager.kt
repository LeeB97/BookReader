package com.leeb.bookreader.media

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.leeb.bookreader.MediaService
import com.leeb.bookreader.MediaControlReceiver
import com.leeb.bookreader.viewmodel.BookReaderViewModel

class MediaManager(private val context: Context) : MediaControlReceiver.MediaControlListener {
    
    private var viewModel: BookReaderViewModel? = null
    
    fun initialize(viewModel: BookReaderViewModel) {
        this.viewModel = viewModel
        MediaControlReceiver.setListener(this)
    }
    
    fun startMediaService() {
        try {
            val isPlaying = viewModel?.isSpeaking ?: false
            
            val intent = Intent(context, MediaService::class.java).apply {
                putExtra(MediaService.EXTRA_IS_PLAYING, isPlaying)
                putExtra(MediaService.EXTRA_TITLE, viewModel?.getCurrentTitle() ?: "Book Reader")
            }
            
            Log.d("MediaManager", "Starting media service with isPlaying=$isPlaying")
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            Log.e("MediaManager", "Error starting media service: ${e.message}")
        }
    }
    
    fun updateMediaNotification() {
        val isPlaying = viewModel?.isSpeaking ?: false
        
        try {
            val intent = Intent(context, MediaService::class.java).apply {
                putExtra(MediaService.EXTRA_IS_PLAYING, isPlaying)
                putExtra(MediaService.EXTRA_TITLE, viewModel?.getCurrentTitle() ?: "Book Reader")
            }
            
            Log.d("MediaManager", "Updating media notification with isPlaying=$isPlaying")
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            Log.e("MediaManager", "Error updating media notification: ${e.message}")
        }
    }
    
    fun stopMediaService() {
        try {
            context.stopService(Intent(context, MediaService::class.java))
        } catch (e: Exception) {
            Log.e("MediaManager", "Error stopping media service: ${e.message}")
        }
    }
    
    fun cleanup() {
        MediaControlReceiver.removeListener()
        stopMediaService()
    }
    
    // MediaControlListener implementation
    override fun onPlayPauseClicked() {
        viewModel?.togglePlayPause()
        updateMediaNotification()
    }
    
    override fun onNextClicked() {
        viewModel?.nextParagraph()
        updateMediaNotification()
    }
    
    override fun onPreviousClicked() {
        viewModel?.previousParagraph()
        updateMediaNotification()
    }
    
    override fun onStopClicked() {
        viewModel?.stop()
        stopMediaService()
    }
} 