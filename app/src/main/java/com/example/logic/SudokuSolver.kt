package com.example.logic

import kotlin.random.Random

object SudokuSolver {

    fun isValidPlacement(grid: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        for (i in 0 until 9) {
            if (grid[row][i] == num) return false
            if (grid[i][col] == num) return false
        }
        val startRow = (row / 3) * 3
        val startCol = (col / 3) * 3
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                if (grid[startRow + r][startCol + c] == num) return false
            }
        }
        return true
    }

    fun solve(grid: Array<IntArray>, randomize: Boolean = false, random: Random = Random.Default): Boolean {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (grid[row][col] == 0) {
                    val numbers = (1..9).toList().let { if (randomize) it.shuffled(random) else it }
                    for (num in numbers) {
                        if (isValidPlacement(grid, row, col, num)) {
                            grid[row][col] = num
                            if (solve(grid, randomize, random)) return true
                            grid[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    fun countSolutions(grid: Array<IntArray>, limit: Int = 2): Int {
        var count = 0

        fun search(): Boolean {
            for (row in 0 until 9) {
                for (col in 0 until 9) {
                    if (grid[row][col] == 0) {
                        for (num in 1..9) {
                            if (isValidPlacement(grid, row, col, num)) {
                                grid[row][col] = num
                                if (search()) return true
                                grid[row][col] = 0
                            }
                        }
                        return false
                    }
                }
            }
            count++
            return count >= limit
        }

        search()
        return count
    }
}
