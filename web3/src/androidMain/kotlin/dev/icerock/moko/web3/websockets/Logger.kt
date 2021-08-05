package dev.icerock.moko.web3.websockets

import android.util.Log
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit

actual fun log(message: String) {
    Log.d("KCWS", message)
}

actual fun createHttpClientEngine(): HttpClientEngine {
    return OkHttp.create {
        config {
            retryOnConnectionFailure(true)
            pingInterval(30, TimeUnit.SECONDS)
        }
    }
}