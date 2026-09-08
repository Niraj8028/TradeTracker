package com.wallstreet.core.util

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalView

enum class HapticStyle {
    Light,   // nav taps, list items, chips, filter selections
    Medium,  // standard buttons, confirmations, primary CTA
    Strong   // destructive actions (delete, logout confirm)
}

fun View.haptic(style: HapticStyle = HapticStyle.Light) {
    performHapticFeedback(
        when (style) {
            HapticStyle.Light  -> HapticFeedbackConstants.KEYBOARD_TAP
            HapticStyle.Medium -> HapticFeedbackConstants.CONTEXT_CLICK
            HapticStyle.Strong -> HapticFeedbackConstants.LONG_PRESS
        }
    )
}

fun Modifier.hapticClickable(
    style: HapticStyle = HapticStyle.Light,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val view = LocalView.current
    clickable(enabled = enabled) {
        view.haptic(style)
        onClick()
    }
}
