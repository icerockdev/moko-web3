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