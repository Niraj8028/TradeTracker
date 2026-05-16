package com.wallstreet.data.mapper

import com.wallstreet.data.local.entity.SyncStatus
import com.wallstreet.data.local.entity.TradeEntity
import com.wallstreet.domain.model.Trade

fun TradeEntity.toDomain(): Trade = Trade(
    id = id,
    symbol = symbol,
    entryPrice = entryPrice,
    exitPrice = exitPrice,
    quantity = quantity,
    tradeType = tradeType,
    strategyId = strategyId,
    tradeDate = tradeDate,
    profitLoss = profitLoss,
    profitLossPercentage = profitLossPercentage,
    strategy = strategy,
    notes = notes,
    imageUrl = imageUrl,
    userId = userId,
    comments = comments,
    mistakes = mistakes,
    createAt = createdAt
)

fun Trade.toEntity(
    syncStatus: SyncStatus = SyncStatus.PENDING
): TradeEntity = TradeEntity(
    id = id.ifEmpty { java.util.UUID.randomUUID().toString() },
    symbol = symbol,
    entryPrice = entryPrice,
    exitPrice = exitPrice,
    quantity = quantity,
    tradeType = tradeType,
    strategyId = strategyId,
    tradeDate = tradeDate,
    profitLoss = profitLoss,
    profitLossPercentage = profitLossPercentage,
    strategy = strategy,
    notes = notes,
    imageUrl = imageUrl,
    userId = userId,
    comments = comments,
    mistakes = mistakes,
    createdAt = createAt,
    syncStatus = syncStatus
)
