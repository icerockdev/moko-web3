/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import kotlinx.serialization.Serializable

@Serializable
data class NewHeadsWeb3SocketEvent(
    val difficulty: String,
    val extraData: String,
    val gasLimit: String,
    val gasUsed: String,
    val logsBloom: String,
    val miner: String,
    val nonce: String,
    val number: String,
    val parentHash: String,
    val receiptsRoot: String,
    val sha3Uncles: String,
    val stateRoot: String,
    val timestamp: String,
    val transactionsRoot: String,
    val hash: String
)
