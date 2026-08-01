package com.example.model

import kotlinx.serialization.Serializable

enum class Difficulty(val displayName: String, val clueCount: Int) {
    EASY("Easy", 42),
    MEDIUM("Medium", 34),
    HARD("Hard", 27),
    EXPERT("Expert", 22)
}

enum class InputMode {
    CELL_FIRST,
    DIGIT_FIRST
}

@Serializable
data class SudokuCell(
    val row: Int,
    val col: Int,
    val value: Int = 0,
    val solutionValue: Int = 0,
    val isGiven: Boolean = false,
    val notes: Set<Int> = emptySet(),
    val isError: Boolean = false,
    val isHinted: Boolean = false
)

@Serializable
data class BoardSnapshot(
    val cells: List<SudokuCell>
)
