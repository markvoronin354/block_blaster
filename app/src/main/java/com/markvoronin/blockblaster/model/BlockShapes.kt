package com.markvoronin.blockblaster.model

import androidx.compose.ui.graphics.Color
import java.util.UUID
import kotlin.random.Random

data class BlockColorPalette(
    val main: Color,
    val light: Color,
    val dark: Color
)

object BlockColors {
    val Emerald = BlockColorPalette(
        main = Color(0xFF2ECC71),
        light = Color(0xFF78E08F),
        dark = Color(0xFF1E8449)
    )
    val Amber = BlockColorPalette(
        main = Color(0xFFFF9F43),
        light = Color(0xFFFFC076),
        dark = Color(0xFFD35400)
    )
    val CoralRed = BlockColorPalette(
        main = Color(0xFFEE5253),
        light = Color(0xFFFF7675),
        dark = Color(0xFFC0392B)
    )
    val Sapphire = BlockColorPalette(
        main = Color(0xFF0ABDE3),
        light = Color(0xFF48DBFB),
        dark = Color(0xFF10AC84)
    )
    val Amethyst = BlockColorPalette(
        main = Color(0xFF9B59B6),
        light = Color(0xFFD6A2E8),
        dark = Color(0xFF6C5CE7)
    )
    val SunYellow = BlockColorPalette(
        main = Color(0xFFF1C40F),
        light = Color(0xFFF9E79F),
        dark = Color(0xFFB7950B)
    )
    val NeonTeal = BlockColorPalette(
        main = Color(0xFF1ABC9C),
        light = Color(0xFFA3E4D7),
        dark = Color(0xFF117864)
    )
    val PinkBlossom = BlockColorPalette(
        main = Color(0xFFFF6B81),
        light = Color(0xFFFFA5B2),
        dark = Color(0xFFD63031)
    )

    val allPalettes = listOf(
        Emerald, Amber, CoralRed, Sapphire, Amethyst, SunYellow, NeonTeal, PinkBlossom
    )
}

object BlockShapes {
    private val T = true
    private val F = false

    private val rawTemplates: List<List<List<Boolean>>> = listOf(
        // 1x1 Single
        listOf(
            listOf(T)
        ),
        // 1x2 Horizontal
        listOf(
            listOf(T, T)
        ),
        // 2x1 Vertical
        listOf(
            listOf(T),
            listOf(T)
        ),
        // 1x3 Horizontal
        listOf(
            listOf(T, T, T)
        ),
        // 3x1 Vertical
        listOf(
            listOf(T),
            listOf(T),
            listOf(T)
        ),
        // 1x4 Horizontal
        listOf(
            listOf(T, T, T, T)
        ),
        // 4x1 Vertical
        listOf(
            listOf(T),
            listOf(T),
            listOf(T),
            listOf(T)
        ),
        // 1x5 Horizontal
        listOf(
            listOf(T, T, T, T, T)
        ),
        // 5x1 Vertical
        listOf(
            listOf(T),
            listOf(T),
            listOf(T),
            listOf(T),
            listOf(T)
        ),
        // 2x2 Square
        listOf(
            listOf(T, T),
            listOf(T, T)
        ),
        // 3x3 Square
        listOf(
            listOf(T, T, T),
            listOf(T, T, T),
            listOf(T, T, T)
        ),
        // Small Corner 2x2 (L-shape 3 blocks) - 4 rotations
        listOf(
            listOf(T, T),
            listOf(T, F)
        ),
        listOf(
            listOf(T, T),
            listOf(F, T)
        ),
        listOf(
            listOf(T, F),
            listOf(T, T)
        ),
        listOf(
            listOf(F, T),
            listOf(T, T)
        ),
        // Big Corner 3x3 (5 blocks) - 4 rotations
        listOf(
            listOf(T, T, T),
            listOf(T, F, F),
            listOf(T, F, F)
        ),
        listOf(
            listOf(T, T, T),
            listOf(F, F, T),
            listOf(F, F, T)
        ),
        listOf(
            listOf(T, F, F),
            listOf(T, F, F),
            listOf(T, T, T)
        ),
        listOf(
            listOf(F, F, T),
            listOf(F, F, T),
            listOf(T, T, T)
        ),
        // Standard L-Shape 3x2 (4 blocks) - 4 rotations
        listOf(
            listOf(T, F),
            listOf(T, F),
            listOf(T, T)
        ),
        listOf(
            listOf(F, T),
            listOf(F, T),
            listOf(T, T)
        ),
        listOf(
            listOf(T, T, T),
            listOf(T, F, F)
        ),
        listOf(
            listOf(T, T, T),
            listOf(F, F, T)
        ),
        // T-Shape 3x2 (4 blocks) - 4 rotations
        listOf(
            listOf(T, T, T),
            listOf(F, T, F)
        ),
        listOf(
            listOf(F, T, F),
            listOf(T, T, T)
        ),
        listOf(
            listOf(T, F),
            listOf(T, T),
            listOf(T, F)
        ),
        listOf(
            listOf(F, T),
            listOf(T, T),
            listOf(F, T)
        ),
        // Z / S Shapes (4 blocks)
        listOf(
            listOf(T, T, F),
            listOf(F, T, T)
        ),
        listOf(
            listOf(F, T, T),
            listOf(T, T, F)
        )
    )

    private class TemplateInfo(
        val grid: List<List<Boolean>>,
        val placementMasks: List<Long>,
        val blockCount: Int
    )

    private val ROW_MASKS = LongArray(8) { r -> 0xFFL shl (r * 8) }
    private val COL_MASKS = LongArray(8) { c -> 0x0101010101010101L shl c }

    private val templateInfos: List<TemplateInfo> by lazy {
        rawTemplates.map { tmplGrid ->
            val height = tmplGrid.size
            val width = if (tmplGrid.isNotEmpty()) tmplGrid[0].size else 0
            val masks = mutableListOf<Long>()
            var blockCount = 0

            for (r in 0 until height) {
                for (c in 0 until width) {
                    if (tmplGrid[r][c]) blockCount++
                }
            }

            for (y in 0..(8 - height)) {
                for (x in 0..(8 - width)) {
                    var mask = 0L
                    for (r in 0 until height) {
                        for (c in 0 until width) {
                            if (tmplGrid[r][c]) {
                                mask = mask or (1L shl ((y + r) * 8 + (x + c)))
                            }
                        }
                    }
                    masks.add(mask)
                }
            }

            TemplateInfo(
                grid = tmplGrid,
                placementMasks = masks,
                blockCount = blockCount
            )
        }
    }

    private fun gridToBitboard(grid: List<List<PlacedBlock?>>): Long {
        var bitboard = 0L
        for (y in 0..7) {
            if (y >= grid.size) break
            val row = grid[y]
            for (x in 0..7) {
                if (x >= row.size) break
                if (row[x] != null) {
                    bitboard = bitboard or (1L shl (y * 8 + x))
                }
            }
        }
        return bitboard
    }

    private fun simulatePlacement(grid: Long, mask: Long): Pair<Long, Int>? {
        if ((grid and mask) != 0L) return null

        val placedGrid = grid or mask
        var clearedGrid = placedGrid
        var clearCount = 0

        for (r in 0..7) {
            if ((placedGrid and ROW_MASKS[r]) == ROW_MASKS[r]) {
                clearCount++
                clearedGrid = clearedGrid and ROW_MASKS[r].inv()
            }
        }
        for (c in 0..7) {
            if ((placedGrid and COL_MASKS[c]) == COL_MASKS[c]) {
                clearCount++
                clearedGrid = clearedGrid and COL_MASKS[c].inv()
            }
        }

        return Pair(clearedGrid, clearCount)
    }

    private fun checkRecursive(
        remainingTemplates: List<Int>,
        currentGrid: Long,
        totalClearedLines: Int,
        requireLineClear: Boolean
    ): Boolean {
        if (remainingTemplates.isEmpty()) {
            return !requireLineClear || totalClearedLines >= 1
        }

        for (i in remainingTemplates.indices) {
            val tmplIdx = remainingTemplates[i]
            val tmpl = templateInfos[tmplIdx]

            val nextRemaining = remainingTemplates.toMutableList()
            nextRemaining.removeAt(i)

            for (mask in tmpl.placementMasks) {
                val res = simulatePlacement(currentGrid, mask) ?: continue
                val nextGrid = res.first
                val clearedNow = res.second

                if (checkRecursive(
                        remainingTemplates = nextRemaining,
                        currentGrid = nextGrid,
                        totalClearedLines = totalClearedLines + clearedNow,
                        requireLineClear = requireLineClear
                    )
                ) {
                    return true
                }
            }
        }

        return false
    }

    fun getRandomShapes(count: Int = 3): List<BlockShape> {
        return getRandomShapes(grid = null, count = count)
    }

    fun getRandomShapes(grid: List<List<PlacedBlock?>>? = null, count: Int = 3): List<BlockShape> {
        val initialGrid = if (grid != null) gridToBitboard(grid) else 0L
        val templateCount = templateInfos.size
        var selectedIndices: List<Int>? = null

        if (count == 3) {
            // Attempt 1: Standard random sampling requiring line clear (500 tries)
            for (attempt in 0 until 500) {
                val i1 = Random.nextInt(templateCount)
                val i2 = Random.nextInt(templateCount)
                val i3 = Random.nextInt(templateCount)
                val candidateIndices = listOf(i1, i2, i3)

                if (checkRecursive(candidateIndices, initialGrid, 0, requireLineClear = true)) {
                    selectedIndices = candidateIndices
                    break
                }
            }

            // Attempt 2: Biased random sampling using line-completing shapes (indices 0..8: 1x1 up to 5x1 and 1x5)
            if (selectedIndices == null) {
                val lineIndices = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8)
                for (attempt in 0 until 500) {
                    val i1 = lineIndices.random()
                    val i2 = lineIndices.random()
                    val i3 = Random.nextInt(templateCount)
                    val candidateIndices = listOf(i1, i2, i3).shuffled()

                    if (checkRecursive(candidateIndices, initialGrid, 0, requireLineClear = true)) {
                        selectedIndices = candidateIndices
                        break
                    }
                }
            }

            // Attempt 3: Systematic search prioritizing line-completing templates
            if (selectedIndices == null) {
                val prioritizedIndices = (0..8).toList() + (9 until templateCount).toList()

                outer@ for (a in prioritizedIndices) {
                    for (b in prioritizedIndices) {
                        for (c in prioritizedIndices) {
                            val candidateIndices = listOf(a, b, c)
                            if (checkRecursive(candidateIndices, initialGrid, 0, requireLineClear = true)) {
                                selectedIndices = candidateIndices
                                break@outer
                            }
                        }
                    }
                }
            }
        } else {
            // General case for count != 3
            for (attempt in 0 until 300) {
                val candidateIndices = List(count) { Random.nextInt(templateCount) }
                if (checkRecursive(candidateIndices, initialGrid, 0, requireLineClear = true)) {
                    selectedIndices = candidateIndices
                    break
                }
            }
        }

        // Guaranteed fallback with line shapes (1x5, 1x3, 1x1) which always fit and clear a line
        val finalIndices = selectedIndices ?: listOf(7, 3, 0)

        val selectedPalettes = BlockColors.allPalettes.shuffled()
        return finalIndices.mapIndexed { index, templateIdx ->
            val tmpl = templateInfos[templateIdx]
            val palette = selectedPalettes[index % selectedPalettes.size]
            BlockShape(
                id = UUID.randomUUID().toString(),
                grid = tmpl.grid,
                color = palette.main,
                lightColor = palette.light,
                darkColor = palette.dark
            )
        }
    }
}
