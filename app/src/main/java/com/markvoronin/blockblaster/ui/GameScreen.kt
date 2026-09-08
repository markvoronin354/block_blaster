package com.markvoronin.blockblaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.markvoronin.blockblaster.model.BlockShape
import com.markvoronin.blockblaster.ui.components.BoardView
import com.markvoronin.blockblaster.ui.components.BlockShapeView
import com.markvoronin.blockblaster.ui.components.GameOverDialog
import com.markvoronin.blockblaster.ui.components.HeaderView
import com.markvoronin.blockblaster.ui.components.PieceTrayView
import com.markvoronin.blockblaster.ui.components.ResetConfirmationDialog
import com.markvoronin.blockblaster.viewmodel.GameViewModel
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initHighScore(context)
    }

    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var draggingShape by remember { mutableStateOf<BlockShape?>(null) }
    var dragPositionWindow by remember { mutableStateOf<Offset?>(null) }

    var showResetDialog by remember { mutableStateOf(false) }

    var rootBounds by remember { mutableStateOf<Rect?>(null) }
    var boardBounds by remember { mutableStateOf<Rect?>(null) }
    var boardCellSizeDp by remember { mutableStateOf(40.dp) }

    // Hover calculation variables
    var hoverGridX by remember { mutableStateOf(-1) }
    var hoverGridY by remember { mutableStateOf(-1) }
    var isHoverValid by remember { mutableStateOf(false) }

    val currentDraggingShape = draggingShape
    val currentDragPos = dragPositionWindow
    val currentBoardBounds = boardBounds
    val currentRootBounds = rootBounds

    if (currentDraggingShape != null && currentDragPos != null && currentBoardBounds != null) {
        val cellSizePx = currentBoardBounds.width / 8f
        val fingerMargin = cellSizePx * 2.0f // Anchor shape 2 grid cells above touch point for clear visibility

        val shapeTopLeftX = currentDragPos.x - (currentDraggingShape.width * cellSizePx / 2f)
        val shapeTopLeftY = currentDragPos.y - fingerMargin - (currentDraggingShape.height * cellSizePx)

        val relativeX = shapeTopLeftX - currentBoardBounds.left
        val relativeY = shapeTopLeftY - currentBoardBounds.top

        val gx = (relativeX / cellSizePx + 0.5f).toInt()
        val gy = (relativeY / cellSizePx + 0.5f).toInt()

        if (gx in 0..(8 - currentDraggingShape.width) && gy in 0..(8 - currentDraggingShape.height)) {
            hoverGridX = gx
            hoverGridY = gy
            isHoverValid = viewModel.canPlaceShape(currentDraggingShape, gx, gy)
        } else {
            hoverGridX = -1
            hoverGridY = -1
            isHoverValid = false
        }
    } else {
        hoverGridX = -1
        hoverGridY = -1
        isHoverValid = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                rootBounds = coordinates.boundsInWindow()
            }
            .background(Color(0xFF141E30))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderView(
                score = uiState.score,
                highScore = uiState.highScore,
                streak = uiState.streak,
                onRestartClick = { showResetDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BoardView(
                grid = uiState.grid,
                hoverShape = if (isHoverValid) draggingShape else null,
                hoverGridX = hoverGridX,
                hoverGridY = hoverGridY,
                isHoverValid = isHoverValid,
                comboText = uiState.lastClearEvent?.comboText,
                onBoardPositioned = { bounds, cellSizeDp ->
                    boardBounds = bounds
                    boardCellSizeDp = cellSizeDp
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            PieceTrayView(
                candidateShapes = uiState.candidateShapes,
                draggingShapeIndex = draggingIndex,
                boardCellSizeDp = boardCellSizeDp,
                onDragStart = { index, shape, startPosInWindow ->
                    draggingIndex = index
                    draggingShape = shape
                    dragPositionWindow = startPosInWindow
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onDrag = { dragAmount ->
                    dragPositionWindow = (dragPositionWindow ?: Offset.Zero) + dragAmount
                },
                onDragEnd = {
                    val idx = draggingIndex
                    if (idx != null && isHoverValid && hoverGridX >= 0 && hoverGridY >= 0) {
                        val placed = viewModel.placeShape(idx, hoverGridX, hoverGridY, context)
                        if (placed) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                    draggingIndex = null
                    draggingShape = null
                    dragPositionWindow = null
                }
            )
        }

        // Floating overlay of dragged shape
        if (currentDraggingShape != null && currentDragPos != null && currentBoardBounds != null) {
            val cellSizePx = currentBoardBounds.width / 8f
            val fingerMargin = cellSizePx * 2.0f // Anchor shape 2 grid cells above touch point

            val shapeTopLeftWindowX = currentDragPos.x - (currentDraggingShape.width * cellSizePx / 2f)
            val shapeTopLeftWindowY = currentDragPos.y - fingerMargin - (currentDraggingShape.height * cellSizePx)

            val rootLeft = currentRootBounds?.left ?: 0f
            val rootTop = currentRootBounds?.top ?: 0f

            val shapeTopLeftLocalX = shapeTopLeftWindowX - rootLeft
            val shapeTopLeftLocalY = shapeTopLeftWindowY - rootTop

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = shapeTopLeftLocalX.roundToInt(),
                            y = shapeTopLeftLocalY.roundToInt()
                        )
                    }
            ) {
                BlockShapeView(
                    shape = currentDraggingShape,
                    cellSize = boardCellSizeDp,
                    alpha = 0.9f
                )
            }
        }

        // Reset Confirmation Dialog
        if (showResetDialog) {
            ResetConfirmationDialog(
                onConfirm = {
                    viewModel.restartGame()
                    showResetDialog = false
                },
                onDismiss = {
                    showResetDialog = false
                }
            )
        }

        // Game Over Dialog
        if (uiState.isGameOver) {
            GameOverDialog(
                score = uiState.score,
                highScore = uiState.highScore,
                onPlayAgainClick = { viewModel.restartGame() }
            )
        }
    }
}
