package com.wallstreet.domain.model

enum class AssetType(val displayName: String) {
    STOCKS("Stocks"),
    CRYPTO("Crypto"),
    FUTURES("Futures"),
    OPTIONS("Options"),
    FOREX("Forex")
}