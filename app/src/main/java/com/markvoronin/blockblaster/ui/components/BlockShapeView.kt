package com.markvoronin.blockblaster.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.markvoronin.blockblaster.model.BlockShape
import com.markvoronin.blockblaster.model.PlacedBlock

@Composable
fun BlockShapeView(
    shape: BlockShape,
    cellSize: Dp,
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    val placedBlock = PlacedBlock(
        mainColor = shape.color.copy(alpha = alpha),
        lightColor = shape.lightColor.copy(alpha = alpha),
        darkColor = shape.darkColor.copy(alpha = alpha)
    )

    Column(modifier = modifier) {
        for (r in 0 until shape.height) {
            Row {
                for (c in 0 until shape.width) {
                    if (shape.grid[r][c]) {
                        BlockCell(
                            placedBlock = placedBlock,
                            cellSize = cellSize
                        )
                    } else {
                        Box(modifier = Modifier.size(cellSize))
                    }
                }
            }
        }
    }
}
