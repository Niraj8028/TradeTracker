package com.wallstreet.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallstreet.core.preferences.ThemePreferences
import com.wallstreet.core.preferences.ThemeTypes
import com.wallstreet.ui.theme.White
import kotlinx.coroutines.launch


@Composable
fun ThemeToggle() {

    val context = LocalContext.current
    val themePrefs = remember { ThemePreferences(context) }
    val selectedTheme by themePrefs.theme.collectAsState(initial = ThemeTypes.SYSTEM)
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
//            .border(
//                width = 1.dp,
//                color = MaterialTheme.colorScheme.outline,
//                shape = RoundedCornerShape(12.dp),
//            )

            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        ThemeTypes.entries.forEach { theme ->
            val isSelected = theme == selectedTheme

            Box(
                modifier = Modifier
                    .weight(1f)

                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(vertical = 10.dp)

                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        scope.launch {
                            themePrefs.setTheme(theme)
                        }
                    },

                contentAlignment = Alignment.Center
            ) {
                Text(
                    theme.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )

            }

        }
    }
}