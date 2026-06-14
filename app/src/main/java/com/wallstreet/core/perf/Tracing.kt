package com.wallstreet.core.perf

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

suspend fun <T> withTrace(name: String, block: suspend (Trace) -> T): T {
    val trace = FirebasePerformance.getInstance().newTrace(name)
    trace.start()
    return try {
        block(trace)
    } finally {
        trace.stop()
    }
}
