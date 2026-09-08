package com.markvoronin.blockblaster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.markvoronin.blockblaster.model.PlacedBlock

@Composable
fun BlockCell(
    placedBlock: PlacedBlock?,
    cellSize: Dp,
    modifier: Modifier = Modifier,
    isPreview: Boolean = false,
    isValidPreview: Boolean = true,
    isLineHighlighted: Boolean = false,
    highlightColor: Color? = null
) {
    val shape = RoundedCornerShape(20)

    if (isLineHighlighted && highlightColor != null) {
        // Line clear highlight: entire clearable row/col changes to the held piece's color!
        Box(
            modifier = modifier
                .size(cellSize)
                .padding(1.5.dp)
                .shadow(elevation = 6.dp, shape = shape)
                .clip(shape)
                .background(highlightColor)
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.7f),
                    shape = shape
                )
        )
    } else if (placedBlock != null) {
        // Render vibrant block
        Box(
            modifier = modifier
                .size(cellSize)
                .padding(1.5.dp)
                .shadow(elevation = 2.dp, shape = shape)
                .clip(shape)
                .background(placedBlock.mainColor)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = shape
                )
        )
    } else if (isPreview) {
        val previewColor = if (isValidPreview) Color(0xFF2ECC71) else Color(0xFFE74C3C)
        Box(
            modifier = modifier
                .size(cellSize)
                .padding(1.5.dp)
                .clip(shape)
                .background(previewColor.copy(alpha = 0.45f))
                .border(
                    width = 2.dp,
                    color = previewColor,
                    shape = shape
                )
        )
    } else {
        // Empty slot cell
        Box(
            modifier = modifier
                .size(cellSize)
                .padding(1.5.dp)
                .clip(shape)
                .background(Color(0xFF1E2836).copy(alpha = 0.7f))
                .border(
                    width = 1.dp,
                    color = Color(0xFF2C3E50).copy(alpha = 0.5f),
                    shape = shape
                )
        )
    }
}
