package com.leeb.bookreader

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d("SplashActivity", "Starting SplashActivity")
            
            setContent {
                SplashScreen()
            }
            
            // Delay for 3 seconds then start MainActivity
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    Log.d("SplashActivity", "Attempting to start MainActivity")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e("SplashActivity", "Error starting MainActivity: ${e.message}", e)
                    e.printStackTrace()
                    Toast.makeText(this, "Error starting app: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }, 3000)
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error in onCreate: ${e.message}", e)
            e.printStackTrace()
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun SplashScreen() {
    val statusMessage = remember { mutableStateOf("Loading...") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Book Reader",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
            
            Text(
                text = statusMessage.value,
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
} 