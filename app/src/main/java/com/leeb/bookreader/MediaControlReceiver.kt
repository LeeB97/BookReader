package com.leeb.bookreader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class MediaControlReceiver : BroadcastReceiver() {
    
    interface MediaControlListener {
        fun onPlayPauseClicked()
        fun onNextClicked()
        fun onPreviousClicked()
        fun onStopClicked()
    }
    
    companion object {
        private var listener: MediaControlListener? = null
        
        fun setListener(controlListener: MediaControlListener) {
            listener = controlListener
        }
        
        fun removeListener() {
            listener = null
        }
        
        // Create explicit intent for the receiver
        fun createExplicitIntent(context: Context, action: String): Intent {
            return Intent(action).apply {
                setClass(context, MediaControlReceiver::class.java)
                // Add the flag for foreground receiver
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            }
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        try {
            when (intent.action) {
                MediaService.ACTION_PLAY_PAUSE -> listener?.onPlayPauseClicked()
                MediaService.ACTION_NEXT -> listener?.onNextClicked()
                MediaService.ACTION_PREVIOUS -> listener?.onPreviousClicked()
                MediaService.ACTION_STOP -> listener?.onStopClicked()
                else -> Log.d("MediaControlReceiver", "Unknown action: ${intent.action}")
            }
        } catch (e: Exception) {
            Log.e("MediaControlReceiver", "Error in onReceive: ${e.message}")
        }
    }
} 