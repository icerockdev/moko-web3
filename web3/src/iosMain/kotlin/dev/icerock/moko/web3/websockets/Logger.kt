/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.websockets

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.ios.Ios
import platform.Foundation.NSLog

actual fun log(message: String) {
    NSLog(message)
}

actual fun createHttpClientEngine(): HttpClientEngine {
    return WSIosHttpClientEngine(Ios.create { })
}