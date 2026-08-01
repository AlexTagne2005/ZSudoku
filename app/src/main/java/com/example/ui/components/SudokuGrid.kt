package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SudokuCell
import com.example.ui.theme.CellSelectedDark
import com.example.ui.theme.CellSelectedLight
import com.example.ui.theme.CrosshairHighlightDark
import com.example.ui.theme.CrosshairHighlightLight
import com.example.ui.theme.DarkGivenNumberColor
import com.example.ui.theme.DarkUserNumberColor
import com.example.ui.theme.GivenNumberColor
import com.example.ui.theme.MatchingDigitHighlightDark
import com.example.ui.theme.MatchingDigitHighlightLight
import com.example.ui.theme.UserNumberColor

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToDown

@Composable
fun SudokuGrid(
    cells: List<SudokuCell>,
    selectedRow: Int?,
    selectedCol: Int?,
    selectedDigit: Int?,
    isDarkTheme: Boolean,
    notesStyle: String = "GRID",
    onCellClick: (row: Int, col: Int) -> Unit,
    onTwoFingerTapToClear: () -> Unit = {},
    onSwipeToggleInputMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    val thickLineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, thickLineColor, RoundedCornerShape(16.dp))
            .padding(2.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.size >= 2 && event.changes.any { it.changedToDown() }) {
                            onTwoFingerTapToClear()
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                var totalDragX = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDragX = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount
                    },
                    onDragEnd = {
                        if (kotlin.math.abs(totalDragX) > 60f) {
                            onSwipeToggleInputMode()
                        }
                    }
                )
            }
            .testTag("sudoku_grid")
    ) {
        val gridWidth = maxWidth

        Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0 until 9) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (c in 0 until 9) {
                        val cellIndex = r * 9 + c
                        val cell = if (cellIndex in cells.indices) cells[cellIndex] else SudokuCell(r, c)

                        val isSelected = selectedRow == r && selectedCol == c
                        val isInCrosshair = (selectedRow == r || selectedCol == c || (selectedRow != null && selectedCol != null && selectedRow / 3 == r / 3 && selectedCol / 3 == c / 3))
                        val isMatchingDigit = selectedDigit != null && selectedDigit != 0 && (cell.value == selectedDigit || (cell.value == 0 && cell.notes.contains(selectedDigit)))

                        // Cell Background color selection
                        val targetBgColor = when {
                            isSelected -> if (isDarkTheme) CellSelectedDark else CellSelectedLight
                            isMatchingDigit -> if (isDarkTheme) MatchingDigitHighlightDark else MatchingDigitHighlightLight
                            isInCrosshair -> if (isDarkTheme) CrosshairHighlightDark else CrosshairHighlightLight
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val animatedBgColor by animateColorAsState(
                            targetValue = targetBgColor,
                            animationSpec = spring(),
                            label = "CellBg"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(animatedBgColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onCellClick(r, c) }
                                .testTag("cell_${r}_${c}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (cell.value != 0) {
                                val textColor = when {
                                    cell.isError -> MaterialTheme.colorScheme.error
                                    cell.isHinted -> MaterialTheme.colorScheme.tertiary
                                    cell.isGiven -> if (isDarkTheme) DarkGivenNumberColor else GivenNumberColor
                                    else -> if (isDarkTheme) DarkUserNumberColor else UserNumberColor
                                }

                                Text(
                                    text = cell.value.toString(),
                                    fontSize = (gridWidth.value / 18).sp,
                                    fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                            } else if (cell.notes.isNotEmpty()) {
                                if (notesStyle == "CENTERED") {
                                    PencilNotesCenteredList(notes = cell.notes, isDarkTheme = isDarkTheme)
                                } else {
                                    PencilNotesGrid(notes = cell.notes, isDarkTheme = isDarkTheme)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Overlay 3x3 Thick Grid Lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val cellW = width / 9f
            val cellH = height / 9f

            // Thin lines
            for (i in 1 until 9) {
                if (i % 3 != 0) {
                    // Vertical thin line
                    drawLine(
                        color = outlineColor,
                        start = Offset(cellW * i, 0f),
                        end = Offset(cellW * i, height),
                        strokeWidth = 1.dp.toPx()
                    )
                    // Horizontal thin line
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, cellH * i),
                        end = Offset(width, cellH * i),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // Thick lines for 3x3 blocks
            for (i in 1 until 3) {
                // Vertical thick line
                drawLine(
                    color = thickLineColor,
                    start = Offset(cellW * i * 3, 0f),
                    end = Offset(cellW * i * 3, height),
                    strokeWidth = 2.5.dp.toPx()
                )
                // Horizontal thick line
                drawLine(
                    color = thickLineColor,
                    start = Offset(0f, cellH * i * 3),
                    end = Offset(width, cellH * i * 3),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        }
    }
}

@Composable
private fun PencilNotesGrid(
    notes: Set<Int>,
    isDarkTheme: Boolean
) {
    val noteColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier.fillMaxSize().padding(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in 0 until 3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (col in 0 until 3) {
                    val num = row * 3 + col + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (notes.contains(num)) {
                            Text(
                                text = num.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                color = noteColor,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PencilNotesCenteredList(
    notes: Set<Int>,
    isDarkTheme: Boolean
) {
    val noteColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val sortedNotesStr = remember(notes) { notes.sorted().joinToString(" ") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = sortedNotesStr,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = noteColor,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp,
            maxLines = 2
        )
    }
}
