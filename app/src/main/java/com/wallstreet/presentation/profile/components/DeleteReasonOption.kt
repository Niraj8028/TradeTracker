package com.wallstreet.presentation.profile.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.wallstreet.presentation.profile.screens.DeleteReason
import com.wallstreet.ui.theme.LocalBorderColors


@Composable
fun DeleteReasonOption(reason: DeleteReason, selected: Boolean, onSelect: () -> Unit) {
    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = if (selected) LocalBorderColors.current.primary else LocalBorderColors.current.secondary,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(
                indication = null,
                interactionSource = remember
                { MutableInteractionSource() }) {
                onSelect()
            },
        verticalAlignment = Alignment.CenterVertically

    ) {
        RadioButton(
            selected = selected, onClick = onSelect,
            modifier = Modifier.size(20.dp)

        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(reason.title)

    }
}