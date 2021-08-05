package dev.icerock.moko.web3.websockets

import io.ktor.client.engine.HttpClientEngine

expect fun log(message: String)

expect fun createHttpClientEngine(): HttpClientEngine
