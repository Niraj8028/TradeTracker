package com.wallstreet.data.mapper

import UserStrategyDto
import com.wallstreet.domain.model.UserStrategy
import kotlin.String

fun UserStrategyDto.toDomain(): UserStrategy {
    return UserStrategy(

        id = id,
        userId = userId,
        name = name,
        description = description,
        isCustom = isCustom,
        createAt = createAt,

        )
}


fun UserStrategy.toDto(): UserStrategyDto {
    return UserStrategyDto(

        id = id,
        userId = userId,
        name = name,
        description = description,
        isCustom = isCustom,
        createAt = createAt,

        )
}