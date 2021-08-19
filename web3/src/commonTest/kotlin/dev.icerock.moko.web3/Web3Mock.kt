/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */


package dev.icerock.moko.web3

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import kotlinx.serialization.json.Json

internal fun createMockWeb3(mock: MockRequestHandler): Web3 {
    val httpClient = HttpClient(MockEngine) {
        engine {
            addHandler(mock)
        }
        install(Logging) {
            logger = StdoutHttpLogger()
            level = LogLevel.ALL
        }
    }
    return Web3(
        httpClient = httpClient,
        json = Json,
        infuraUrl = "https://localhost/"
    )
}
