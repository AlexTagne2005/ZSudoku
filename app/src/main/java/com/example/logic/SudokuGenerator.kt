package com.example.logic

import com.example.model.Difficulty
import com.example.model.SudokuCell
import kotlin.random.Random

object SudokuGenerator {

    fun generatePuzzle(difficulty: Difficulty, random: Random = Random(System.currentTimeMillis() + Random.nextInt())): List<SudokuCell> {
        // 1. Generate full solved grid
        val solution = Array(9) { IntArray(9) }
        SudokuSolver.solve(solution, randomize = true, random = random)

        // Copy solution grid
        val puzzle = Array(9) { r -> solution[r].clone() }

        // Determine number of cells to remove
        val totalCells = 81
        val targetClues = difficulty.clueCount
        var cellsToRemove = totalCells - targetClues

        val positions = (0 until 81).toList().shuffled(random)

        for (pos in positions) {
            if (cellsToRemove <= 0) break
            val r = pos / 9
            val c = pos % 9

            val backup = puzzle[r][c]
            puzzle[r][c] = 0

            // Check if grid still has unique solution
            val copyGrid = Array(9) { row -> puzzle[row].clone() }
            if (SudokuSolver.countSolutions(copyGrid) != 1) {
                // Not unique, put it back
                puzzle[r][c] = backup
            } else {
                cellsToRemove--
            }
        }

        // Build list of cells
        val result = mutableListOf<SudokuCell>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val isGiven = puzzle[r][c] != 0
                result.add(
                    SudokuCell(
                        row = r,
                        col = c,
                        value = if (isGiven) puzzle[r][c] else 0,
                        solutionValue = solution[r][c],
                        isGiven = isGiven,
                        notes = emptySet(),
                        isError = false,
                        isHinted = false
                    )
                )
            }
        }
        return result
    }
}
