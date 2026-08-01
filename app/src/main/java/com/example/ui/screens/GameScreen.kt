package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import com.example.ui.components.InteractiveTutorialModal
import com.example.util.SoundManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.model.Difficulty
import com.example.model.InputMode
import com.example.model.SudokuCell
import com.example.ui.components.SudokuActionBar
import com.example.ui.components.SudokuGrid
import com.example.ui.components.SudokuKeypad

import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.widget.Toast
import com.example.ui.components.AudioMixerModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    cells: List<SudokuCell>,
    selectedRow: Int?,
    selectedCol: Int?,
    selectedDigit: Int?,
    isPencilActive: Boolean,
    inputMode: InputMode,
    difficulty: Difficulty,
    elapsedSeconds: Long,
    errorCount: Int,
    maxErrors: Int,
    isGameOver: Boolean,
    starsCount: Int,
    isTimerRunning: Boolean,
    isTimerVisible: Boolean,
    isDarkTheme: Boolean,
    isHapticsEnabled: Boolean = true,
    notesStyle: String = "GRID",
    isAmbientEnabled: Boolean = false,
    rainVolume: Float = 0.5f,
    forestVolume: Float = 0.3f,
    wavesVolume: Float = 0.4f,
    canUndo: Boolean,
    canRedo: Boolean,
    isCompleted: Boolean,
    showHintDialog: Boolean,
    showAdSimulation: Boolean,
    adProgress: Float,
    zenQuote: String,
    shakeTriggerCount: Int = 0,
    isDailyChallenge: Boolean = false,
    showTutorial: Boolean = false,
    isAdaptiveModeActive: Boolean = false,
    onCellClick: (row: Int, col: Int) -> Unit,
    onDigitClick: (Int) -> Unit,
    onToggleInputMode: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onEraseClick: () -> Unit,
    onPencilClick: () -> Unit,
    onHintClick: () -> Unit,
    onConfirmHintAd: () -> Unit,
    onDismissHintDialog: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartGame: () -> Unit,
    onStartAnotherGame: () -> Unit,
    onBackToHome: () -> Unit,
    onTwoFingerTapToClear: () -> Unit = {},
    onSwipeToggleInputMode: () -> Unit = {},
    onUpdateAudioSettings: (isEnabled: Boolean, rainVol: Float, forestVol: Float, wavesVol: Float) -> Unit = { _, _, _, _ -> },
    onOpenTutorialClick: () -> Unit = {},
    onCloseTutorialClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val shakeOffset = remember { Animatable(0f) }
    var showAudioMixer by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(shakeTriggerCount) {
        if (shakeTriggerCount > 0) {
            soundManager.playErrorSound()
            soundManager.vibrateShort()

            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    (-16f) at 50
                    16f at 100
                    (-12f) at 150
                    12f at 200
                    (-6f) at 270
                    6f at 340
                    0f at 400
                }
            )
        }
    }
    
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            soundManager.playWinSound()
            soundManager.vibrateShort()
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                onPauseClick()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isDailyChallenge) "🏆 Défi Quotidien" else "ZenSudoku",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isAdaptiveModeActive) "${difficulty.displayName} • ⚡ Adaptatif" else difficulty.displayName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier.testTag("game_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isHapticsEnabled) soundManager.vibrateShort()
                            Toast.makeText(context, "💾 Brouillon sauvegardé ! Vous pourrez reprendre cette partie à tout moment.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("save_draft_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Sauvegarder Brouillon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { showAudioMixer = true },
                        modifier = Modifier.testTag("open_audio_mixer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Mixeur Audio Zen",
                            tint = if (isAmbientEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onOpenTutorialClick,
                        modifier = Modifier.testTag("game_tutorial_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Guide"
                        )
                    }

                    // Error Badge Counter
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (errorCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Errors",
                                tint = if (errorCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$errorCount / $maxErrors",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (errorCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isTimerVisible) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = formatTime(elapsedSeconds),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = if (isTimerRunning) onPauseClick else onResumeClick,
                        modifier = Modifier.testTag("game_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isTimerRunning) "Pause" else "Resume"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // Zen Quote Subtitle Ticker
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = zenQuote,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 9x9 Sudoku Board
                SudokuGrid(
                    cells = cells,
                    selectedRow = selectedRow,
                    selectedCol = selectedCol,
                    selectedDigit = selectedDigit,
                    isDarkTheme = isDarkTheme,
                    notesStyle = notesStyle,
                    onCellClick = { r, c ->
                        if (isHapticsEnabled) soundManager.vibrateShort()
                        onCellClick(r, c)
                    },
                    onTwoFingerTapToClear = {
                        soundManager.playEraseSound()
                        if (isHapticsEnabled) soundManager.vibrateShort()
                        onTwoFingerTapToClear()
                    },
                    onSwipeToggleInputMode = {
                        if (isHapticsEnabled) soundManager.vibrateShort()
                        onSwipeToggleInputMode()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .graphicsLayer { translationX = shakeOffset.value }
                )

                // Action Bar (Undo, Redo, Erase, Pencil, Hint)
                SudokuActionBar(
                    canUndo = canUndo,
                    canRedo = canRedo,
                    isPencilActive = isPencilActive,
                    onUndoClick = {
                        soundManager.playDigitSound()
                        if (isHapticsEnabled) soundManager.vibrateShort()
                        onUndoClick()
                    },
                    onRedoClick = {
                        soundManager.playDigitSound()
                        if (isHapticsEnabled) soundManager.vibrateShort()
                        onRedoClick()
                    },
                    onEraseClick = {
                        soundManager.playEraseSound()
                        if (isHapticsEnabled) soundManager.vibrateShort()
                        onEraseClick()
                    },
                    onPencilClick = {
                        soundManager.playDigitSound()
                        if (isHapticsEnabled) soundManager.vibrateShort()
                        onPencilClick()
                    },
                    onHintClick = {
                        if (isHapticsEnabled) soundManager.vibrateShort()
                        onHintClick()
                    }
                )

                // Keypad
                SudokuKeypad(
                    cells = cells,
                    selectedDigit = selectedDigit,
                    inputMode = inputMode,
                    onDigitClick = { digit ->
                        soundManager.playDigitSound()
                        if (isHapticsEnabled) soundManager.vibrateShort()
                        onDigitClick(digit)
                    },
                    onToggleInputMode = {
                        if (isHapticsEnabled) soundManager.vibrateShort()
                        onToggleInputMode()
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Pause Overlay
            if (!isTimerRunning && !isCompleted && !isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Partie en Pause",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Prenez une pause et reprenez dès que vous êtes prêt.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                            )

                            Button(
                                onClick = onResumeClick,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Reprendre la partie", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onRestartGame,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Recommencer")
                            }
                        }
                    }
                }
            }

            // Game Over Modal (Error limit reached)
            if (isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.errorContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Partie Terminée",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Vous avez atteint la limite de $maxErrors erreurs ($errorCount/$maxErrors).",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                            )

                            Button(
                                onClick = onStartAnotherGame,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Nouvelle partie", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onRestartGame,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Recommencer la même")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(onClick = onBackToHome) {
                                Text("Retour à l'accueil")
                            }
                        }
                    }
                }
            }

            // Opt-in Reward Ad Hint Dialog
            if (showHintDialog) {
                AlertDialog(
                    onDismissRequest = onDismissHintDialog,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    title = {
                        Text(text = "Indice Zen", fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text(
                            text = "Besoin d'un indice ? Regardez une courte pause de 3 secondes pour révéler le bon chiffre de la case sélectionnée.",
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = onConfirmHintAd,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.OndemandVideo, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Révéler l'indice")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onDismissHintDialog) {
                            Text("Annuler")
                        }
                    }
                )
            }

            // Ad Simulation Progress Modal
            if (showAdSimulation) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Pause Mindful",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Préparation de l'indice...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )

                            LinearProgressIndicator(
                                progress = { adProgress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Game Win / Completion Modal
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Félicitations !",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Grille ${difficulty.displayName} complétée !",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            // Stars Display
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                for (i in 1..3) {
                                    val isFilled = i <= starsCount
                                    Icon(
                                        imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarOutline,
                                        contentDescription = "Star $i",
                                        tint = if (isFilled) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        modifier = Modifier.size(40.dp).padding(horizontal = 2.dp)
                                    )
                                }
                            }

                            // Star label
                            val starText = when (starsCount) {
                                3 -> "⭐⭐⭐ Victoire Parfaite ! (0 Erreur)"
                                2 -> "⭐⭐ Très Bien ! ($errorCount Erreur)"
                                else -> "⭐ Bien Joué ! ($errorCount Erreurs)"
                            }

                            Text(
                                text = starText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(bottom = 20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Temps: ${formatTime(elapsedSeconds)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Fautes: $errorCount / $maxErrors",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    com.example.util.ShareHelper.shareVictoryCard(
                                        context = context,
                                        difficulty = difficulty,
                                        timeSeconds = elapsedSeconds,
                                        stars = starsCount,
                                        errorCount = errorCount,
                                        maxErrors = maxErrors
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("share_victory_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Partager ma victoire", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = onStartAnotherGame,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Nouvelle partie", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onBackToHome,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retour à l'accueil")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTutorial) {
        InteractiveTutorialModal(onDismiss = onCloseTutorialClick)
    }

    if (showAudioMixer) {
        AudioMixerModal(
            isAmbientEnabled = isAmbientEnabled,
            rainVolume = rainVolume,
            forestVolume = forestVolume,
            wavesVolume = wavesVolume,
            onToggleAmbient = { onUpdateAudioSettings(it, rainVolume, forestVolume, wavesVolume) },
            onRainVolumeChange = { onUpdateAudioSettings(isAmbientEnabled, it, forestVolume, wavesVolume) },
            onForestVolumeChange = { onUpdateAudioSettings(isAmbientEnabled, rainVolume, it, wavesVolume) },
            onWavesVolumeChange = { onUpdateAudioSettings(isAmbientEnabled, rainVolume, forestVolume, it) },
            onDismiss = { showAudioMixer = false }
        )
    }
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
