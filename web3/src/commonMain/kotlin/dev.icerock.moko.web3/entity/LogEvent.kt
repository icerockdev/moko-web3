/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:UseSerializers(BigIntSerializer::class)

package dev.icerock.moko.web3.entity

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.BlockHash
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.serializer.BigIntSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers

@Serializable
class LogEvent(
    @SerialName("address")
    private val _address: String,
    @SerialName("blockHash")
    private val _blockHash: String,
    @SerialName("transactionHash")
    private val _transactionHash: String,
    val blockNumber: BigInt,
    val data: String,
    val logIndex: BigInt,
    val removed: Boolean,
    val topics: List<String>,
    val transactionIndex: BigInt
) {
    @Transient
    val address: ContractAddress = ContractAddress(_address)

    @Transient
    val blockHash: BlockHash = BlockHash(_blockHash)

    @Transient
    val txHash: TransactionHash = TransactionHash(_transactionHash)
}
