package com.wallstreet.presentation.analytics.components

import com.wallstreet.core.util.formatPercent
import com.wallstreet.core.util.formatPnl
import com.wallstreet.domain.model.MistakeStat
import com.wallstreet.domain.model.MistakesAnalysisData

/** A single actionable suggestion rendered in the Mistakes tab. */
data class MistakeSuggestion(
    val title: String,
    val detail: String,
    val isWarning: Boolean
)

/**
 * Curated, hand-written advice keyed by the canonical mistake names in
 * [com.wallstreet.core.constants.AppConstants.mistakes]. Falls back to a generic
 * line for any unmapped tag so new mistake types still render gracefully.
 */
private val curatedAdvice: Map<String, String> = mapOf(
    "FOMO" to "You're chasing entries after the move has already started. Wait for a pullback or the next clean setup instead of buying strength.",
    "Early Exit" to "You're cutting winners short. Try scaling out in parts or trailing a stop so profits can run to your target.",
    "Large Size" to "Oversized positions amplify emotional decisions. Size every trade to a fixed % risk of your account.",
    "Revenge Trade" to "Trading to win back a loss rarely works. Step away after a losing streak and reset before re-entering.",
    "No Stop Loss" to "Trading without a stop exposes you to uncapped losses. Define your exit before you enter the trade.",
    "No Setup" to "You're entering without a defined edge. Only take trades that match a rule from your playbook.",
    "SL Trailed" to "You trailed your stop too aggressively and got shaken out before the move completed. Give price enough room to breathe while still protecting profit.",
    "Small SL" to "Your stop was too tight relative to the setup's natural volatility, leading to an early stop-out. Size the stop to the structure, then adjust position size to fit your risk."
)

private fun adviceFor(name: String): String =
    curatedAdvice[name] ?: "Look back at the trades you tagged “$name” and write one rule that stops it happening again."

/**
 * Combines real per-mistake statistics with curated coaching advice. The result is
 * ordered most-impactful first so the worst leaks surface at the top.
 */
fun buildMistakeSuggestions(data: MistakesAnalysisData, symbol: String = "$"): List<MistakeSuggestion> {
    val tagged = data.topMistakes.filter { it.count > 0 }
    if (tagged.isEmpty()) return emptyList()

    val result = mutableListOf<MistakeSuggestion>()

    // Headline: discipline pays. Only worth showing once there are clean trades to compare.
    if (data.cleanTradeWinRate > 0.0) {
        result += MistakeSuggestion(
            title = "Discipline pays off",
            detail = "Your clean trades — none tagged with a mistake — win ${data.cleanTradeWinRate.formatPercent()} of the time. " +
                "Following your own rules is the biggest edge you have, so protect it.",
            isWarning = false
        )
    }

    // One coaching line per mistake, worst financial impact first.
    tagged.sortedBy { it.totalPnlImpact }.forEach { stat ->
        result += stat.toSuggestion(symbol)
    }

    return result
}

private fun MistakeStat.toSuggestion(symbol: String): MistakeSuggestion {
    val impactClause = if (totalPnlImpact < 0) {
        "cost you ${totalPnlImpact.formatPnl(symbol)} across $count ${tradeWord(count)} (${winRate.formatPercent()} win rate)."
    } else {
        "appeared in $count ${tradeWord(count)} (${winRate.formatPercent()} win rate)."
    }
    return MistakeSuggestion(
        title = name,
        detail = "$name $impactClause ${adviceFor(name)}",
        isWarning = totalPnlImpact < 0
    )
}

private fun tradeWord(count: Int) = if (count == 1) "trade" else "trades"
