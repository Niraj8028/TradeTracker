package com.wallstreet.presentation.log_trade.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallstreet.domain.model.TradeType
import com.wallstreet.ui.theme.DarkSurfaceVariant
import com.wallstreet.ui.theme.DarkTextSecondary
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.White

@Composable
fun TradeTypeToggle(
    selectedType: TradeType,
    onTypeSelected: (TradeType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TradeType.entries.forEach { type ->
            val isSelected = selectedType == type
            Box(
                modifier = Modifier.weight(1f)
                    .background(
                        if (isSelected) PrimaryBlue else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onTypeSelected(type) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (type == TradeType.LONG) "Buy / Long" else "Sell / Short",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) White else DarkTextSecondary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }

}