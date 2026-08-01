package com.example.data

import com.example.model.BoardSnapshot
import com.example.model.SudokuCell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SudokuRepository(
    private val gameDao: GameDao,
    private val statsDao: StatsDao,
    private val settingsDao: SettingsDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    val savedGame: Flow<GameEntity?> = gameDao.getSavedGame()
    val stats: Flow<StatsEntity> = statsDao.getStats().map { it ?: StatsEntity() }
    val settings: Flow<SettingsEntity> = settingsDao.getSettings().map { it ?: SettingsEntity() }

    suspend fun getStatsDirect(): StatsEntity = statsDao.getStatsDirect() ?: StatsEntity()

    suspend fun saveGameProgress(
        difficultyName: String,
        cells: List<SudokuCell>,
        elapsedSeconds: Long,
        isCompleted: Boolean,
        errorCount: Int,
        maxErrors: Int,
        undoStack: List<BoardSnapshot>,
        redoStack: List<BoardSnapshot>
    ) {
        val cellsJson = json.encodeToString(cells)
        val undoJson = json.encodeToString(undoStack)
        val redoJson = json.encodeToString(redoStack)

        val entity = GameEntity(
            id = 1,
            difficultyName = difficultyName,
            cellsJson = cellsJson,
            elapsedSeconds = elapsedSeconds,
            isCompleted = isCompleted,
            errorCount = errorCount,
            maxErrors = maxErrors,
            undoStackJson = undoJson,
            redoStackJson = redoJson,
            lastUpdated = System.currentTimeMillis()
        )
        gameDao.saveGame(entity)
    }

    fun parseCells(jsonString: String): List<SudokuCell> {
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseSnapshots(jsonString: String): List<BoardSnapshot> {
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun recordGameStarted() {
        val currentStats = statsDao.getStatsDirect() ?: StatsEntity()
        val updated = currentStats.copy(totalGamesPlayed = currentStats.totalGamesPlayed + 1)
        statsDao.saveStats(updated)
    }

    suspend fun recordGameCompleted(difficultyName: String, timeInSeconds: Long, starsEarned: Int) {
        val currentStats = statsDao.getStatsDirect() ?: StatsEntity()
        var easyComp = currentStats.easyCompleted
        var medComp = currentStats.mediumCompleted
        var hardComp = currentStats.hardCompleted
        var expComp = currentStats.expertCompleted

        var easyS = currentStats.easyStars
        var medS = currentStats.mediumStars
        var hardS = currentStats.hardStars
        var expS = currentStats.expertStars

        var bestEasy = currentStats.bestTimeEasy
        var bestMed = currentStats.bestTimeMedium
        var bestHard = currentStats.bestTimeHard
        var bestExp = currentStats.bestTimeExpert

        when (difficultyName) {
            "EASY" -> {
                easyComp++
                easyS += starsEarned
                if (bestEasy == 0L || timeInSeconds < bestEasy) bestEasy = timeInSeconds
            }
            "MEDIUM" -> {
                medComp++
                medS += starsEarned
                if (bestMed == 0L || timeInSeconds < bestMed) bestMed = timeInSeconds
            }
            "HARD" -> {
                hardComp++
                hardS += starsEarned
                if (bestHard == 0L || timeInSeconds < bestHard) bestHard = timeInSeconds
            }
            "EXPERT" -> {
                expComp++
                expS += starsEarned
                if (bestExp == 0L || timeInSeconds < bestExp) bestExp = timeInSeconds
            }
        }

        val newStreak = currentStats.currentStreak + 1
        val bestStreak = maxOf(newStreak, currentStats.bestStreak)

        val updated = currentStats.copy(
            easyCompleted = easyComp,
            mediumCompleted = medComp,
            hardCompleted = hardComp,
            expertCompleted = expComp,
            easyStars = easyS,
            mediumStars = medS,
            hardStars = hardS,
            expertStars = expS,
            bestTimeEasy = bestEasy,
            bestTimeMedium = bestMed,
            bestTimeHard = bestHard,
            bestTimeExpert = bestExp,
            currentStreak = newStreak,
            bestStreak = bestStreak
        )
        statsDao.saveStats(updated)
    }

    suspend fun recordDailyCompleted(dateString: String) {
        val currentStats = statsDao.getStatsDirect() ?: StatsEntity()
        if (currentStats.lastDailyCompletedDate != dateString) {
            val updated = currentStats.copy(
                lastDailyCompletedDate = dateString,
                dailyCompletedCount = currentStats.dailyCompletedCount + 1
            )
            statsDao.saveStats(updated)
        }
    }

    suspend fun updateSettings(settingsEntity: SettingsEntity) {
        settingsDao.saveSettings(settingsEntity)
    }

    suspend fun clearSavedGame() {
        gameDao.clearSavedGame()
    }
}
