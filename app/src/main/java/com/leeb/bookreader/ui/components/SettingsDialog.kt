package com.leeb.bookreader.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.leeb.bookreader.R
import com.leeb.bookreader.model.AppSettings
import java.util.Locale

// Define font families using bundled fonts instead of Google Fonts
private val robotoFontFamily = FontFamily(
    Font(R.font.roboto_light, FontWeight.Light),
    Font(R.font.roboto_regular, FontWeight.Normal),
    Font(R.font.roboto_bold, FontWeight.Bold)
)

private val openSansFontFamily = FontFamily(
    Font(R.font.opensans_light, FontWeight.Light),
    Font(R.font.opensans_regular, FontWeight.Normal),
    Font(R.font.opensans_bold, FontWeight.Bold)
)

private val montserratFontFamily = FontFamily(
    Font(R.font.montserrat_light, FontWeight.Light),
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

private val caveatFontFamily = FontFamily(
    Font(R.font.caveat_regular, FontWeight.Normal),
    Font(R.font.caveat_bold, FontWeight.Bold)
)

private val satisfyFontFamily = FontFamily(
    Font(R.font.satisfy_regular, FontWeight.Normal)
)

private val gloriaHallelujahFontFamily = FontFamily(
    Font(R.font.gloria_hallelujah_regular, FontWeight.Normal)
)

private val handleeFontFamily = FontFamily(
    Font(R.font.handlee_regular, FontWeight.Normal)
)

private val juraFontFamily = FontFamily(
    Font(R.font.jura_light, FontWeight.Light),
    Font(R.font.jura_regular, FontWeight.Normal),
    Font(R.font.jura_bold, FontWeight.Bold)
)

// Helper function to get the appropriate font weight for fonts that might not have Light variant
private fun getLightWeightForFont(fontName: String): FontWeight {
    return when (fontName) {
        "Caveat", "Satisfy", "Gloria Hallelujah", "Handlee" -> FontWeight.Normal
        else -> FontWeight.Light
    }
}

@Composable
fun SettingsDialog(
    settings: AppSettings,
    defaultValues: () -> Unit,
    onDismiss: () -> Unit,
    onUrlChange: (String) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onFontColorChange: (Int) -> Unit,
    onBackgroundColorChange: (Int) -> Unit,
    onSpeechRateChange: (Float) -> Unit = {},
    onVoiceLocaleChange: (String) -> Unit = {},
    onFontFamilyChange: (String) -> Unit = {},
    onFontWeightChange: (FontWeight) -> Unit = {},
    onLoadContent: () -> Unit,
    availableLocales: List<Locale> = listOf(Locale.US)
) {
    var url by remember { mutableStateOf(settings.url) }
    var showVoiceDropdown by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    
    // Tab state
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Appearance", "TTS", "Import")
    
    Dialog(
        onDismissRequest = onDismiss, 
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(elevation = 24.dp, shape = MaterialTheme.shapes.large),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                ) 
                            }
                        )
                    }
                }
                
                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .padding(top = 16.dp, bottom = 8.dp)
                        .verticalScroll(scrollState)
                ) {
                    when (selectedTabIndex) {
                        0 -> AppearanceTab(
                            settings = settings,
                            onFontSizeChange = onFontSizeChange,
                            onFontColorChange = onFontColorChange,
                            onBackgroundColorChange = onBackgroundColorChange,
                            onFontFamilyChange = onFontFamilyChange,
                            onFontWeightChange = onFontWeightChange
                        )
                        1 -> TTSTab(
                            settings = settings,
                            onSpeechRateChange = onSpeechRateChange,
                            onVoiceLocaleChange = onVoiceLocaleChange,
                            availableLocales = availableLocales,
                            showVoiceDropdown = showVoiceDropdown,
                            onShowVoiceDropdownChange = { showVoiceDropdown = it }
                        )
                        2 -> ImportTab(
                            url = url,
                            onUrlChange = { 
                                url = it
                                onUrlChange(it)
                            },
                            onLoadContent = onLoadContent
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            defaultValues()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Restore Defaults")
                    }
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun AppearanceTab(
    settings: AppSettings,
    onFontSizeChange: (Float) -> Unit,
    onFontColorChange: (Int) -> Unit,
    onBackgroundColorChange: (Int) -> Unit,
    onFontFamilyChange: (String) -> Unit,
    onFontWeightChange: (FontWeight) -> Unit = {}
) {
    var showFontDropdown by remember { mutableStateOf(false) }
    var showFontWeightDropdown by remember { mutableStateOf(false) }
    
    // Alphabetically ordered font options
    val fontOptions = listOf(
        "Default",
        "Caveat",
        "Gloria Hallelujah",
        "Handlee",
        "Jura",
        "Monospace",
        "Montserrat",
        "Open Sans",
        "Roboto",
        "Sans Serif",
        "Satisfy",
        "Serif"
    )
    
    // Simplified font weight options
    val fontWeightOptions = listOf(
        "Light" to FontWeight.Light,
        "Normal" to FontWeight.Normal,
        "Bold" to FontWeight.Bold
    )
    
    // Get current font weight from settings, with fallback handling
    val currentFontWeight = when (settings.fontWeight) {
        FontWeight.Light -> "Light"
        FontWeight.Bold -> "Bold"
        else -> "Normal"
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Font Family Selection
        Text(
            "Font Family",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        Box(modifier = Modifier.fillMaxWidth()) {
            // Current selected font display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable { showFontDropdown = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display the current font name using its own font family and weight
                Text(
                    settings.fontFamily,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = getFontFamily(settings.fontFamily),
                    fontWeight = FontWeight.Normal
                )
                
                Icon(
                    painter = painterResource(id = R.drawable.round_arrow_drop_down_24),
                    contentDescription = "Select Font",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Dropdown menu for font selection
            DropdownMenu(
                expanded = showFontDropdown,
                onDismissRequest = { showFontDropdown = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                fontOptions.forEach { font ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                font,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = getFontFamily(font),
                                fontWeight = FontWeight.Normal
                            ) 
                        },
                        onClick = {
                            onFontFamilyChange(font)
                            showFontDropdown = false
                        }
                    )
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Font Weight Selection
        Text(
            "Font Weight",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        Box(modifier = Modifier.fillMaxWidth()) {
            // Current selected font weight display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable { showFontWeightDropdown = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    currentFontWeight,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal
                )
                
                Icon(
                    painter = painterResource(id = R.drawable.round_arrow_drop_down_24),
                    contentDescription = "Select Font Weight",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Dropdown menu for font weight selection
            DropdownMenu(
                expanded = showFontWeightDropdown,
                onDismissRequest = { showFontWeightDropdown = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                fontWeightOptions.forEach { (name, weight) ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal
                            ) 
                        },
                        onClick = {
                            onFontWeightChange(weight)
                            showFontWeightDropdown = false
                        }
                    )
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Font Size Slider
        SettingSlider(
            title = "Font Size",
            value = settings.fontSize,
            onValueChange = onFontSizeChange,
            valueRange = 14f..30f,
            valueDisplay = "${settings.fontSize.toInt()}"
        )

        Spacer(Modifier.height(24.dp))
        
        // Font Color Selection
        Text(
            "Font Color",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
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
        
        Spacer(Modifier.height(24.dp))
        
        // Background Color Selection
        Text(
            "Background Color",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
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
}

// Helper function to get the FontFamily for a given font name
@Composable
fun getFontFamily(fontName: String): FontFamily {
    return when (fontName) {
        "Sans Serif" -> FontFamily.SansSerif
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        "Roboto" -> robotoFontFamily
        "Open Sans" -> openSansFontFamily
        "Montserrat" -> montserratFontFamily
        "Caveat" -> caveatFontFamily
        "Satisfy" -> satisfyFontFamily
        "Gloria Hallelujah" -> gloriaHallelujahFontFamily
        "Handlee" -> handleeFontFamily
        "Jura" -> juraFontFamily
        else -> FontFamily.Default
    }
}

@Composable
fun TTSTab(
    settings: AppSettings,
    onSpeechRateChange: (Float) -> Unit,
    onVoiceLocaleChange: (String) -> Unit,
    availableLocales: List<Locale>,
    showVoiceDropdown: Boolean,
    onShowVoiceDropdownChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Voice Selection
        Text(
            "Voice",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        Box(modifier = Modifier.fillMaxWidth()) {
            // Current selected locale display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onShowVoiceDropdownChange(true) }
                    .padding(16.dp),
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
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Icon(
                    painter = painterResource(id = R.drawable.round_arrow_drop_down_24),
                    contentDescription = "Select Voice",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Dropdown menu for voice selection
            DropdownMenu(
                expanded = showVoiceDropdown,
                onDismissRequest = { onShowVoiceDropdownChange(false) },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                availableLocales.forEach { locale ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                locale.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        onClick = {
                            onVoiceLocaleChange(locale.toLanguageTag())
                            onShowVoiceDropdownChange(false)
                        }
                    )
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))

        
        // Speech Rate Slider
        SettingSlider(
            title = "Speech Rate",
            value = settings.speechRate,
            onValueChange = onSpeechRateChange,
            valueRange = 0.5f..2.0f,
            valueDisplay = "%.2f".format(settings.speechRate),
            steps = 30
        )
    }
}

@Composable
fun ImportTab(
    url: String,
    onUrlChange: (String) -> Unit,
    onLoadContent: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // URL Input
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text("Content URL") },
            placeholder = { Text("Enter URL", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
            ),
            shape = MaterialTheme.shapes.small,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(Modifier.height(16.dp))

        // Load Content Button
        Button(
            onClick = onLoadContent,
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Load Content")
        }
        
        // Future: Add file picker for loading from device
        Spacer(Modifier.height(24.dp))
        
        Text(
            "Coming Soon: Import from device",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SettingSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueDisplay: String,
    steps: Int = 0
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                valueDisplay,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Animate the border color and width for a more dynamic feel
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 300)
    )
    
    val borderWidth by animateFloatAsState(
        targetValue = if (isSelected) 3f else 0f,
        animationSpec = tween(durationMillis = 200)
    )
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .border(
                width = borderWidth.dp,
                color = borderColor,
                shape = CircleShape
            )
    )
}