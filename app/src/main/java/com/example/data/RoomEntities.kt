package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Difficulty
import com.example.model.InputMode

@Entity(tableName = "saved_game")
data class GameEntity(
    @PrimaryKey val id: Int = 1,
    val difficultyName: String = Difficulty.EASY.name,
    val cellsJson: String,
    val elapsedSeconds: Long = 0L,
    val isCompleted: Boolean = false,
    val errorCount: Int = 0,
    val maxErrors: Int = 3,
    val undoStackJson: String = "[]",
    val redoStackJson: String = "[]",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_stats")
data class StatsEntity(
    @PrimaryKey val id: Int = 1,
    val totalGamesPlayed: Int = 0,
    val easyCompleted: Int = 0,
    val mediumCompleted: Int = 0,
    val hardCompleted: Int = 0,
    val expertCompleted: Int = 0,
    val easyStars: Int = 0,
    val mediumStars: Int = 0,
    val hardStars: Int = 0,
    val expertStars: Int = 0,
    val bestTimeEasy: Long = 0L,
    val bestTimeMedium: Long = 0L,
    val bestTimeHard: Long = 0L,
    val bestTimeExpert: Long = 0L,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastDailyCompletedDate: String = "",
    val dailyCompletedCount: Int = 0
)

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isDarkMode: Boolean = false,
    val isTimerVisible: Boolean = true, // Timer enabled by default
    val isErrorCheckingEnabled: Boolean = true, // Highlight errors enabled by default
    val autoClearNotes: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val colorTheme: String = "OCEAN",
    val inputModeName: String = InputMode.CELL_FIRST.name,
    val notesStyle: String = "GRID", // "GRID" or "CENTERED"
    val isAmbientEnabled: Boolean = false,
    val rainVolume: Float = 0.5f,
    val forestVolume: Float = 0.3f,
    val wavesVolume: Float = 0.4f
)
