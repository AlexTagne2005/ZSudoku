package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InputMode
import com.example.model.SudokuCell

@Composable
fun SudokuKeypad(
    cells: List<SudokuCell>,
    selectedDigit: Int?,
    inputMode: InputMode,
    onDigitClick: (Int) -> Unit,
    onToggleInputMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Count remaining for each digit 1..9
    val remainingCounts = (1..9).associateWith { digit ->
        val count = cells.count { it.value == digit }
        maxOf(0, 9 - count)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Dual Mode Switch Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (inputMode == InputMode.CELL_FIRST) "Mode: Select Cell → Tap Digit" else "Mode: Select Digit → Tap Cells",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                onClick = onToggleInputMode,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.testTag("toggle_input_mode")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch Input Mode",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (inputMode == InputMode.CELL_FIRST) "Digit First" else "Cell First",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 1-9 Keypad Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (digit in 1..9) {
                val remaining = remainingCounts[digit] ?: 9
                val isSelectedDigit = selectedDigit == digit && inputMode == InputMode.DIGIT_FIRST
                val isCompletedDigit = remaining == 0

                if (isCompletedDigit && !isSelectedDigit) {
                    // Completed digit disappears from keypad
                    Box(modifier = Modifier.width(36.dp).height(50.dp))
                } else {
                    KeypadButton(
                        digit = digit,
                        remaining = remaining,
                        isSelected = isSelectedDigit,
                        isCompleted = isCompletedDigit,
                        onClick = { onDigitClick(digit) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    digit: Int,
    remaining: Int,
    isSelected: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "KeyBg"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onPrimary
            isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "KeyTextColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .shadow(if (isSelected) 4.dp else 1.dp, CircleShape)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(enabled = !isCompleted || isSelected, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 10.dp)
            .testTag("keypad_$digit")
    ) {
        Text(
            text = digit.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Text(
            text = if (isCompleted) "✓" else "$remaining",
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
