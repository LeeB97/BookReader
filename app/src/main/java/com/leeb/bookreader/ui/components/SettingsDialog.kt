package com.leeb.bookreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.leeb.bookreader.model.AppSettings

@Composable
fun SettingsDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onUrlChange: (String) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onFontColorChange: (Int) -> Unit,
    onBackgroundColorChange: (Int) -> Unit,
    onLoadContent: () -> Unit
) {
    var url by remember { mutableStateOf(settings.url) }
    
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties()) {
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
                    onUrlChange(it)
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
            
            Button(onClick = onLoadContent) {
                Text("Load Content")
            }
            
            Spacer(Modifier.height(10.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Font Size:", color = Color.White)
                Slider(
                    value = settings.fontSize,
                    onValueChange = onFontSizeChange,
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
                    ColorOption(
                        color = Color.White,
                        isSelected = settings.fontColor == Color.White.toArgb(),
                        onClick = { onFontColorChange(Color.White.toArgb()) }
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
            
            Spacer(Modifier.height(8.dp))
            
            Button(onClick = onDismiss) {
                Text("Save")
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
            .background(color)
            .clickable(onClick = onClick)
            .border(
                width = 2.dp,
                color = if (isSelected) Color.Yellow else Color.Transparent
            )
    )
} 