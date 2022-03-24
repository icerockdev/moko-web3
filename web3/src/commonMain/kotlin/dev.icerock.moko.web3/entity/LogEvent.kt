/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:UseSerializers(BigIntSerializer::class)

package dev.icerock.moko.web3.entity

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.BlockHash
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.serializer.BigIntSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers

@Serializable
data class LogEvent(
    val address: EthereumAddress,
    val blockHash: BlockHash,
    val transactionHash: TransactionHash,
    val blockNumber: BigInt,
    val data: HexString,
    val logIndex: BigInt,
    val removed: Boolean,
    val topics: List<Hex32String>,
    val transactionIndex: BigInt
) {
    fun <T> deserializeData(dataDeserializer: DataDeserializer<T>): T =
        dataDeserializer.deserialize(data.withoutPrefix.chunked(size = 32 * 2).map(::Hex32String))

    fun interface DataDeserializer<T> {
        fun deserialize(source: List<Hex32String>): T
    }
}
