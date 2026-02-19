package com.wallstreet.data.repository

import com.wallstreet.data.local.dao.TradeDao
import com.wallstreet.data.mapper.toDomain
import com.wallstreet.data.mapper.toEntity
import com.wallstreet.domain.model.AssetType
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class TradeRepositoryImpl(private val dao: TradeDao) : TradeRepository {

    override fun getTradeHistory(
        assetType: AssetType?,
        from: LocalDate?,
        to: LocalDate?
    ): Flow<List<Trade>> {
        val flow = if (assetType != null)
            dao.getByAssetType(assetType.name)
        else
            dao.getAll()

        return flow.map { entities ->
            entities.map { it.toDomain() }
                .filter { trade ->
                    (from == null || !trade.date.isBefore(from)) &&
                            (to == null || !trade.date.isAfter(to))
                }
        }
    }

    override fun getRecentTrades(limit: Int): Flow<List<Trade>> =
        dao.getRecent(limit).map { it.map { e -> e.toDomain() } }

    override suspend fun getTradeById(id: String): Trade? =
        dao.getById(id)?.toDomain()

    override suspend fun saveTrade(trade: Trade) =
        dao.upsert(trade.toEntity())

    override suspend fun deleteTrade(id: String) =
        dao.delete(id)
}