/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.requests.polling

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.BlockHash
import dev.icerock.moko.web3.BlockInfo
import dev.icerock.moko.web3.BlockState
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.requests.Web3Requests
import dev.icerock.moko.web3.requests.getBlockNumber
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transform


private operator fun BigInt.rangeTo(other: BigInt): Iterable<BigInt> =
    generateSequence(seed = this) { block -> (block + 1).takeIf { it <= other} }.asIterable()

fun Web3Executor.newBlocksShortPolling(
    fromBlock: BigInt? = null,
    pollingInterval: Long = 5_000
): Flow<BlockInfo> =
    flow {
        var previousBlockNumber = fromBlock ?: getBlockNumber()
        while (true) {
            delay(pollingInterval)
            val blockNumber = getBlockNumber()
            if (blockNumber != previousBlockNumber) {
                emit(value = previousBlockNumber to blockNumber)
                previousBlockNumber = blockNumber
            }
        }
    }.transform { (fromBlock, toBlock): Pair<BigInt, BigInt> ->
        val blockNumbers = fromBlock..(toBlock - 1)

        val requests = blockNumbers
            .map(BlockState::Quantity)
            .map(Web3Requests::getBlockByNumber)

        executeBatch(requests)
            .forEach { block ->
                block ?: error("Block does not seem to exist")
                emit(block)
            }
    }
