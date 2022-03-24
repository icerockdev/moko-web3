/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.websockets

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit

actual fun log(message: String) {
    println("KCWS: $message")
}

actual fun createHttpClientEngine(): HttpClientEngine {
    return OkHttp.create {
        config {
            retryOnConnectionFailure(true)
            pingInterval(30, TimeUnit.SECONDS)
        }
    }
}