package com.wallstreet.testutil

import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.TradeType
import com.wallstreet.domain.model.TrendDirection
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * Builders for deterministic [Trade] fixtures. `daysAgo` maps to a fixed `tradeDate` so
 * window-slicing tests are stable; `pnl == null` produces an open trade.
 */
object TradeFixtures {

    fun epochFor(daysAgo: Long, base: LocalDate = LocalDate.now()): Long =
        base.minusDays(daysAgo)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    fun trade(
        id: String = UUID.randomUUID().toString(),
        pnl: Double? = 100.0,
        daysAgo: Long = 1,
        mistakes: List<String> = emptyList(),
        strategyId: String? = null,
        type: TradeType = TradeType.LONG,
        trend: TrendDirection? = null,
        symbol: String = "AAPL",
        userId: String = "u1",
    ): Trade = Trade(
        id = id,
        symbol = symbol,
        entryPrice = 100.0,
        exitPrice = pnl?.let { 100.0 + it },
        quantity = 1.0,
        strategyId = strategyId,
        tradeType = type,
        tradeDate = epochFor(daysAgo),
        profitLoss = pnl,
        strategy = "",
        userId = userId,
        mistakes = mistakes,
        trendDirection = trend,
    )

    fun winners(n: Int, each: Double = 100.0, daysAgo: Long = 1): List<Trade> =
        List(n) { trade(pnl = each, daysAgo = daysAgo) }

    fun losers(
        n: Int,
        each: Double = -50.0,
        mistakes: List<String> = emptyList(),
        daysAgo: Long = 1,
    ): List<Trade> = List(n) { trade(pnl = each, mistakes = mistakes, daysAgo = daysAgo) }
}
