package com.wallstreet.domain.usecase.strategy

import com.wallstreet.domain.model.strategy.StrategyDetail
import com.wallstreet.domain.repository.StrategyRepository
import com.wallstreet.domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

//class GetStrategyDetailUsecase(
//    private val tradeRepository: TradeRepository,
//    private val strategyRepository: StrategyRepository
//    ) {
//    suspend operator fun invoke(strategyId: String, userId: String, period: TimePeriod): Flow<StrategyDetail>
//            = combine(
//                tradeRepository.getRecentTrades(userId, period.toDuration()),
//                strategyRepository.getStrategy(strategyId)
//
//            )
//}