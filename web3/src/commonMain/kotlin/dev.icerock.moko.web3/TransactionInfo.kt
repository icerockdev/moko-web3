/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:UseSerializers(BigIntSerializer::class)

package dev.icerock.moko.web3

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.serializer.BigIntSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class TransactionInfo(
    val hash: TransactionHash,
    val nonce: BigInt,
    val blockHash: BlockHash,
    val blockNumber: BigInt?,
    val transactionIndex: BigInt?,
    val from: EthereumAddress,
    val to: EthereumAddress?,
    val value: BigInt,
    val gasPrice: BigInt,
    val gas: BigInt,
    val input: String
)
