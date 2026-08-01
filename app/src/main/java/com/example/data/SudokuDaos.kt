package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM saved_game WHERE id = 1 LIMIT 1")
    fun getSavedGame(): Flow<GameEntity?>

    @Query("SELECT * FROM saved_game WHERE id = 1 LIMIT 1")
    suspend fun getSavedGameDirect(): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGame(game: GameEntity)

    @Query("DELETE FROM saved_game WHERE id = 1")
    suspend fun clearSavedGame()
}

@Dao
interface StatsDao {
    @Query("SELECT * FROM game_stats WHERE id = 1 LIMIT 1")
    fun getStats(): Flow<StatsEntity?>

    @Query("SELECT * FROM game_stats WHERE id = 1 LIMIT 1")
    suspend fun getStatsDirect(): StatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStats(stats: StatsEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)
}
