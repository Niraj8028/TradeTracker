package com.wallstreet.domain.repository

import com.wallstreet.domain.model.MistakeTag
import kotlinx.coroutines.flow.Flow

interface MistakeRepository {
    fun getMistakeFrequency(): Flow<Map<MistakeTag, Int>>
    fun getMistakePnlImpact(): Flow<Map<MistakeTag, Double>>
}