package com.wallstreet.domain.model.insights

/** One canonical mistake tag, current window vs the equal-length prior window. */
data class MistakeComparison(
    val name: String,
    val count: Int,
    val prevCount: Int,
    val totalImpact: Double,
    val prevImpact: Double,
    val winRate: Double,
) {
    val delta: Int get() = count - prevCount
    val isNew: Boolean get() = prevCount == 0 && count > 0
    val isCleared: Boolean get() = prevCount > 0 && count == 0
}
