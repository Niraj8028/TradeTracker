package com.wallstreet.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wallstreet.data.local.dao.TradeDao
import com.wallstreet.data.local.entity.TradeEntity

@Database(entities = [TradeEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao
}
