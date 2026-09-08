package com.markvoronin.blockblaster.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.markvoronin.blockblaster.model.BlockShape
import com.markvoronin.blockblaster.model.PlacedBlock
import kotlinx.coroutines.delay

@Composable
fun BoardView(
    grid: List<List<PlacedBlock?>>,
    hoverShape: BlockShape?,
    hoverGridX: Int,
    hoverGridY: Int,
    isHoverValid: Boolean,
    comboText: String?,
    onBoardPositioned: (Rect, Dp) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var visibleComboText by remember { mutableStateOf<String?>(null) }

    val highlightedRows = remember(grid, hoverShape, hoverGridX, hoverGridY, isHoverValid) {
        val rows = mutableSetOf<Int>()
        if (isHoverValid && hoverShape != null) {
            for (r in 0..7) {
                var rowFull = true
                for (c in 0..7) {
                    val isHoveredCell = c - hoverGridX in 0 until hoverShape.width &&
                            r - hoverGridY in 0 until hoverShape.height &&
                            hoverShape.grid[r - hoverGridY][c - hoverGridX]
                    if (grid[r][c] == null && !isHoveredCell) {
                        rowFull = false
                        break
                    }
                }
                if (rowFull) rows.add(r)
            }
        }
        rows
    }

    val highlightedCols = remember(grid, hoverShape, hoverGridX, hoverGridY, isHoverValid) {
        val cols = mutableSetOf<Int>()
        if (isHoverValid && hoverShape != null) {
            for (c in 0..7) {
                var colFull = true
                for (r in 0..7) {
                    val isHoveredCell = c - hoverGridX in 0 until hoverShape.width &&
                            r - hoverGridY in 0 until hoverShape.height &&
                            hoverShape.grid[r - hoverGridY][c - hoverGridX]
                    if (grid[r][c] == null && !isHoveredCell) {
                        colFull = false
                        break
                    }
                }
                if (colFull) cols.add(c)
            }
        }
        cols
    }

    LaunchedEffect(comboText) {
        if (comboText != null) {
            visibleComboText = comboText
            delay(650L)
            visibleComboText = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1B2631))
            .border(
                width = 3.dp,
                color = Color(0xFF2ECC71),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(8.dp)
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInWindow()
                val cellSizePx = bounds.width / 8f
                val cellSizeDp = with(density) { cellSizePx.toDp() }
                onBoardPositioned(bounds, cellSizeDp)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0..7) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    for (c in 0..7) {
                        val placedBlock = grid[r][c]

                        // Check if cell is covered by hover preview
                        var isPreview = false
                        var isValidPreview = false

                        if (hoverShape != null) {
                            val relativeX = c - hoverGridX
                            val relativeY = r - hoverGridY
                            if (relativeY in 0 until hoverShape.height &&
                                relativeX in 0 until hoverShape.width &&
                                hoverShape.grid[relativeY][relativeX]
                            ) {
                                isPreview = true
                                isValidPreview = isHoverValid
                            }
                        }

                        val isLineHighlighted = r in highlightedRows || c in highlightedCols
                        val lineHighlightColor = if (isLineHighlighted && hoverShape != null) hoverShape.color else null

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        ) {
                            BlockCell(
                                placedBlock = if (isPreview) null else placedBlock,
                                cellSize = Dp.Unspecified,
                                modifier = Modifier.fillMaxSize(),
                                isPreview = isPreview,
                                isValidPreview = isValidPreview,
                                isLineHighlighted = isLineHighlighted,
                                highlightColor = lineHighlightColor
                            )
                        }
                    }
                }
            }
        }

        // Animated Banner for Combos / Clears
        AnimatedVisibility(
            visible = visibleComboText != null,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                initialScale = 0.5f
            ) + fadeIn(tween(150)) + slideInVertically(
                animationSpec = tween(200),
                initialOffsetY = { -it / 2 }
            ),
            exit = scaleOut(
                animationSpec = tween(180),
                targetScale = 0.7f
            ) + fadeOut(tween(180)) + slideOutVertically(
                animationSpec = tween(180),
                targetOffsetY = { -it / 2 }
            )
        ) {
            visibleComboText?.let { text ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEE5253))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}
