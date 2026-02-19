package com.wallstreet.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wallstreet.data.local.dao.TradeDao
import com.wallstreet.data.local.dao.JournalDao
import com.wallstreet.data.local.dao.StrategyDao
import com.wallstreet.data.local.dao.MistakeDao
import com.wallstreet.data.local.entity.TradeEntity
import com.wallstreet.data.local.entity.JournalEntity
import com.wallstreet.data.local.entity.StrategyEntity
import com.wallstreet.data.local.entity.MistakeTagEntity

@Database(
    entities = [
        TradeEntity::class,
        JournalEntity::class,
        StrategyEntity::class,
        MistakeTagEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao
    abstract fun journalDao(): JournalDao
    abstract fun strategyDao(): StrategyDao
    abstract fun mistakeDao(): MistakeDao
}