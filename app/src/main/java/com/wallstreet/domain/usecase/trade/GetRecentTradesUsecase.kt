package com.wallstreet.domain.usecase.trade

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.TradeRepository
import com.wallstreet.presentation.home.TimePeriod
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

class GetTradesUsecase(private val tradeRepository: TradeRepository) {
     operator fun invoke(userId: String, period:TimePeriod, limit: Int): Flow<List<Trade>> {
         if(period == TimePeriod.ALL) {
            return tradeRepository.getAllTrades(userId)
         }
         val cutOfMillis = LocalDate.now()
             .minus(period.toDuration())
             .atStartOfDay(ZoneId.systemDefault())
             .toInstant()
             .toEpochMilli()

         return tradeRepository.getRecentTrades(userId, cutOfMillis, 100);
    }
}


fun TimePeriod.toDuration(): Period =
    when(this) {
        TimePeriod.ONE_WEEk -> Period.ofDays(7)
        TimePeriod.THREE_MONTHS -> Period.ofMonths(3)
        TimePeriod.SIX_MONTHS -> Period.ofMonths(6)
        TimePeriod.ONE_YEAR -> Period.ofYears(1)
        TimePeriod.ALL -> Period.ofYears(2)
        TimePeriod.ONE_MONTH -> Period.ofMonths(1)
    }
