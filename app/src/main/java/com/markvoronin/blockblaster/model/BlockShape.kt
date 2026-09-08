package com.markvoronin.blockblaster.model

import androidx.compose.ui.graphics.Color

data class BlockShape(
    val id: String,
    val grid: List<List<Boolean>>,
    val color: Color,
    val lightColor: Color,
    val darkColor: Color
) {
    val width: Int get() = if (grid.isNotEmpty()) grid[0].size else 0
    val height: Int get() = grid.size
    
    val blockCount: Int get() = grid.sumOf { row -> row.count { it } }
}
