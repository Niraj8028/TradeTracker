package com.wallstreet.data.local.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter fun fromStringList(list: List<String>): String = list.joinToString(",")
    @TypeConverter fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(",")

    @TypeConverter fun fromLocalDate(date: LocalDate?): String? = date?.toString()
    @TypeConverter fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }

    @TypeConverter fun fromLocalTime(time: LocalTime?): String? = time?.toString()
    @TypeConverter fun toLocalTime(value: String?): LocalTime? =
        value?.let { LocalTime.parse(it) }
}