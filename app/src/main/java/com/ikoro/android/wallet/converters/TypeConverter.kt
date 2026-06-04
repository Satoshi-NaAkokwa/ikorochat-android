package com.ikoro.android.wallet.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ikoro.android.data.model.Currency
import com.ikoro.android.wallet.model.CoinData

/**
 * TypeConverters for Room database
 * Handles serialization/deserialization of complex types to/from JSON
 */
class TypeConverter {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromCoinData(coinData: CoinData?): String? {
        return coinData?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toCoinData(json: String?): CoinData? {
        return json?.let { gson.fromJson(it, CoinData::class.java) }
    }
    
    @TypeConverter
    fun fromCurrencyList(list: List<Currency>?): String? {
        return list?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toCurrencyList(json: String?): List<Currency>? {
        return json?.let { 
            val type = object : TypeToken<List<Currency>>() {}.type
            gson.fromJson(it, type)
        }
    }
}
