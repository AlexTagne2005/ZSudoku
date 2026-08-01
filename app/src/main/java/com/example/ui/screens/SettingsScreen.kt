package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsEntity,
    onSettingsChanged: (SettingsEntity) -> Unit,
    onBack: () -> Unit,
    onOpenGesturePractice: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Zen Experience",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            SettingSwitchRow(
                title = "Dark Theme",
                subtitle = "Night blue and dark slate tones for low light",
                checked = settings.isDarkMode,
                testTag = "switch_dark_theme",
                onCheckedChange = { onSettingsChanged(settings.copy(isDarkMode = it)) }
            )

            SettingSwitchRow(
                title = "Show Timer",
                subtitle = "Hidden by default in Zen mode to remove stress",
                checked = settings.isTimerVisible,
                testTag = "switch_show_timer",
                onCheckedChange = { onSettingsChanged(settings.copy(isTimerVisible = it)) }
            )

            SettingSwitchRow(
                title = "Highlight Errors",
                subtitle = "Indicative error checks without failure penalties",
                checked = settings.isErrorCheckingEnabled,
                testTag = "switch_highlight_errors",
                onCheckedChange = { onSettingsChanged(settings.copy(isErrorCheckingEnabled = it)) }
            )

            Text(
                text = "Thèmes Visuals",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            ThemeSelectionRow(
                currentTheme = settings.colorTheme,
                onThemeSelected = { newTheme ->
                    onSettingsChanged(settings.copy(colorTheme = newTheme))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Game Controls & Notes",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            SettingSwitchRow(
                title = "Auto-Clear Notes",
                subtitle = "Automatically erase pencil notes in row/col/block",
                checked = settings.autoClearNotes,
                testTag = "switch_auto_clear_notes",
                onCheckedChange = { onSettingsChanged(settings.copy(autoClearNotes = it)) }
            )

            SettingSwitchRow(
                title = "Sound & Haptics",
                subtitle = "Soothing tactile feedback on taps",
                checked = settings.isHapticsEnabled,
                testTag = "switch_sound_haptics",
                onCheckedChange = { onSettingsChanged(settings.copy(isHapticsEnabled = it, isSoundEnabled = it)) }
            )

            SettingSwitchRow(
                title = "Difficulté Adaptative Intelligente",
                subtitle = "Ajuste subtilement les contraintes selon vos performances",
                checked = settings.isAdaptiveDifficultyEnabled,
                testTag = "switch_adaptive_difficulty",
                onCheckedChange = { onSettingsChanged(settings.copy(isAdaptiveDifficultyEnabled = it)) }
            )

            if (onOpenGesturePractice != null) {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.OutlinedButton(
                    onClick = onOpenGesturePractice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_gesture_practice_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("🖐️ Entraînement interactif aux Gestes", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Style d'Affichage des Notes",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            NotesStyleSelectionRow(
                currentStyle = settings.notesStyle,
                onStyleSelected = { newStyle ->
                    onSettingsChanged(settings.copy(notesStyle = newStyle))
                }
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun ThemeSelectionRow(
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf(
        Triple("ZEN", "Zen", androidx.compose.ui.graphics.Color(0xFF386641)),
        Triple("FOREST", "Forêt", androidx.compose.ui.graphics.Color(0xFF2D5A27)),
        Triple("OCEAN", "Océan", androidx.compose.ui.graphics.Color(0xFF0284C7)),
        Triple("SUNSET", "Sunset", androidx.compose.ui.graphics.Color(0xFFC2410C)),
        Triple("OLED", "OLED", androidx.compose.ui.graphics.Color(0xFF00E5FF))
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        themes.forEach { (id, label, color) ->
            val isSelected = currentTheme == id
            Surface(
                onClick = { onThemeSelected(id) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("theme_btn_$id"),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .height(20.dp)
                            .fillMaxWidth(0.5f)
                            .background(color, shape = RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun NotesStyleSelectionRow(
    currentStyle: String,
    onStyleSelected: (String) -> Unit
) {
    val styles = listOf(
        Pair("GRID", "⏹️ Grille (3x3)"),
        Pair("CENTERED", "🔢 Liste centrée")
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        styles.forEach { (id, label) ->
            val isSelected = currentStyle == id
            Surface(
                onClick = { onStyleSelected(id) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("notes_style_btn_$id"),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

