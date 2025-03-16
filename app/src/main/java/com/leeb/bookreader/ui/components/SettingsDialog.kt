package com.leeb.bookreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.leeb.bookreader.R
import com.leeb.bookreader.model.AppSettings
import java.util.Locale

@Composable
fun SettingsDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    defualtValues: () -> Unit,
    onUrlChange: (String) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onFontColorChange: (Int) -> Unit,
    onBackgroundColorChange: (Int) -> Unit,
    onSpeechRateChange: (Float) -> Unit = {},
    onVoiceLocaleChange: (String) -> Unit = {},
    onLoadContent: () -> Unit,
    availableLocales: List<Locale> = listOf(Locale.US)
) {
    var url by remember { mutableStateOf(settings.url) }
    var showVoiceDropdown by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray, shape = MaterialTheme.shapes.large)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            TextField(
                value = url,
                onValueChange = { 
                    url = it
                    onUrlChange(it)
                },
                placeholder = { Text("Enter URL", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black
                ),
                shape = MaterialTheme.shapes.small
            )
            
            Spacer(Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onLoadContent) {
                    Text("Load Content")
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Font Size Slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Font Size:", color = Color.White, modifier = Modifier.width(100.dp))
                Slider(
                    value = settings.fontSize,
                    onValueChange = onFontSizeChange,
                    valueRange = 14f..30f,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${settings.fontSize.toInt()}",
                    color = Color.White,
                    modifier = Modifier.width(30.dp),
                    textAlign = TextAlign.End
                )
            }

            Spacer(Modifier.height(12.dp))
            
            // Speech Rate Slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Speech Rate:", color = Color.White, modifier = Modifier.width(100.dp))
                Slider(
                    value = settings.speechRate,
                    onValueChange = onSpeechRateChange,
                    valueRange = 0.5f..2.0f,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "%.1f".format(settings.speechRate),
                    color = Color.White,
                    modifier = Modifier.width(30.dp),
                    textAlign = TextAlign.End
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Voice Selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voice:", color = Color.White, modifier = Modifier.width(100.dp))
                
                Box(modifier = Modifier.weight(1f)) {
                    // Current selected locale display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, MaterialTheme.shapes.small)
                            .border(1.dp, Color.Gray, MaterialTheme.shapes.small)
                            .clickable { showVoiceDropdown = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentLocale = try {
                            Locale.forLanguageTag(settings.voiceLocale).displayName
                        } catch (e: Exception) {
                            "English (US)"
                        }
                        
                        Text(
                            currentLocale,
                            color = Color.White
                        )
                        
                        Icon(
                            painter = painterResource(id = R.drawable.round_arrow_drop_down_24),
                            contentDescription = "Select Voice",
                            tint = Color.White
                        )
                    }
                    
                    // Dropdown menu for voice selection
                    DropdownMenu(
                        expanded = showVoiceDropdown,
                        onDismissRequest = { showVoiceDropdown = false },
                        modifier = Modifier.background(Color.DarkGray)
                    ) {
                        availableLocales.forEach { locale ->
                            DropdownMenuItem(
                                text = { Text(locale.displayName, color = Color.White) },
                                onClick = {
                                    onVoiceLocaleChange(locale.toLanguageTag())
                                    showVoiceDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Font Color Selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Font Color:", color = Color.White, modifier = Modifier.width(100.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ColorOption(
                        color = Color.White,
                        isSelected = settings.fontColor == Color.White.toArgb(),
                        onClick = { onFontColorChange(Color.White.toArgb()) },
                    )
                    ColorOption(
                        color = Color.Black,
                        isSelected = settings.fontColor == Color.Black.toArgb(),
                        onClick = { onFontColorChange(Color.Black.toArgb()) }
                    )
                    ColorOption(
                        color = Color.Gray,
                        isSelected = settings.fontColor == Color.Gray.toArgb(),
                        onClick = { onFontColorChange(Color.Gray.toArgb()) }
                    )
                    ColorOption(
                        color = Color.Yellow,
                        isSelected = settings.fontColor == Color.Yellow.toArgb(),
                        onClick = { onFontColorChange(Color.Yellow.toArgb()) }
                    )
                    ColorOption(
                        color = Color.Green,
                        isSelected = settings.fontColor == Color.Green.toArgb(),
                        onClick = { onFontColorChange(Color.Green.toArgb()) }
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Background Color Selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)

            ) {
                Text("BG Color:", color = Color.White, modifier = Modifier.width(100.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ColorOption(
                        color = Color.Black,
                        isSelected = settings.backgroundColor == Color.Black.toArgb(),
                        onClick = { onBackgroundColorChange(Color.Black.toArgb()) }
                    )
                    ColorOption(
                        color = Color.White,
                        isSelected = settings.backgroundColor == Color.White.toArgb(),
                        onClick = { onBackgroundColorChange(Color.White.toArgb()) }
                    )
                    ColorOption(
                        color = Color.DarkGray,
                        isSelected = settings.backgroundColor == Color.DarkGray.toArgb(),
                        onClick = { onBackgroundColorChange(Color.DarkGray.toArgb()) }
                    )
                    ColorOption(
                        color = Color.Blue.copy(alpha = 0.7f),
                        isSelected = settings.backgroundColor == Color.Blue.copy(alpha = 0.7f).toArgb(),
                        onClick = { onBackgroundColorChange(Color.Blue.copy(alpha = 0.7f).toArgb()) }
                    )
                    ColorOption(
                        color = Color(0xFF2D2D2D),
                        isSelected = settings.backgroundColor == Color(0xFF2D2D2D).toArgb(),
                        onClick = { onBackgroundColorChange(Color(0xFF2D2D2D).toArgb()) }
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    defualtValues()
                    onDismiss()
                }) {
                    Text("Restore Defaults")
                }
                
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(color, shape = MaterialTheme.shapes.extraSmall)
            .clickable(onClick = onClick)
            .border(
                width = 2.dp,
                color = if (isSelected) Color.Yellow else {
                    Color.Gray.copy(alpha = 0.3f)
                },
                shape = MaterialTheme.shapes.extraSmall
            )
    )
}