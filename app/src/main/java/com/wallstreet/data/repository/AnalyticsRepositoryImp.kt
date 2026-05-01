package com.wallstreet.data.repository

import com.wallstreet.core.util.calculateTotalPnL
import com.wallstreet.core.util.calculateWinRate
import com.wallstreet.core.util.toDayOfWeek
import com.wallstreet.domain.model.DayPerformance
import com.wallstreet.domain.model.DayStats
import com.wallstreet.domain.model.TradeStats
import com.wallstreet.domain.model.TradeSummary
import com.wallstreet.domain.model.CalendarDay
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.TradeType
import com.wallstreet.domain.repository.AnalyticsRepository
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.abs

class AnalyticsRepositoryImp : AnalyticsRepository {

    override fun getTradeSummary(trades: List<Trade>): TradeSummary {
        val totalCount = trades.size
        val longTrades = trades.filter { it.tradeType == TradeType.LONG }
        val shortTrades = trades.filter { it.tradeType == TradeType.SHORT }

        fun calculateStats(filteredTrades: List<Trade>): TradeStats {
            val count = filteredTrades.size
            val pnl = filteredTrades.calculateTotalPnL()
            val winRate = filteredTrades.calculateWinRate()
            val percentage = if (totalCount > 0) (count.toDouble() / totalCount) * 100 else 0.0
            return TradeStats(count, pnl, winRate, percentage)
        }

        return TradeSummary(
            long = calculateStats(longTrades),
            short = calculateStats(shortTrades),
            totalTrades = totalCount
        )
    }

    override fun getDayPerformance(trades: List<Trade>): DayPerformance {
        val validTrades = trades.filter { it.tradeDate > 0 }

        val pnlByDay = validTrades.groupBy { it.toDayOfWeek() }
            .mapValues { (_, dayTrades) ->
                dayTrades.calculateTotalPnL()
            }

        val orderedDays = listOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        )

        val maxAbsPnl = pnlByDay.values.maxOfOrNull { abs(it) } ?: 0.0

        val dayStatsList = orderedDays.map { day ->
            val pnl = pnlByDay[day] ?: 0.0
            DayStats(
                day = day,
                pnl = pnl,
                fraction = if (maxAbsPnl > 0) (abs(pnl) / maxAbsPnl).toFloat() else 0f,
                isProfit = pnl > 0
            )
        }


        val bestDay: DayOfWeek? = orderedDays.maxByOrNull {
            pnlByDay[it] ?: Double.NEGATIVE_INFINITY
        }

        return DayPerformance(dayStatsList, bestDay)
    }

    override fun getCalendarData(yearMonth: YearMonth, trades: List<Trade>): List<CalendarDay> {
        val gridSize = 42
        val firstDay = yearMonth.atDay(1)
        val offset = firstDay.dayOfWeek.value % 7
        val daysInMonth = yearMonth.lengthOfMonth()
        val today = LocalDate.now()

        val tradesByDate = trades
            .filter { it.tradeDate > 0 }
            .groupBy {
                Instant.ofEpochMilli(it.tradeDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }

        return List(gridSize) { index ->
            when {
             
                index < offset -> CalendarDay(
                    date = null,
                    isCurrentMonth = false,
                    isToday = false,
                    isSelected = false,
                    pnl = 0.0,
                    tradeCount = 0
                )

                index < offset + daysInMonth -> {
                    val day = index - offset + 1
                    val date = yearMonth.atDay(day)
                    val dayTrades = tradesByDate[date] ?: emptyList()
                    val pnl = dayTrades.calculateTotalPnL()
                    CalendarDay(
                        date = date,
                        isCurrentMonth = true,
                        isToday = date == today,
                        isSelected = false,
                        pnl = pnl,
                        tradeCount = dayTrades.size
                    )
                }

                else -> CalendarDay(
                    date = null,
                    isCurrentMonth = false,
                    isToday = false,
                    isSelected = false,
                    pnl = 0.0,
                    tradeCount = 0
                )
            }
        }
    }
}