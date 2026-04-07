package com.wallstreet.data.mapper

import com.wallstreet.data.model.strategyDto
import com.wallstreet.domain.model.Strategy

fun strategyDto.toDomain(): Strategy {
    return Strategy(
        id = id,
        userId = userId,
        name = name,
        description = description,
        isCustom = isCustom,
        createAt = createAt,

        )
}


fun Strategy.toDto(): strategyDto {
    return strategyDto(


        userId = userId,
        name = name,
        description = description,
        isCustom = isCustom,
        createAt = createAt,

        )
}