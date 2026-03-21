package com.wallstreet.domain.usecase.home

import com.wallstreet.domain.model.RecentTradeItem
import com.wallstreet.domain.model.Trade

class RecentTradesDataUsecase {
    operator fun invoke(trades: List<Trade>): List<RecentTradeItem> {
        return getRecentTradeData(trades)
    }
}

fun getRecentTradeData(trades: List<Trade>): List<RecentTradeItem> {
    return trades.sortedByDescending { it.tradeDate }
        .take(15)
//        TODO decide trade limit
        .map { trade ->
            RecentTradeItem(
                exitPrice = trade.exitPrice,
                entryPrice = trade.entryPrice,
                quanity = trade.quantity,
                tradeType = trade.tradeType,
                profitLoss = trade.profitLoss ?: 0.0,
                symbol = trade.symbol,
                id = trade.id
            )
        }

}