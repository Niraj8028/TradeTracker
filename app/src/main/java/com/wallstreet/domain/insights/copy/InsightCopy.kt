package com.wallstreet.domain.insights.copy

/**
 * All user-facing insight strings. Templates use `{token}` placeholders filled by the
 * detectors (numbers already formatted). Copy is deliberately short and plain-spoken: one
 * observation, one instruction, no em-dashes.
 */
object InsightCopy {

    private val templates: Map<String, String> = mapOf(
        "mistake.costliest" to
            "{mistake} is your biggest leak this period: {impact} across {count} {tradeWord}. {advice}",
        "mistake.overallRate.rising" to
            "You flagged a mistake on {rate} of trades this period, up from {prev}. Slow down before you enter.",
        "mistake.overallRate.falling" to
            "Flagged mistakes fell from {prev} to {rate}. Cleaner trading is showing up.",
        "mistake.repeating" to
            "{mistake} went from {prev}x to {count}x this period. It isn't improving. {advice}",
        "mistake.reducing" to
            "{mistake} is down from {prev}x to {count}x. Keep it going.",
        "mistake.cleared" to
            "No {mistake} trades this period, down from {prev}x. That one looks fixed.",
        "mistake.new" to
            "{mistake} showed up for the first time this period: {count} {tradeWord}, {impact}. {advice}",
        "mistake.cleanEdge" to
            "Clean trades win {cleanWr}. Trades with a mistake tag win {taggedWr}. That is {gap} points of edge.",
        "mistake.concentration" to
            "{mistake} is behind {pct} of your flagged trades. Fix this one first.",
        "mistake.doNotEnter" to
            "{mistake} trades win just {winRate}. Treat the tag as a signal to skip the trade.",
        "strategy.scaleUp" to
            "{strategy} is working: {pf} profit factor over {trades} trades, {pnl}. Give it more size.",
        "strategy.drop" to
            "{strategy} has lost {pnl} over {trades} trades at a {pf} profit factor. Park it until it turns around.",
        "strategy.declining" to
            "{strategy} win rate fell from {prevWr} to {curWr} this period. Check your recent entries.",
        "strategy.improving" to
            "{strategy} is picking up: win rate {prevWr} to {curWr}, {pnl} this period.",
        "strategy.needsData" to
            "Only {trades} trades on {strategy} this period. Give it {min} or so before judging it.",
        "strategy.steady" to
            "{strategy} is steady: {dd} max drawdown against {pnl} profit. Keep running it.",
        "direction.edge" to
            "You win {betterWr} going {betterSide}, {worseWr} the other way. Favour {betterSide} setups.",
        "direction.bleeding" to
            "Your {loserSide} trades are down {loserPnl} while {winnerSide} is green. Trade {loserSide} smaller until it turns.",
        "trend.edge" to
            "You trade {bestLabel} markets best: {bestWr} win rate against {worstWr} in {worstLabel}. Lean into {bestLabel}.",
        "trend.losing" to
            "{label} trades have cost you {pnl} overall. Sit them out until you find an edge there.",
        "timing.bestDay" to
            "{day} is your best day at {pnl}. Whatever you do then, keep doing it.",
        "timing.worstDay" to
            "{day} has cost you {pnl} over {trades} trades. Trade smaller, or not at all, on {day}.",
        "risk.profitFactor" to
            "Your profit factor is {pf}. Losers are bigger than winners right now. Tighten your exits.",
        "risk.drawdown" to
            "Your worst drawdown this period was {dd}. Cut size after two losses in a row.",
        "discipline.lossStreak" to
            "You're {n} losing trades in a row. Step away and reset before the next one.",
    )

    // Short, plain coaching lines per canonical mistake tag.
    private val mistakeAdvice: Map<String, String> = mapOf(
        "FOMO" to "You're chasing the move after it's already going. Wait for a pullback or the next clean setup.",
        "Early Exit" to "You're cutting winners short. Scale out in pieces or trail a stop so they can run.",
        "Large Size" to "Big positions make you trade scared. Risk a fixed percent of your account on every trade.",
        "Revenge Trade" to "Chasing a loss back rarely works. Walk away after a losing streak and reset.",
        "No Stop Loss" to "With no stop the loss has no floor. Decide your exit before you enter.",
        "No Setup" to "You're entering without an edge. Only take trades that match a rule in your playbook.",
        "SL Trailed" to "You trailed your stop too tight and got shaken out early. Give the trade room while still protecting profit.",
        "Small SL" to "Your stop was tighter than the setup's normal noise. Set it to the structure, then size the position to your risk.",
    )

    fun advice(mistakeName: String): String =
        mistakeAdvice[mistakeName]
            ?: "Pull up your “$mistakeName” trades and write one rule to stop it repeating."

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
