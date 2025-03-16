package com.leeb.bookreader.model

import android.graphics.Color
import java.util.Locale

data class AppSettings(
    val url: String = "https://dl.dropboxusercontent.com/s/8ndtu5xb7gr6j2p/index.html?dl=0",
    val fontSize: Float = 18f,
    val fontColor: Int = Color.WHITE,
    val backgroundColor: Int = Color.BLACK,
    val speechRate: Float = 1.0f,
    val voiceLocale: String = Locale.US.toLanguageTag()
) 