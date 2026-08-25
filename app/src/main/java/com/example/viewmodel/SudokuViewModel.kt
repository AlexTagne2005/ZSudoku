package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SettingsEntity
import com.example.data.StatsEntity
import com.example.data.SudokuRepository
import com.example.logic.SudokuGenerator
import com.example.model.BoardSnapshot
import com.example.model.Difficulty
import com.example.model.InputMode
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import com.example.model.SudokuCell
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.data.backend.CloudSyncRepository
import com.example.model.BrainEvolutionCalculator
import com.example.util.AmbientAudioMixer

data class SudokuUiState(
    val cells: PersistentList<SudokuCell> = persistentListOf(),
    val selectedRow: Int? = null,
    val selectedCol: Int? = null,
    val selectedDigit: Int? = null,
    val isPencilActive: Boolean = false,
    val inputMode: InputMode = InputMode.CELL_FIRST,
    val currentDifficulty: Difficulty = Difficulty.EASY,
    val elapsedSeconds: Long = 0L,
    val isTimerRunning: Boolean = false,
    val isCompleted: Boolean = false,
    val isGameOver: Boolean = false,
    val errorCount: Int = 0,
    val maxErrors: Int = 3,
    val starsCount: Int = 0,
    val undoStack: PersistentList<BoardSnapshot> = persistentListOf(),
    val redoStack: PersistentList<BoardSnapshot> = persistentListOf(),
    val settings: SettingsEntity = SettingsEntity(),
    val stats: StatsEntity = StatsEntity(),
    val hasSavedGame: Boolean = false,
    val savedGameDifficulty: String = "",
    val savedGameTime: Long = 0L,
    val showHintDialog: Boolean = false,
    val showAdSimulation: Boolean = false,
    val adProgress: Float = 0f,
    val zenQuote: String = "In stillness, clarity unfolds.",
    val shakeTriggerCount: Int = 0,
    val isDailyChallenge: Boolean = false,
    val dailyDateString: String = "",
    val showTutorial: Boolean = false,
    val isAdaptiveModeActive: Boolean = false,
    val showGesturePractice: Boolean = false,
    val graduatedBrainTierInfo: com.example.model.BrainLevelInfo? = null
)

class SudokuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SudokuRepository
    val cloudSyncRepository = CloudSyncRepository(application)
    val leaderboardFlow = cloudSyncRepository.getLeaderboardFlow()


    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _uiState = MutableStateFlow(SudokuUiState())
    val uiState: StateFlow<SudokuUiState> = _uiState.asStateFlow()

    val ambientAudioMixer = AmbientAudioMixer()

    private var timerJob: Job? = null

    private var saveJob: Job? = null


    private val zenQuotes = listOf(
        "In stillness, clarity unfolds.",
        "Focus on the present move.",
        "Patience turns difficulty into peace.",
        "One number at a time.",
        "A peaceful mind sees all solutions.",
        "Breathe, observe, and discover.",
        "Silence is the soil of wisdom."
    )

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SudokuRepository(db.gameDao(), db.statsDao(), db.settingsDao())

        viewModelScope.launch {
            repository.settings.collectLatest { settings ->
                _uiState.update { it.copy(settings = settings, inputMode = try { InputMode.valueOf(settings.inputModeName) } catch (e: Exception) { InputMode.CELL_FIRST }) }
                ambientAudioMixer.updateSettings(
                    isEnabled = settings.isAmbientEnabled,
                    rainVol = settings.rainVolume,
                    forestVol = settings.forestVolume,
                    wavesVol = settings.wavesVolume
                )
            }
        }

        viewModelScope.launch {
            repository.stats.collectLatest { stats ->
                _uiState.update { it.copy(stats = stats) }
                if (cloudSyncRepository.isFirebaseInitialized) {
                    val brainInfo = BrainEvolutionCalculator.calculate(stats)
                    cloudSyncRepository.syncStatsToCloud(stats, brainInfo)
                }
            }
        }

        viewModelScope.launch {
            repository.savedGame.collectLatest { saved ->
                if (saved != null && !saved.isCompleted && saved.cellsJson.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            hasSavedGame = true,
                            savedGameDifficulty = saved.difficultyName,
                            savedGameTime = saved.elapsedSeconds
                        )
                    }
                } else {
                    _uiState.update { it.copy(hasSavedGame = false) }
                }
            }
        }
    }

    fun toggleGesturePractice(show: Boolean) {
        _uiState.update { it.copy(showGesturePractice = show) }
    }

    private fun calculateAdaptiveClueOffset(difficulty: Difficulty): Int {
        val state = _uiState.value
        if (!state.settings.isAdaptiveDifficultyEnabled) return 0
        val bestTime = when (difficulty) {
            Difficulty.EASY -> state.stats.bestTimeEasy
            Difficulty.MEDIUM -> state.stats.bestTimeMedium
            Difficulty.HARD -> state.stats.bestTimeHard
            Difficulty.EXPERT -> state.stats.bestTimeExpert
        }
        val threshold = when (difficulty) {
            Difficulty.EASY -> 150L
            Difficulty.MEDIUM -> 270L
            Difficulty.HARD -> 420L
            Difficulty.EXPERT -> 600L
        }
        return if (bestTime in 1..threshold) -2 else 0
    }

    fun startNewGame(difficulty: Difficulty) {
        viewModelScope.launch {
            val offset = calculateAdaptiveClueOffset(difficulty)
            val isAdaptive = offset != 0
            val newCells = withContext(Dispatchers.Default) {
                SudokuGenerator.generatePuzzle(difficulty, adaptiveClueOffset = offset)
            }.toPersistentList().toPersistentList()
            _uiState.update {
                it.copy(
                    cells = newCells,
                    selectedRow = null,
                    selectedCol = null,
                    selectedDigit = null,
                    isPencilActive = false,
                    currentDifficulty = difficulty,


                    isCompleted = false,
                    isGameOver = false,
                    errorCount = 0,
                    starsCount = 0,
                    undoStack = persistentListOf<BoardSnapshot>(),
                    redoStack = persistentListOf<BoardSnapshot>(),
                    zenQuote = zenQuotes.random(),
                    isDailyChallenge = false,
                    dailyDateString = "",
                    isAdaptiveModeActive = isAdaptive
                )
            }
            startTimer()
            repository.recordGameStarted()
            saveCurrentGame()
        }
    }

    fun startDailyChallenge() {
        viewModelScope.launch {
            val dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val seed = dateString.replace("-", "").toLongOrNull() ?: 20260801L
            val random = kotlin.random.Random(seed)
            val newCells = withContext(Dispatchers.Default) {
                SudokuGenerator.generatePuzzle(Difficulty.MEDIUM, random)
            }.toPersistentList().toPersistentList()

            _uiState.update {
                it.copy(
                    cells = newCells,
                    selectedRow = null,
                    selectedCol = null,
                    selectedDigit = null,
                    isPencilActive = false,
                    currentDifficulty = Difficulty.MEDIUM,


                    isCompleted = false,
                    isGameOver = false,
                    errorCount = 0,
                    starsCount = 0,
                    undoStack = persistentListOf<BoardSnapshot>(),
                    redoStack = persistentListOf<BoardSnapshot>(),
                    zenQuote = "Défi Quotidien - $dateString",
                    isDailyChallenge = true,
                    dailyDateString = dateString
                )
            }
            startTimer()
            repository.recordGameStarted()
            saveCurrentGame()
        }
    }

    fun toggleTutorial(show: Boolean) {
        _uiState.update { it.copy(showTutorial = show) }
    }

    fun startAnotherGame() {
        startNewGame(_uiState.value.currentDifficulty)
    }

    fun resumeSavedGame() {
        viewModelScope.launch {
            val dbGame = repository.savedGame
            dbGame.collectLatest { saved ->
                if (saved != null && saved.cellsJson.isNotEmpty()) {
                    val cells = repository.parseCells(saved.cellsJson).toPersistentList().toPersistentList()
                    val undo = repository.parseSnapshots(saved.undoStackJson).toPersistentList()
                    val redo = repository.parseSnapshots(saved.redoStackJson).toPersistentList()
                    val diff = try { Difficulty.valueOf(saved.difficultyName) } catch (e: Exception) { Difficulty.EASY }
                    val isGameOver = saved.errorCount >= saved.maxErrors && !saved.isCompleted

                    _uiState.update {
                        it.copy(
                            cells = cells,
                            currentDifficulty = diff,

                            isCompleted = saved.isCompleted,
                            isGameOver = isGameOver,
                            errorCount = saved.errorCount,
                            maxErrors = saved.maxErrors,
                            undoStack = undo.toPersistentList(),
                            redoStack = redo.toPersistentList(),
                            isTimerRunning = !saved.isCompleted && !isGameOver,
                            zenQuote = zenQuotes.random()
                        )
                    }
                    if (!saved.isCompleted && !isGameOver) {
                        startTimer()
                    }
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (_isTimerRunning.value && !_uiState.value.isCompleted) {
                    _elapsedSeconds.update { it + 1 }
                    // Auto-save periodically every 10 seconds
                    if (_elapsedSeconds.value % 10 == 0L) {
                        saveCurrentGame()
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.update { false }
        saveCurrentGame()
    }

    fun resumeTimer() {
        if (!_uiState.value.isCompleted) {
            _isTimerRunning.update { true }
        }
    }

    fun onCellClicked(row: Int, col: Int) {
        val currentState = _uiState.value
        val cellIndex = row * 9 + col
        if (cellIndex !in currentState.cells.indices) return

        val cell = currentState.cells[cellIndex]

        if (currentState.inputMode == InputMode.DIGIT_FIRST) {
            if (cell.value != 0) {
                // Tapping an existing digit in DIGIT_FIRST mode switches active digit to that cell's value
                _uiState.update {
                    it.copy(
                        selectedDigit = cell.value,
                        selectedRow = row,
                        selectedCol = col
                    )
                }
            } else if (currentState.selectedDigit != null) {
                // DIGIT_FIRST mode: placing selectedDigit into clicked empty cell
                if (!cell.isGiven) {
                    applyDigitToCell(row, col, currentState.selectedDigit, currentState.isPencilActive)
                }
                _uiState.update { it.copy(selectedRow = row, selectedCol = col) }
            } else {
                _uiState.update { it.copy(selectedRow = row, selectedCol = col) }
            }
        } else {
            // CELL_FIRST mode: select row and col
            _uiState.update {
                it.copy(
                    selectedRow = row,
                    selectedCol = col,
                    selectedDigit = if (cell.value != 0) cell.value else it.selectedDigit
                )
            }
        }
    }

    fun onKeypadDigitClicked(digit: Int) {
        val currentState = _uiState.value

        if (currentState.inputMode == InputMode.DIGIT_FIRST) {
            // Toggle selected digit for digit-first placing
            _uiState.update {
                it.copy(selectedDigit = if (it.selectedDigit == digit) null else digit)
            }
        } else {
            // CELL_FIRST mode: place digit into currently selected cell
            val r = currentState.selectedRow
            val c = currentState.selectedCol
            if (r != null && c != null) {
                val cellIndex = r * 9 + c
                if (cellIndex in currentState.cells.indices && !currentState.cells[cellIndex].isGiven) {
                    applyDigitToCell(r, c, digit, currentState.isPencilActive)
                }
            }
        }
    }

    private fun applyDigitToCell(row: Int, col: Int, digit: Int, isPencil: Boolean) {
        val currentState = _uiState.value
        val cellIndex = row * 9 + col
        val cell = currentState.cells[cellIndex]

        // Push current state to undo stack before change
        val snapshot = BoardSnapshot(currentState.cells)
        val newUndo = (currentState.undoStack + snapshot).toPersistentList()

        val updatedCells = currentState.cells.toMutableList()

        if (isPencil) {
            // Toggle pencil note
            val currentNotes = cell.notes.toMutableSet()
            if (currentNotes.contains(digit)) {
                currentNotes.remove(digit)
            } else {
                currentNotes.add(digit)
            }
            updatedCells[cellIndex] = cell.copy(value = 0, notes = currentNotes, isError = false)
        } else {
            // Set value
            val newValue = if (cell.value == digit) 0 else digit
            var newErrorCount = currentState.errorCount

            val isError = if (newValue != 0 && newValue != cell.solutionValue) {
                if (cell.value != newValue) {
                    newErrorCount++
                }
                true
            } else false

            updatedCells[cellIndex] = cell.copy(value = newValue, notes = emptySet(), isError = isError)

            // Auto-clear notes in same row, column, block if setting enabled
            if (currentState.settings.autoClearNotes && newValue != 0 && !isError) {
                for (i in updatedCells.indices) {
                    val other = updatedCells[i]
                    if (other.row == row || other.col == col || (other.row / 3 == row / 3 && other.col / 3 == col / 3)) {
                        if (other.notes.contains(newValue)) {
                            updatedCells[i] = other.copy(notes = other.notes - newValue)
                        }
                    }
                }
            }

            // Auto-completion for obvious cells if current move was valid
            val finalBoard = if (newValue != 0 && !isError) {
                runAutoComplete(updatedCells)
            } else {
                updatedCells
            }

            val isWin = checkWinCondition(finalBoard)
            val isGameOver = !isWin && newErrorCount >= currentState.maxErrors

            // Auto-advance digit in DIGIT_FIRST mode when a digit is completed (9 instances)
            var nextSelectedDigit = currentState.selectedDigit
            if (currentState.inputMode == InputMode.DIGIT_FIRST && digit != 0 && !isError) {
                val digitCount = finalBoard.count { it.value == digit && !it.isError }
                if (digitCount >= 9) {
                    nextSelectedDigit = findNextAvailableDigit(digit, finalBoard)
                }
            }

            val calculatedStars = if (isWin) {
                when {
                    newErrorCount == 0 -> 3
                    newErrorCount == 1 -> 2
                    else -> 1
                }
            } else 0

            _uiState.update {
                it.copy(
                    cells = finalBoard.toPersistentList(),
                    selectedDigit = nextSelectedDigit,
                    errorCount = newErrorCount,
                    shakeTriggerCount = if (isError) it.shakeTriggerCount + 1 else it.shakeTriggerCount,
                    isGameOver = isGameOver,
                    isCompleted = isWin,
                    starsCount = calculatedStars,
                    undoStack = newUndo.toPersistentList(),
                    redoStack = persistentListOf<BoardSnapshot>(),
                    isTimerRunning = !isWin && !isGameOver
                )
            }

            if (isWin) {
                viewModelScope.launch {
                    val prevBrainInfo = com.example.model.BrainEvolutionCalculator.calculate(_uiState.value.stats)
                    repository.recordGameCompleted(currentState.currentDifficulty.name, _elapsedSeconds.value, calculatedStars)
                    if (currentState.isDailyChallenge && currentState.dailyDateString.isNotEmpty()) {
                        repository.recordDailyCompleted(currentState.dailyDateString)
                    }
                    repository.clearSavedGame()

                    val updatedStats = repository.getStatsDirect()
                    val newBrainInfo = com.example.model.BrainEvolutionCalculator.calculate(updatedStats)
                    if (newBrainInfo.level > prevBrainInfo.level) {
                        _uiState.update { it.copy(graduatedBrainTierInfo = newBrainInfo) }
                    }
                }
            } else if (isGameOver) {
                viewModelScope.launch {
                    repository.clearSavedGame()
                }
            } else {
                saveCurrentGame()
            }
            return
        }

        // Handle pencil update state
        _uiState.update {
            it.copy(
                cells = updatedCells.toPersistentList(),
                undoStack = newUndo.toPersistentList(),
                redoStack = persistentListOf()
            )
        }
        saveCurrentGame()
    }

    fun eraseCell() {
        val currentState = _uiState.value
        val r = currentState.selectedRow
        val c = currentState.selectedCol
        if (r != null && c != null) {
            val cellIndex = r * 9 + c
            if (cellIndex in currentState.cells.indices && !currentState.cells[cellIndex].isGiven) {
                val snapshot = BoardSnapshot(currentState.cells)
                val newUndo = (currentState.undoStack + snapshot).toPersistentList()

                val updatedCells = currentState.cells.toMutableList()
                val cell = updatedCells[cellIndex]
                updatedCells[cellIndex] = cell.copy(value = 0, notes = emptySet(), isError = false)

                _uiState.update {
                    it.copy(
                        cells = updatedCells.toPersistentList(),
                        undoStack = newUndo.toPersistentList(),
                        redoStack = persistentListOf()
                    )
                }
                saveCurrentGame()
            }
        }
    }

    fun togglePencil() {
        _uiState.update { it.copy(isPencilActive = !it.isPencilActive) }
    }

    fun toggleInputMode() {
        val newMode = if (_uiState.value.inputMode == InputMode.CELL_FIRST) InputMode.DIGIT_FIRST else InputMode.CELL_FIRST
        _uiState.update { it.copy(inputMode = newMode, selectedDigit = null) }
        viewModelScope.launch {
            repository.updateSettings(_uiState.value.settings.copy(inputModeName = newMode.name))
        }
    }

    fun undo() {
        val currentState = _uiState.value
        if (currentState.undoStack.isNotEmpty()) {
            val lastSnapshot = currentState.undoStack.last()
            val newUndo = currentState.undoStack.dropLast(1).toPersistentList().toPersistentList()
            val currentSnapshot = BoardSnapshot(currentState.cells)
            val newRedo = (currentState.redoStack + currentSnapshot).toPersistentList()

            _uiState.update {
                it.copy(
                    cells = lastSnapshot.cells.toPersistentList(),
                    undoStack = newUndo.toPersistentList(),
                    redoStack = newRedo
                )
            }
            saveCurrentGame()
        }
    }

    fun redo() {
        val currentState = _uiState.value
        if (currentState.redoStack.isNotEmpty()) {
            val nextSnapshot = currentState.redoStack.last()
            val newRedo = currentState.redoStack.dropLast(1).toPersistentList().toPersistentList()
            val currentSnapshot = BoardSnapshot(currentState.cells)
            val newUndo = (currentState.undoStack + currentSnapshot).toPersistentList()

            _uiState.update {
                it.copy(
                    cells = nextSnapshot.cells.toPersistentList(),
                    undoStack = newUndo.toPersistentList(),
                    redoStack = newRedo
                )
            }
            saveCurrentGame()
        }
    }

    fun showHintPrompt() {
        _uiState.update { it.copy(showHintDialog = true) }
    }

    fun dismissHintDialog() {
        _uiState.update { it.copy(showHintDialog = false, showAdSimulation = false) }
    }

    fun triggerRewardAdAndGiveHint() {
        _uiState.update { it.copy(showHintDialog = false, showAdSimulation = true, adProgress = 0f) }
        viewModelScope.launch {
            // Simulate 3-second relaxing ad/mindful pause
            for (i in 1..30) {
                delay(100)
                _uiState.update { it.copy(adProgress = i / 30f) }
            }
            _uiState.update { it.copy(showAdSimulation = false) }
            applyHint()
        }
    }

    fun applyHint() {
        val currentState = _uiState.value
        val r = currentState.selectedRow
        val c = currentState.selectedCol

        val targetIndex = if (r != null && c != null && currentState.cells[r * 9 + c].value != currentState.cells[r * 9 + c].solutionValue) {
            r * 9 + c
        } else {
            // Find first empty or wrong cell
            currentState.cells.indexOfFirst { it.value != it.solutionValue }
        }

        if (targetIndex in currentState.cells.indices) {
            val cell = currentState.cells[targetIndex]
            val snapshot = BoardSnapshot(currentState.cells)
            val newUndo = (currentState.undoStack + snapshot).toPersistentList()

            val updatedCells = currentState.cells.toMutableList()
            updatedCells[targetIndex] = cell.copy(
                value = cell.solutionValue,
                notes = emptySet(),
                isError = false,
                isHinted = true
            )

            val isWin = checkWinCondition(updatedCells)
            val hintStars = if (isWin) {
                when {
                    currentState.errorCount == 0 -> 3
                    currentState.errorCount == 1 -> 2
                    else -> 1
                }
            } else 0

            _uiState.update {
                it.copy(
                    cells = updatedCells.toPersistentList(),
                    selectedRow = cell.row,
                    selectedCol = cell.col,
                    selectedDigit = cell.solutionValue,
                    undoStack = newUndo.toPersistentList(),
                    isCompleted = isWin,
                    starsCount = hintStars,
                    isTimerRunning = !isWin
                )
            }

            if (isWin) {
                viewModelScope.launch {
                    repository.recordGameCompleted(currentState.currentDifficulty.name, _elapsedSeconds.value, hintStars)
                    repository.clearSavedGame()
                }
            } else {
                saveCurrentGame()
            }
        }
    }

    fun restartCurrentGame() {
        val currentState = _uiState.value
        val resetCells = currentState.cells.map {
            if (it.isGiven) it else it.copy(value = 0, notes = emptySet(), isError = false, isHinted = false)
        }
        _uiState.update {
            it.copy(
                cells = resetCells.toPersistentList(),

                errorCount = 0,
                isCompleted = false,
                isGameOver = false,

                undoStack = persistentListOf<BoardSnapshot>(),
                redoStack = persistentListOf()
            )
        }
        startTimer()
        saveCurrentGame()
    }

    fun updateSettings(newSettings: SettingsEntity) {
        _uiState.update { it.copy(settings = newSettings) }
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }

    private fun findNextAvailableDigit(currentDigit: Int, cells: List<SudokuCell>): Int? {
        var candidate = (currentDigit % 9) + 1
        for (i in 0 until 9) {
            val count = cells.count { it.value == candidate && !it.isError }
            if (count < 9) {
                return candidate
            }
            candidate = (candidate % 9) + 1
        }
        return null
    }

    private fun runAutoComplete(cells: List<SudokuCell>): List<SudokuCell> {
        val currentCells = cells.toMutableList()
        var changed = true

        while (changed) {
            changed = false

            // 1. Check rows with 8 filled cells (1 empty)
            for (r in 0 until 9) {
                val rowCells = currentCells.filter { it.row == r }
                val emptyInRow = rowCells.filter { it.value == 0 }
                if (emptyInRow.size == 1) {
                    val filledValues = rowCells.map { it.value }.toSet()
                    val missing = (1..9).firstOrNull { !filledValues.contains(it) }
                    if (missing != null) {
                        val target = emptyInRow.first()
                        if (missing == target.solutionValue) {
                            val idx = target.row * 9 + target.col
                            currentCells[idx] = target.copy(value = target.solutionValue, notes = emptySet(), isError = false)
                            changed = true
                        }
                    }
                }
            }

            // 2. Check cols with 8 filled cells (1 empty)
            for (c in 0 until 9) {
                val colCells = currentCells.filter { it.col == c }
                val emptyInCol = colCells.filter { it.value == 0 }
                if (emptyInCol.size == 1) {
                    val filledValues = colCells.map { it.value }.toSet()
                    val missing = (1..9).firstOrNull { !filledValues.contains(it) }
                    if (missing != null) {
                        val target = emptyInCol.first()
                        if (missing == target.solutionValue) {
                            val idx = target.row * 9 + target.col
                            currentCells[idx] = target.copy(value = target.solutionValue, notes = emptySet(), isError = false)
                            changed = true
                        }
                    }
                }
            }

            // 3. Check 3x3 boxes with 8 filled cells (1 empty)
            for (b in 0 until 9) {
                val boxRowStart = (b / 3) * 3
                val boxColStart = (b % 3) * 3
                val boxCells = currentCells.filter { it.row in boxRowStart until boxRowStart + 3 && it.col in boxColStart until boxColStart + 3 }
                val emptyInBox = boxCells.filter { it.value == 0 }
                if (emptyInBox.size == 1) {
                    val filledValues = boxCells.map { it.value }.toSet()
                    val missing = (1..9).firstOrNull { !filledValues.contains(it) }
                    if (missing != null) {
                        val target = emptyInBox.first()
                        if (missing == target.solutionValue) {
                            val idx = target.row * 9 + target.col
                            currentCells[idx] = target.copy(value = target.solutionValue, notes = emptySet(), isError = false)
                            changed = true
                        }
                    }
                }
            }

            // 4. Check empty cells with only 1 possible candidate digit
            for (i in currentCells.indices) {
                val cell = currentCells[i]
                if (cell.value == 0) {
                    val r = cell.row
                    val c = cell.col
                    val usedInRow = currentCells.filter { it.row == r }.map { it.value }.toSet()
                    val usedInCol = currentCells.filter { it.col == c }.map { it.value }.toSet()
                    val usedInBox = currentCells.filter { it.row / 3 == r / 3 && it.col / 3 == c / 3 }.map { it.value }.toSet()
                    val usedAll = (usedInRow + usedInCol + usedInBox) - 0

                    val candidates = (1..9).filter { !usedAll.contains(it) }
                    if (candidates.size == 1) {
                        val singleValue = candidates.first()
                        if (singleValue == cell.solutionValue) {
                            currentCells[i] = cell.copy(value = singleValue, notes = emptySet(), isError = false)
                            changed = true
                        }
                    }
                }
            }
        }

        // Auto-clear notes for filled numbers
        for (i in currentCells.indices) {
            val cell = currentCells[i]
            if (cell.value != 0) {
                for (j in currentCells.indices) {
                    val other = currentCells[j]
                    if (other.value == 0 && (other.row == cell.row || other.col == cell.col || (other.row / 3 == cell.row / 3 && other.col / 3 == cell.col / 3))) {
                        if (other.notes.contains(cell.value)) {
                            currentCells[j] = other.copy(notes = other.notes - cell.value)
                        }
                    }
                }
            }
        }

        return currentCells
    }

    private fun checkWinCondition(cells: List<SudokuCell>): Boolean {
        if (cells.size != 81) return false
        return cells.all { it.value != 0 && it.value == it.solutionValue }
    }

    private fun saveCurrentGame() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(1000L) // Debounce for 1 second
            val state = _uiState.value
            if (state.cells.isNotEmpty()) {
                repository.saveGameProgress(
                    difficultyName = state.currentDifficulty.name,
                    cells = state.cells,
                    elapsedSeconds = _elapsedSeconds.value,
                    isCompleted = state.isCompleted,
                    errorCount = state.errorCount,
                    maxErrors = state.maxErrors,
                    undoStack = state.undoStack,
                    redoStack = state.redoStack
                )
            }
        }
    }

    fun clearSelectedCell() {
        val state = _uiState.value
        val row = state.selectedRow ?: return
        val col = state.selectedCol ?: return
        val cellIndex = row * 9 + col
        if (cellIndex in state.cells.indices) {
            val cell = state.cells[cellIndex]
            if (!cell.isGiven && (cell.value != 0 || cell.notes.isNotEmpty())) {
                val snapshot = BoardSnapshot(state.cells)
                val newUndo = (state.undoStack + snapshot).toPersistentList()
                val updated = state.cells.toMutableList()
                updated[cellIndex] = cell.copy(value = 0, notes = emptySet(), isError = false)
                _uiState.update { it.copy(cells = updated.toPersistentList(), undoStack = newUndo.toPersistentList(), redoStack = persistentListOf()) }
                saveCurrentGame()
            }
        }
    }

    fun toggleInputModeBySwipe() {
        val newMode = if (_uiState.value.inputMode == InputMode.CELL_FIRST) InputMode.DIGIT_FIRST else InputMode.CELL_FIRST
        _uiState.update { it.copy(inputMode = newMode) }
        viewModelScope.launch {
            repository.updateSettings(_uiState.value.settings.copy(inputModeName = newMode.name))
        }
    }

    fun updateAudioSettings(
        isAmbientEnabled: Boolean,
        rainVolume: Float,
        forestVolume: Float,
        wavesVolume: Float
    ) {
        ambientAudioMixer.updateSettings(isAmbientEnabled, rainVolume, forestVolume, wavesVolume)
        viewModelScope.launch {
            val currentSettings = _uiState.value.settings
            val updatedSettings = currentSettings.copy(
                isAmbientEnabled = isAmbientEnabled,
                rainVolume = rainVolume,
                forestVolume = forestVolume,
                wavesVolume = wavesVolume
            )
            repository.updateSettings(updatedSettings)
        }
    }

    fun dismissBrainGraduationModal() {
        _uiState.update { it.copy(graduatedBrainTierInfo = null) }
    }

    fun syncCurrentStatsToCloud() {
        viewModelScope.launch {
            val stats = repository.getStatsDirect()
            val brainInfo = com.example.model.BrainEvolutionCalculator.calculate(stats)
            cloudSyncRepository.syncStatsToCloud(stats, brainInfo)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ambientAudioMixer.stopPlayback()
    }
}
