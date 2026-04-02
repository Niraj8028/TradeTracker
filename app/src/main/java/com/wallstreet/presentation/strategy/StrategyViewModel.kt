package com.wallstreet.presentation.strategy

import androidx.lifecycle.ViewModel
import com.wallstreet.domain.usecase.strategy.AddStrategyUseCase
import com.wallstreet.domain.usecase.strategy.DeleteStrategyUseCase
import com.wallstreet.domain.usecase.strategy.GetStrategyUseCase
import com.wallstreet.domain.usecase.strategy.UpdateStrategyUseCase

class StrategyViewModel(
    private val updateStrategyUseCase: UpdateStrategyUseCase,
    private val getStrategyUseCase: GetStrategyUseCase,
    private val deleteStrategyUseCase: DeleteStrategyUseCase,
    private val addStrategyUseCase: AddStrategyUseCase
) : ViewModel() {


}