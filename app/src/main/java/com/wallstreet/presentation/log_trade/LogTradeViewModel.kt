package com.wallstreet.presentation.log_trade

import androidx.lifecycle.ViewModel
import com.wallstreet.domain.repository.TradeRepository

class LogTradeViewModel(
    private val tradeRepository: TradeRepository
): ViewModel() {
}