package com.wallstreet.domain.insights.copy

/**
 * All user-facing insight strings. Templates use `{token}` placeholders filled by the
 * detectors (numbers already formatted). The per-mistake coaching paragraphs are the ones
 * previously hard-coded in the presentation layer, moved here verbatim.
 */
object InsightCopy {

    private val templates: Map<String, String> = mapOf(
        "mistake.costliest" to
            "{mistake} cost you {impact} across {count} {tradeWord} this period — your single most expensive leak. {advice}",
        "mistake.overallRate.rising" to
            "You tagged a mistake on {rate} of trades, up from {prev} last period. Tighten your checklist before entries.",
        "mistake.overallRate.falling" to
            "Mistake-tagged trades dropped from {prev} to {rate} — you're trading cleaner.",
        "mistake.repeating" to
            "You tagged {mistake} {count}×, up from {prev}× — it isn't improving. {advice}",
        "mistake.reducing" to
            "You cut {mistake} from {prev}× to {count}× — keep that going.",
        "mistake.cleared" to
            "Zero {mistake} trades this period, down from {prev}×. That habit looks fixed.",
        "mistake.new" to
            "{mistake} is new this period ({count} {tradeWord}, {impact}). {advice}",
        "mistake.cleanEdge" to
            "Your clean trades win {cleanWr} vs {taggedWr} when you tag a mistake — discipline is worth about {gap} points.",
        "mistake.concentration" to
            "{mistake} shows up in {pct} of your flagged trades — fixing this one moves the needle most.",
        "mistake.doNotEnter" to
            "Trades where you tag {mistake} win only {winRate} — treat the tag as a do-not-enter signal. {advice}",
        "strategy.scaleUp" to
            "{strategy} is your edge — {pf} profit factor over {trades} trades ({pnl}). Consider sizing it up.",
        "strategy.drop" to
            "{strategy} has lost {pnl} across {trades} trades (profit factor {pf}). Park it or paper-trade until it turns.",
        "strategy.declining" to
            "{strategy} slipped from {prevWr} to {curWr} win rate this period — review recent entries.",
        "strategy.improving" to
            "{strategy} is trending up — {prevWr} → {curWr} win rate, {pnl} this period.",
        "strategy.needsData" to
            "Only {trades} trades on {strategy} this period — you need about {min} for a reliable read.",
        "strategy.steady" to
            "{strategy} grinds steadily — max drawdown only {dd} against {pnl} profit.",
        "direction.edge" to
            "You win {betterWr} going {betterSide} vs {worseWr} the other way — favour {betterSide} setups.",
        "direction.bleeding" to
            "Your {loserSide} trades are in the red ({loserPnl}) while {winnerSide} is green. Trade {loserSide} smaller — or skip it — until it turns around.",
        "trend.edge" to
            "You're sharpest in {bestLabel} markets — {bestWr} win rate vs {worstWr} in {worstLabel}. Prioritise {bestLabel} setups.",
        "trend.losing" to
            "{label} trades are bleeding {pnl} overall — sit them out until you find an edge.",
        "timing.bestDay" to
            "{day} is your strongest day ({pnl}). Protect your process the rest of the week.",
        "timing.worstDay" to
            "{day} bleeds {pnl} across {trades} trades — trade lighter then.",
        "risk.profitFactor" to
            "Your profit factor is {pf} — losers are outweighing winners. Tighten your exits.",
        "risk.drawdown" to
            "Your worst equity dip was {dd}. Size down after two losses to avoid deep holes.",
        "discipline.lossStreak" to
            "You're on a {n}-trade losing streak — the data says step back and reset.",
    )

    // Moved verbatim from presentation/analytics/components/MistakeSuggestions.kt.
    private val mistakeAdvice: Map<String, String> = mapOf(
        "FOMO" to "You're chasing entries after the move has already started. Wait for a pullback or the next clean setup instead of buying strength.",
        "Early Exit" to "You're cutting winners short. Try scaling out in parts or trailing a stop so profits can run to your target.",
        "Large Size" to "Oversized positions amplify emotional decisions. Size every trade to a fixed % risk of your account.",
        "Revenge Trade" to "Trading to win back a loss rarely works. Step away after a losing streak and reset before re-entering.",
        "No Stop Loss" to "Trading without a stop exposes you to uncapped losses. Define your exit before you enter the trade.",
        "No Setup" to "You're entering without a defined edge. Only take trades that match a rule from your playbook.",
        "SL Trailed" to "You trailed your stop too aggressively and got shaken out before the move completed. Give price enough room to breathe while still protecting profit.",
        "Small SL" to "Your stop was too tight relative to the setup's natural volatility, leading to an early stop-out. Size the stop to the structure, then adjust position size to fit your risk.",
    )

    fun advice(mistakeName: String): String =
        mistakeAdvice[mistakeName]
            ?: "Look back at the trades you tagged “$mistakeName” and write one rule that stops it happening again."

    fun hasTemplate(key: String): Boolean = templates.containsKey(key)

    /** Substitutes `{token}` placeholders; unknown keys throw so template drift is caught in tests. */
    fun render(key: String, args: Map<String, String>): String {
        val template = templates[key] ?: error("No insight copy template for '$key'")
        var out = template
        for ((k, v) in args) out = out.replace("{$k}", v)
        // An empty {advice} etc. can leave a double space — normalise.
        return out.replace(Regex(" {2,}"), " ").trim()
    }
}
