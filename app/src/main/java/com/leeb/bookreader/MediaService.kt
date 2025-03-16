package com.leeb.bookreader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver

class MediaService : LifecycleService() {
    companion object {
        const val CHANNEL_ID = "BookReaderMediaChannel"
        const val NOTIFICATION_ID = 1
        
        // Action constants for media controls
        const val ACTION_PLAY_PAUSE = "com.leeb.bookreader.PLAY_PAUSE"
        const val ACTION_NEXT = "com.leeb.bookreader.NEXT"
        const val ACTION_PREVIOUS = "com.leeb.bookreader.PREVIOUS"
        const val ACTION_STOP = "com.leeb.bookreader.STOP"
        
        // Extra constants
        const val EXTRA_TITLE = "com.leeb.bookreader.TITLE"
        const val EXTRA_IS_PLAYING = "com.leeb.bookreader.IS_PLAYING"
    }
    
    private lateinit var mediaSession: MediaSessionCompat
    private var isPlaying = false
    private var title = "Book Reader"
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Create notification channel for Android O and above
            createNotificationChannel()
            
            // Initialize MediaSession
            mediaSession = MediaSessionCompat(this, "BookReaderMediaSession")
            mediaSession.setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    broadcastAction(ACTION_PLAY_PAUSE)
                }
                
                override fun onPause() {
                    broadcastAction(ACTION_PLAY_PAUSE)
                }
                
                override fun onSkipToNext() {
                    broadcastAction(ACTION_NEXT)
                }
                
                override fun onSkipToPrevious() {
                    broadcastAction(ACTION_PREVIOUS)
                }
                
                override fun onStop() {
                    broadcastAction(ACTION_STOP)
                }
            })
        } catch (e: Exception) {
            Log.e("MediaService", "Error in onCreate: ${e.message}")
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        try {
            // Handle media button events
            if (intent != null) {
                MediaButtonReceiver.handleIntent(mediaSession, intent)
                
                // Update state based on intent extras
                if (intent.hasExtra(EXTRA_IS_PLAYING)) {
                    isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
                }
                
                if (intent.hasExtra(EXTRA_TITLE)) {
                    title = intent.getStringExtra(EXTRA_TITLE) ?: "Book Reader"
                }
                
                // Update notification
                startForeground(NOTIFICATION_ID, buildNotification())
            }
        } catch (e: Exception) {
            Log.e("MediaService", "Error in onStartCommand: ${e.message}")
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
    
    override fun onDestroy() {
        try {
            mediaSession.release()
        } catch (e: Exception) {
            Log.e("MediaService", "Error in onDestroy: ${e.message}")
        }
        super.onDestroy()
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Book Reader Media Controls",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Media controls for Book Reader TTS"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun buildNotification(): Notification {
        try {
            // Create pending intents for actions
            val playPauseIntent = PendingIntent.getBroadcast(
                this,
                0,
                MediaControlReceiver.createExplicitIntent(this, ACTION_PLAY_PAUSE),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val nextIntent = PendingIntent.getBroadcast(
                this,
                1,
                MediaControlReceiver.createExplicitIntent(this, ACTION_NEXT),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val previousIntent = PendingIntent.getBroadcast(
                this,
                2,
                MediaControlReceiver.createExplicitIntent(this, ACTION_PREVIOUS),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val stopIntent = PendingIntent.getBroadcast(
                this,
                3,
                MediaControlReceiver.createExplicitIntent(this, ACTION_STOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Create content intent to open the app
            val contentIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Create media style
            val mediaStyle = MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0, 1, 2)
            
            // Build the notification
            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.round_menu_book_24)
                .setContentTitle(title)
                .setContentText(if (isPlaying) "Playing" else "Paused")
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(
                    R.drawable.round_skip_previous_24,
                    "Previous",
                    previousIntent
                )
                .addAction(
                    if (isPlaying) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24,
                    if (isPlaying) "Pause" else "Play",
                    playPauseIntent
                )
                .addAction(
                    R.drawable.round_skip_next_24,
                    "Next",
                    nextIntent
                )
                .addAction(
                    R.drawable.round_stop_24,
                    "Stop",
                    stopIntent
                )
                .setStyle(mediaStyle)
                .setColorized(true)
                .setColor(ContextCompat.getColor(this, R.color.notification_background))
                .build()
        } catch (e: Exception) {
            Log.e("MediaService", "Error building notification: ${e.message}")
            // Return a simple notification as fallback
            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.round_menu_book_24)
                .setContentTitle("Book Reader")
                .setContentText("Running")
                .build()
        }
    }
    
    private fun broadcastAction(action: String) {
        try {
            val intent = MediaControlReceiver.createExplicitIntent(this, action)
            sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e("MediaService", "Error broadcasting action: ${e.message}")
        }
    }
} 