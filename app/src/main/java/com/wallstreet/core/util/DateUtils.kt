package com.wallstreet.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    private val displayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    private val groupFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

    fun formatDisplay(date: LocalDate): String = date.format(displayFormatter)

    fun formatGroupHeader(date: LocalDate): String = when (date) {
        LocalDate.now() -> "Today ${date.format(DateTimeFormatter.ofPattern("MMM d"))}"
        LocalDate.now().minusDays(1) -> "Yesterday ${date.format(DateTimeFormatter.ofPattern("MMM d"))}"
        else -> date.format(groupFormatter)
    }
}