/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.requests.polling

import dev.icerock.moko.web3.BlockState
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.Web3RpcException
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.requests.getLogs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform

fun Web3Executor.newLogsShortPolling(
    address: EthereumAddress? = null,
    topics: List<Hex32String> = listOf(),
    fromBlock: BlockState? = null,
    pollingInterval: Long = 5_000
): Flow<LogEvent> = flow {
    if (fromBlock != null)
        getLogs(address, fromBlock, topics = topics)?.forEach { emit(it) }

    val newLogs = newBlocksShortPolling(pollingInterval = pollingInterval)
        .transform { block ->
            while (true) {
                // sometimes there is an exception because of unknown block,
                // but the block was returned from web3 provider, so
                // this is impossible that this block does not exist
                try {
                    getLogs(address, topics = topics, blockHash = block.hash)?.forEach { emit(it) } ?: continue
                } catch (_: Web3RpcException) { continue }
                break
            }
        }
    emitAll(newLogs)
}
