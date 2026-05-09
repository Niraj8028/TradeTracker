package com.wallstreet.data.local.database

import androidx.room.TypeConverter
import com.wallstreet.data.local.entity.SyncStatus
import com.wallstreet.domain.model.TradeType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun fromStringList(list: List<String>): String = Json.encodeToString(list)

    @TypeConverter
    fun toStringList(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun fromTradeType(value: TradeType): String = value.name

    @TypeConverter
    fun toTradeType(value: String): TradeType = TradeType.valueOf(value)

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}
