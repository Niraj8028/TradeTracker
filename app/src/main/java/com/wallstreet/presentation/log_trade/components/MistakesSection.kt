package com.wallstreet.presentation.log_trade.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.dp

@Composable
fun MistakesSection(
    selectedMistakes: Set<String>,
    mistakes: List<String>,
    onMistakeToggled: (String) -> Unit
) {
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
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.background,
                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                else null,
                modifier = Modifier
                    .height(28.dp)
                    .wrapContentWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = mistake,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
        var totalHeight = 0
        val gap = 8.dp.roundToPx()

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0))
            if (currentWidth + placeable.width > constraints.maxWidth && currentSequence.isNotEmpty()) {
                sequences.add(currentSequence.toList())
                currentSequence.clear()
                totalHeight += currentHeight + gap
                currentHeight = 0
                currentWidth = 0
            }
            currentSequence.add(placeable)
            currentWidth += placeable.width + gap
            currentHeight = maxOf(currentHeight, placeable.height)
        }

        if (currentSequence.isNotEmpty()) {
            sequences.add(currentSequence)
            totalHeight += currentHeight
        }

        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            sequences.forEach { row ->
                var x = 0
                var rowHeight = 0
                row.forEach { placeable ->
                    placeable.place(x, y)
                    x += placeable.width + gap
                    rowHeight = maxOf(rowHeight, placeable.height)
                }
                y += rowHeight + gap
            }
        }
    }
}
