package com.wallstreet.domain.model

enum class MistakeTag(val displayName: String) {
    FOMO("FOMO"),
    EARLY_EXIT("Early Exit"),
    NO_STOP_LOSS("No Stop Loss"),
    OVER_LEVERAGING("Over-leveraging"),
    CHASING("Chasing"),
    LATE_EXIT("Late Exit"),
    REVENGE_TRADE("Revenge Trade"),
    LARGE_SIZE("Large Size")
}