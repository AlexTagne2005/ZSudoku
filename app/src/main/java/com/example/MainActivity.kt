package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.model.Difficulty
import com.example.ui.components.BrainTierGraduationModal
import com.example.ui.components.GesturePracticeModal
import com.example.ui.components.InteractiveTutorialModal
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.ZenSudokuTheme
import com.example.viewmodel.SudokuViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SudokuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            ZenSudokuTheme(
                darkTheme = uiState.settings.isDarkMode,
                colorThemeName = uiState.settings.colorTheme
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ZenSudokuApp(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }
}

object Routes {
    const val HOME = "home"
    const val GAME = "game"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
}

@Composable
fun ZenSudokuApp(
    viewModel: SudokuViewModel,
    uiState: com.example.viewmodel.SudokuUiState
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                hasSavedGame = uiState.hasSavedGame,
                savedDifficulty = uiState.savedGameDifficulty,
                savedTime = uiState.savedGameTime,
                stats = uiState.stats,
                onNewGameSelect = { difficulty ->
                    viewModel.startNewGame(difficulty)
                    navController.navigate(Routes.GAME)
                },
                onStartDailyChallenge = {
                    viewModel.startDailyChallenge()
                    navController.navigate(Routes.GAME)
                },
                onResumeGameSelect = {
                    viewModel.resumeSavedGame()
                    navController.navigate(Routes.GAME)
                },
                onOpenStatsSelect = {
                    navController.navigate(Routes.STATISTICS)
                },
                onOpenSettingsSelect = {
                    navController.navigate(Routes.SETTINGS)
                },
                onOpenTutorial = {
                    viewModel.toggleTutorial(true)
                }
            )

            if (uiState.showTutorial) {
                InteractiveTutorialModal(
                    onDismiss = { viewModel.toggleTutorial(false) }
                )
            }
        }

        composable(Routes.GAME) {
            GameScreen(
                cells = uiState.cells,
                selectedRow = uiState.selectedRow,
                selectedCol = uiState.selectedCol,
                selectedDigit = uiState.selectedDigit,
                isPencilActive = uiState.isPencilActive,
                inputMode = uiState.inputMode,
                difficulty = uiState.currentDifficulty,
                elapsedSeconds = uiState.elapsedSeconds,
                errorCount = uiState.errorCount,
                maxErrors = uiState.maxErrors,
                isGameOver = uiState.isGameOver,
                starsCount = uiState.starsCount,
                isTimerRunning = uiState.isTimerRunning,
                isTimerVisible = uiState.settings.isTimerVisible,
                isDarkTheme = uiState.settings.isDarkMode || uiState.settings.colorTheme == "OLED",
                isHapticsEnabled = uiState.settings.isHapticsEnabled,
                notesStyle = uiState.settings.notesStyle,
                isAmbientEnabled = uiState.settings.isAmbientEnabled,
                rainVolume = uiState.settings.rainVolume,
                forestVolume = uiState.settings.forestVolume,
                wavesVolume = uiState.settings.wavesVolume,
                canUndo = uiState.undoStack.isNotEmpty(),
                canRedo = uiState.redoStack.isNotEmpty(),
                isCompleted = uiState.isCompleted,
                showHintDialog = uiState.showHintDialog,
                showAdSimulation = uiState.showAdSimulation,
                adProgress = uiState.adProgress,
                zenQuote = uiState.zenQuote,
                shakeTriggerCount = uiState.shakeTriggerCount,
                isDailyChallenge = uiState.isDailyChallenge,
                showTutorial = uiState.showTutorial,
                isAdaptiveModeActive = uiState.isAdaptiveModeActive,
                onCellClick = { r, c -> viewModel.onCellClicked(r, c) },
                onDigitClick = { digit -> viewModel.onKeypadDigitClicked(digit) },
                onToggleInputMode = { viewModel.toggleInputMode() },
                onUndoClick = { viewModel.undo() },
                onRedoClick = { viewModel.redo() },
                onEraseClick = { viewModel.eraseCell() },
                onPencilClick = { viewModel.togglePencil() },
                onHintClick = { viewModel.showHintPrompt() },
                onConfirmHintAd = { viewModel.triggerRewardAdAndGiveHint() },
                onDismissHintDialog = { viewModel.dismissHintDialog() },
                onPauseClick = { viewModel.pauseTimer() },
                onResumeClick = { viewModel.resumeTimer() },
                onRestartGame = { viewModel.restartCurrentGame() },
                onStartAnotherGame = { viewModel.startAnotherGame() },
                onBackToHome = {
                    viewModel.pauseTimer()
                    navController.popBackStack(Routes.HOME, false)
                },
                onTwoFingerTapToClear = { viewModel.clearSelectedCell() },
                onSwipeToggleInputMode = { viewModel.toggleInputModeBySwipe() },
                onUpdateAudioSettings = { isEnabled, rainVol, forestVol, wavesVol ->
                    viewModel.updateAudioSettings(isEnabled, rainVol, forestVol, wavesVol)
                },
                onOpenTutorialClick = { viewModel.toggleTutorial(true) },
                onCloseTutorialClick = { viewModel.toggleTutorial(false) }
            )
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen(
                stats = uiState.stats,
                cloudSyncRepository = viewModel.cloudSyncRepository,
                leaderboardFlow = viewModel.leaderboardFlow,
                onManualSync = { viewModel.syncCurrentStatsToCloud() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = uiState.settings,
                onSettingsChanged = { newSettings -> viewModel.updateSettings(newSettings) },
                onBack = { navController.popBackStack() },
                onOpenGesturePractice = { viewModel.toggleGesturePractice(true) }
            )
        }
    }

    if (uiState.showGesturePractice) {
        GesturePracticeModal(
            onDismiss = { viewModel.toggleGesturePractice(false) }
        )
    }

    uiState.graduatedBrainTierInfo?.let { brainInfo ->
        BrainTierGraduationModal(
            brainInfo = brainInfo,
            onDismiss = { viewModel.dismissBrainGraduationModal() }
        )
    }
}
