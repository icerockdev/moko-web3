/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.requests.polling

import kotlinx.coroutines.delay
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class TimeOutException(val timeout: Long) : Throwable(message = "Max timeout exceed ($timeout millis)")

@OptIn(ExperimentalTime::class)
suspend inline fun <T> shortPollingUntilSuccess(
    timeOutMillis: Long?,
    intervalMillis: Long,
    noinline block: suspend () -> Result<T>
): T {
    var timeSpent = 0L
    while(timeOutMillis == null || timeOutMillis > timeSpent) {
        val millis = measureTime {
            block()
                .takeIf(Result<*>::isSuccess)
                ?.run { return getOrThrow() }
            delay(intervalMillis)
        }.inWholeMilliseconds
        timeSpent += millis
    }
    throw TimeOutException(timeOutMillis)
}

suspend inline fun <T> shortPollingUntilNotNull(
    timeOutMillis: Long?,
    intervalMillis: Long,
    noinline block: suspend () -> T?
): T = shortPollingUntilSuccess(timeOutMillis, intervalMillis) {
    val value = block() ?: return@shortPollingUntilSuccess Result.failure(IllegalStateException())
    return@shortPollingUntilSuccess Result.success(value)
}
