/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.requests.polling

import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.requests.getLogs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

fun Web3Executor.newLogsShortPolling(
    address: EthereumAddress? = null,
    topics: List<Hex32String> = listOf(),
    pollingInterval: Long = 5_000
): Flow<LogEvent> = newBlocksShortPolling(pollingInterval)
    .transform { block ->
        getLogs(address, topics = topics, blockHash = block.hash)
            .forEach { emit(it) }
    }
