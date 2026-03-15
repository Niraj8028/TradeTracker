package com.wallstreet.presentation.log_trade.components

import android.text.Layout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wallstreet.ui.theme.DarkTextSecondary
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import com.wallstreet.ui.theme.DarkTextTertiary
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.White

@Composable
fun MistakesSection(
    selectedMistakes: Set<String>,
    mistakes: List<String>,
    onMistakeToggled: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "MISTAKES IDENTIFIED",
            style = MaterialTheme.typography.labelMedium,
            color = DarkTextSecondary
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            mistakes.forEach { mistake ->
                val isSelected = selectedMistakes.contains(mistake)
                Surface(
                    onClick = { onMistakeToggled(mistake) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) PrimaryBlue else Color.Transparent,
                    border = if (!isSelected) {
                        androidx.compose.foundation.BorderStroke(1.dp, DarkTextTertiary)
                    } else null,
                    modifier = Modifier.height(32.dp).wrapContentWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = mistake,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) White else DarkTextSecondary
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val sequences = mutableListOf<List<Placeable>>()
        val currentSequence = mutableListOf<Placeable>()
        var currentWidth = 0
        var currentHeight = 0
        var maxHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0))

            if (currentWidth + placeable.width > constraints.maxWidth && currentSequence.isNotEmpty()) {
                sequences.add(currentSequence.toList())
                currentSequence.clear()
                maxHeight += currentHeight + 8.dp.roundToPx()
                currentHeight = 0
                currentWidth = 0
            }

            currentSequence.add(placeable)
            currentWidth += placeable.width + 8.dp.roundToPx()
            currentHeight = maxOf(currentHeight, placeable.height)
        }

        if (currentSequence.isNotEmpty()) {
            sequences.add(currentSequence)
            maxHeight += currentHeight
        }

        layout(constraints.maxWidth, maxHeight) {
            var yPosition = 0
            sequences.forEach { sequence ->
                var xPosition = 0
                var rowHeight = 0
                sequence.forEach { placeable ->
                    placeable.place(xPosition, yPosition)
                    xPosition += placeable.width + 8.dp.roundToPx()
                    rowHeight = maxOf(rowHeight, placeable.height)
                }
                yPosition += rowHeight + 8.dp.roundToPx()
            }
        }
    }
}
