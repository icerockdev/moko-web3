/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.websockets

import io.ktor.client.engine.HttpClientEngine

expect fun log(message: String)

expect fun createHttpClientEngine(): HttpClientEngine
