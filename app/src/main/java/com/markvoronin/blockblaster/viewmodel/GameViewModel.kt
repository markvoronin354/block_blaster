package com.markvoronin.blockblaster.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.markvoronin.blockblaster.model.BlockShape
import com.markvoronin.blockblaster.model.BlockShapes
import com.markvoronin.blockblaster.model.PlacedBlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ClearEvent(
    val rows: List<Int>,
    val cols: List<Int>,
    val comboText: String?
)

data class GameUiState(
    val grid: List<List<PlacedBlock?>> = List(8) { List(8) { null } },
    val candidateShapes: List<BlockShape?> = BlockShapes.getRandomShapes(3),
    val score: Int = 0,
    val highScore: Int = 0,
    val streak: Int = 0,
    val isGameOver: Boolean = false,
    val lastClearEvent: ClearEvent? = null,
    val newlyPlacedCells: List<Pair<Int, Int>> = emptyList()
)

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val sharedPrefsName = "block_blaster_prefs"

    fun initHighScore(context: Context) {
        val prefs = context.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        val savedHigh = prefs.getInt("high_score", 0)
        _uiState.update { it.copy(highScore = savedHigh) }
    }

    private fun saveHighScore(context: Context, newHigh: Int) {
        val prefs = context.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        prefs.edit().putInt("high_score", newHigh).apply()
    }

    fun canPlaceShape(shape: BlockShape, gridX: Int, gridY: Int): Boolean {
        val currentGrid = _uiState.value.grid
        for (r in 0 until shape.height) {
            for (c in 0 until shape.width) {
                if (shape.grid[r][c]) {
                    val targetX = gridX + c
                    val targetY = gridY + r
                    if (targetX !in 0..7 || targetY !in 0..7) return false
                    if (currentGrid[targetY][targetX] != null) return false
                }
            }
        }
        return true
    }

    fun placeShape(
        shapeIndex: Int,
        gridX: Int,
        gridY: Int,
        context: Context
    ): Boolean {
        val currentState = _uiState.value
        val shape = currentState.candidateShapes.getOrNull(shapeIndex) ?: return false

        if (!canPlaceShape(shape, gridX, gridY)) return false

        val newGrid = currentState.grid.map { it.toMutableList() }.toMutableList()
        val placedCells = mutableListOf<Pair<Int, Int>>()

        val placedBlock = PlacedBlock(
            mainColor = shape.color,
            lightColor = shape.lightColor,
            darkColor = shape.darkColor
        )

        for (r in 0 until shape.height) {
            for (c in 0 until shape.width) {
                if (shape.grid[r][c]) {
                    val targetX = gridX + c
                    val targetY = gridY + r
                    newGrid[targetY][targetX] = placedBlock
                    placedCells.add(Pair(targetX, targetY))
                }
            }
        }

        var newScore = currentState.score + shape.blockCount

        val newCandidates = currentState.candidateShapes.toMutableList()
        newCandidates[shapeIndex] = null

        val clearedRows = mutableListOf<Int>()
        val clearedCols = mutableListOf<Int>()

        for (r in 0..7) {
            if (newGrid[r].all { it != null }) {
                clearedRows.add(r)
            }
        }

        for (c in 0..7) {
            if ((0..7).all { r -> newGrid[r][c] != null }) {
                clearedCols.add(c)
            }
        }

        val totalLinesCleared = clearedRows.size + clearedCols.size
        var newStreak = currentState.streak
        var comboText: String? = null

        if (totalLinesCleared > 0) {
            newStreak += 1
            val baseClearScore = totalLinesCleared * 100
            val multiLineBonus = when (totalLinesCleared) {
                1 -> 0
                2 -> 150
                3 -> 400
                else -> 800
            }
            val streakBonus = (newStreak - 1) * 50
            val lineTotalScore = baseClearScore + multiLineBonus + streakBonus
            newScore += lineTotalScore

            comboText = when {
                totalLinesCleared >= 4 -> "SUPER BLAST! +$lineTotalScore"
                totalLinesCleared == 3 -> "TRIPLE CLEAR! +$lineTotalScore"
                totalLinesCleared == 2 -> "DOUBLE CLEAR! +$lineTotalScore"
                newStreak > 1 -> "STREAK x$newStreak! +$lineTotalScore"
                else -> "LINE CLEAR! +$lineTotalScore"
            }

            for (r in clearedRows) {
                for (c in 0..7) {
                    newGrid[r][c] = null
                }
            }
            for (c in clearedCols) {
                for (r in 0..7) {
                    newGrid[r][c] = null
                }
            }
        } else {
            newStreak = 0
        }

        if (newCandidates.all { it == null }) {
            val gridForNextCandidates = newGrid.map { it.toList() }
            val freshCandidates = BlockShapes.getRandomShapes(grid = gridForNextCandidates, count = 3)
            newCandidates.clear()
            newCandidates.addAll(freshCandidates)
        }

        var newHigh = currentState.highScore
        if (newScore > newHigh) {
            newHigh = newScore
            saveHighScore(context, newHigh)
        }

        val gridForCheck = newGrid.map { it.toList() }
        val remainingCandidates = newCandidates.filterNotNull()
        val hasAnyValidMove = remainingCandidates.any { candidate ->
            canPlaceAnywhere(candidate, gridForCheck)
        }

        val isGameOver = remainingCandidates.isNotEmpty() && !hasAnyValidMove

        _uiState.value = currentState.copy(
            grid = newGrid.map { it.toList() },
            candidateShapes = newCandidates,
            score = newScore,
            highScore = newHigh,
            streak = newStreak,
            isGameOver = isGameOver,
            lastClearEvent = if (totalLinesCleared > 0) ClearEvent(clearedRows, clearedCols, comboText) else null,
            newlyPlacedCells = placedCells
        )

        return true
    }

    private fun canPlaceAnywhere(shape: BlockShape, grid: List<List<PlacedBlock?>>): Boolean {
        for (y in 0..7) {
            for (x in 0..7) {
                var canFit = true
                for (r in 0 until shape.height) {
                    for (c in 0 until shape.width) {
                        if (shape.grid[r][c]) {
                            val targetX = x + c
                            val targetY = y + r
                            if (targetX !in 0..7 || targetY !in 0..7 || grid[targetY][targetX] != null) {
                                canFit = false
                                break
                            }
                        }
                    }
                    if (!canFit) break
                }
                if (canFit) return true
            }
        }
        return false
    }

    fun restartGame() {
        _uiState.update {
            it.copy(
                grid = List(8) { List(8) { null } },
                candidateShapes = BlockShapes.getRandomShapes(3),
                score = 0,
                streak = 0,
                isGameOver = false,
                lastClearEvent = null,
                newlyPlacedCells = emptyList()
            )
        }
    }
}
