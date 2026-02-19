package com.wallstreet.core.util

import androidx.compose.ui.graphics.Color

object ColorUtils {
    val profitGreen = Color(0xFF00C087)
    val lossRed = Color(0xFFFF4C4C)
    val neutral = Color(0xFF8A8A8A)

    fun pnlColor(pnl: Double): Color = when {
        pnl > 0 -> profitGreen
        pnl < 0 -> lossRed
        else -> neutral
    }
}