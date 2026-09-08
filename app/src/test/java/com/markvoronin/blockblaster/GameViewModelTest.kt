package com.markvoronin.blockblaster

import androidx.compose.ui.graphics.Color
import com.markvoronin.blockblaster.model.BlockShape
import com.markvoronin.blockblaster.model.BlockShapes
import com.markvoronin.blockblaster.model.PlacedBlock
import com.markvoronin.blockblaster.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameViewModelTest {

    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        viewModel = GameViewModel()
    }

    @Test
    fun testInitialState() {
        val uiState = viewModel.uiState.value
        assertEquals(0, uiState.score)
        assertEquals(0, uiState.streak)
        assertFalse(uiState.isGameOver)
        assertEquals(3, uiState.candidateShapes.size)
        assertTrue(uiState.candidateShapes.all { it != null })
    }

    @Test
    fun testCanPlaceShapeInBounds() {
        val singleBlockShape = BlockShape(
            id = "test_single",
            grid = listOf(listOf(true)),
            color = Color.Red,
            lightColor = Color.Red,
            darkColor = Color.Red
        )

        assertTrue(viewModel.canPlaceShape(singleBlockShape, 0, 0))
        assertTrue(viewModel.canPlaceShape(singleBlockShape, 7, 7))
        assertFalse(viewModel.canPlaceShape(singleBlockShape, 8, 0))
        assertFalse(viewModel.canPlaceShape(singleBlockShape, 0, 8))
        assertFalse(viewModel.canPlaceShape(singleBlockShape, -1, 0))
    }

    @Test
    fun testRestartGame() {
        viewModel.restartGame()
        val uiState = viewModel.uiState.value
        assertEquals(0, uiState.score)
        assertFalse(uiState.isGameOver)
        assertEquals(3, uiState.candidateShapes.filterNotNull().size)
    }

    @Test
    fun testInitialCandidateShapesCanBePlacedAndClearLine() {
        val uiState = viewModel.uiState.value
        val candidates = uiState.candidateShapes.filterNotNull()
        assertEquals(3, candidates.size)

        val canPlaceAndClear = canPlaceAllShapesAndClearLine(candidates, uiState.grid)
        assertTrue("Every 3 candidate shapes must be able to be placed and clear at least one line", canPlaceAndClear)
        assertFalse("At game start, game over must be false", uiState.isGameOver)
    }

    @Test
    fun testSparseGridCandidateShapesAlwaysClearLine() {
        val dummyBlock = PlacedBlock(Color.Red, Color.Red, Color.Red)
        val sparseGrid = List(8) { r ->
            List(8) { c ->
                if ((r == 0 && (c == 2 || c == 3)) || (r == 3 && c == 5)) dummyBlock else null
            }
        }

        repeat(50) { iteration ->
            val candidates = BlockShapes.getRandomShapes(sparseGrid, 3)
            assertEquals(3, candidates.size)
            val canPlaceAndClear = canPlaceAllShapesAndClearLine(candidates, sparseGrid)
            assertTrue("Iteration $iteration: generated 3 candidate shapes must be able to clear a line on sparse grid", canPlaceAndClear)
        }
    }

    private fun canPlaceAllShapesAndClearLine(
        shapes: List<BlockShape>,
        grid: List<List<PlacedBlock?>>
    ): Boolean {
        val permutations = listOf(
            listOf(0, 1, 2),
            listOf(0, 2, 1),
            listOf(1, 0, 2),
            listOf(1, 2, 0),
            listOf(2, 0, 1),
            listOf(2, 1, 0)
        )

        for (perm in permutations) {
            val s0 = shapes[perm[0]]
            val s1 = shapes[perm[1]]
            val s2 = shapes[perm[2]]

            for (y0 in 0..(8 - s0.height)) {
                for (x0 in 0..(8 - s0.width)) {
                    val res0 = placeAndClear(grid, s0, x0, y0) ?: continue
                    val grid1 = res0.first
                    val cleared0 = res0.second

                    for (y1 in 0..(8 - s1.height)) {
                        for (x1 in 0..(8 - s1.width)) {
                            val res1 = placeAndClear(grid1, s1, x1, y1) ?: continue
                            val grid2 = res1.first
                            val cleared1 = res1.second

                            for (y2 in 0..(8 - s2.height)) {
                                for (x2 in 0..(8 - s2.width)) {
                                    val res2 = placeAndClear(grid2, s2, x2, y2) ?: continue
                                    val cleared2 = res2.second

                                    if (cleared0 + cleared1 + cleared2 >= 1) {
                                        return true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    private fun placeAndClear(
        grid: List<List<PlacedBlock?>>,
        shape: BlockShape,
        x: Int,
        y: Int
    ): Pair<List<List<PlacedBlock?>>, Int>? {
        // Check fit
        for (r in 0 until shape.height) {
            for (c in 0 until shape.width) {
                if (shape.grid[r][c]) {
                    if (grid[y + r][x + c] != null) return null
                }
            }
        }

        val dummyBlock = PlacedBlock(
            Color.Red, Color.Red, Color.Red
        )

        val newGrid = grid.map { it.toMutableList() }.toMutableList()
        for (r in 0 until shape.height) {
            for (c in 0 until shape.width) {
                if (shape.grid[r][c]) {
                    newGrid[y + r][x + c] = dummyBlock
                }
            }
        }

        val clearedRows = (0..7).filter { r -> newGrid[r].all { it != null } }
        val clearedCols = (0..7).filter { c -> (0..7).all { r -> newGrid[r][c] != null } }
        val clearedCount = clearedRows.size + clearedCols.size

        for (r in clearedRows) {
            for (c in 0..7) newGrid[r][c] = null
        }
        for (c in clearedCols) {
            for (r in 0..7) newGrid[r][c] = null
        }

        return Pair(newGrid.map { it.toList() }, clearedCount)
    }
}
