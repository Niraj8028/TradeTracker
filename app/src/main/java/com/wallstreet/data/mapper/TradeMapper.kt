package com.wallstreet.data.mapper

import com.wallstreet.data.model.TradeDto
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.TradeType

fun TradeDto.toDomain(): Trade {
    return Trade(
        id = id,
        symbol = symbol,
        entryPrice = entryPrice,
        exitPrice = exitPrice,
        quantity = quantity,
        tradeType = TradeType.valueOf(tradeType.toString()),
        profitLoss = profitLoss,
        profitLossPercentage = profitLossPercentage,
        strategy = strategy,
        notes = notes,
        comments = comments,
        imageUrl = imageUrl,
        userId = userId,
        mistakes = mistakes,
        createAt = createdAt
    )
}

fun Trade.toDto(): TradeDto {
    return TradeDto(
        id = id,
        symbol = symbol,
        entryPrice = entryPrice,
        exitPrice = exitPrice,
        quantity = quantity,
        tradeType = TradeType.valueOf(tradeType.toString()),
        profitLoss = profitLoss,
        profitLossPercentage = profitLossPercentage,
        strategy = strategy,
        notes = notes,
        comments = comments,
        imageUrl = imageUrl,
        userId = userId,
        mistakes = mistakes,
        createdAt = createAt
    )
}
