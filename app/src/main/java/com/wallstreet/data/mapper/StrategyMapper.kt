package com.wallstreet.data.mapper

import com.wallstreet.data.local.entity.StrategyEntity
import com.wallstreet.domain.model.Strategy

fun StrategyEntity.toDomain() = Strategy(
    id = id, userId = userId, name = name,
    description = description, winRate = winRate,
    profitFactor = profitFactor, netPnl = netPnl,
    expectedValue = expectedValue, tradeIds = tradeIds,
    synced = synced
)

fun Strategy.toEntity() = StrategyEntity(
    id = id, userId = userId, name = name,
    description = description, winRate = winRate,
    profitFactor = profitFactor, netPnl = netPnl,
    expectedValue = expectedValue, tradeIds = tradeIds,
    synced = synced
)