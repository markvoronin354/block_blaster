package com.markvoronin.blockblaster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.markvoronin.blockblaster.model.BlockShape

@Composable
fun PieceTrayView(
    candidateShapes: List<BlockShape?>,
    draggingShapeIndex: Int?,
    boardCellSizeDp: Dp,
    onDragStart: (index: Int, shape: BlockShape, startPosInWindow: Offset) -> Unit,
    onDrag: (dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1B2631)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            candidateShapes.forEachIndexed { index, shape ->
                var slotBoundsInWindow by remember(index) { mutableStateOf<Rect?>(null) }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .onGloballyPositioned { coordinates ->
                            slotBoundsInWindow = coordinates.boundsInWindow()
                        }
                        .then(
                            if (shape != null) {
                                Modifier.pointerInput(shape) {
                                    detectDragGestures(
                                        onDragStart = { localOffset ->
                                            val bounds = slotBoundsInWindow
                                            if (bounds != null) {
                                                val startPos = bounds.topLeft + localOffset
                                                onDragStart(index, shape, startPos)
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            onDrag(dragAmount)
                                        },
                                        onDragEnd = {
                                            onDragEnd()
                                        },
                                        onDragCancel = {
                                            onDragEnd()
                                        }
                                    )
                                }
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (shape != null && draggingShapeIndex != index) {
                        val trayCellSize = 18.dp
                        BlockShapeView(
                            shape = shape,
                            cellSize = trayCellSize
                        )
                    }
                }
            }
        }
    }
}
