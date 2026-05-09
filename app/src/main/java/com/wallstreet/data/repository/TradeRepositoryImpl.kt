package com.wallstreet.data.repository

import com.wallstreet.core.result.Result
import com.wallstreet.data.local.dao.TradeDao
import com.wallstreet.data.mapper.toDomain
import com.wallstreet.data.mapper.toEntity
import com.wallstreet.data.sync.SyncScheduler
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TradeRepositoryImpl(
    private val tradeDao: TradeDao,
    private val syncScheduler: SyncScheduler
) : TradeRepository {

    override fun getAllTrades(userId: String): Flow<List<Trade>> =
        tradeDao.getAllTrades(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addTrade(trade: Trade): Result<String> {
        return try {
            val entity = trade.toEntity()
            tradeDao.insertTrade(entity)
            syncScheduler.scheduleSync(trade.userId)
            Result.Success(entity.id)
        } catch (e: Exception) {
            Result.Error(e.toString())
        }
    }

    override fun getRecentTrades(userId: String, fromMilis: Long, limit: Int): Flow<List<Trade>> =
        tradeDao.getAllTrades(userId).map { entities ->
            entities.map { it.toDomain() }
                .filter { it.tradeDate >= fromMilis }
                .take(limit)
        }
}
