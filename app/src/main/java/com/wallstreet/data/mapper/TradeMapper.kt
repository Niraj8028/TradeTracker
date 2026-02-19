package com.wallstreet.data.mapper

import com.wallstreet.data.local.entity.TradeEntity
import com.wallstreet.domain.model.AssetType
import com.wallstreet.domain.model.MistakeTag
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.TradeSide

fun TradeEntity.toDomain() = Trade(
    id = id, userId = userId, symbol = symbol,
    side = TradeSide.valueOf(side),
    assetType = AssetType.valueOf(assetType),
    quantity = quantity, unit = unit,
    entryPrice = entryPrice, exitPrice = exitPrice,
    pnl = pnl, pnlPercent = pnlPercent, date = date,
    entryTime = entryTime, exitTime = exitTime,
    strategyId = strategyId,
    mistakes = mistakes.mapNotNull { runCatching { MistakeTag.valueOf(it) }.getOrNull() },
    tradingLogic = tradingLogic, psychologyNote = psychologyNote,
    attachmentUrls = attachmentUrls, synced = synced
)

fun Trade.toEntity() = TradeEntity(
    id = id, userId = userId, symbol = symbol,
    side = side.name, assetType = assetType.name,
    quantity = quantity, unit = unit,
    entryPrice = entryPrice, exitPrice = exitPrice,
    pnl = pnl, pnlPercent = pnlPercent, date = date,
    entryTime = entryTime, exitTime = exitTime,
    strategyId = strategyId,
    mistakes = mistakes.map { it.name },
    tradingLogic = tradingLogic, psychologyNote = psychologyNote,
    attachmentUrls = attachmentUrls, synced = synced
)