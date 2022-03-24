/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:UseSerializers(BigIntSerializer::class)

package dev.icerock.moko.web3.entity

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.BlockHash
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.serializer.BigIntSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers

@Serializable
data class Transaction(
    @SerialName("from")
    private val _from: String, // 20 Bytes - address of the sender
    @SerialName("hash")
    private val _hash: String, // 32 Bytes - hash of the transaction
    @SerialName("to")
    private val _to: String?, // 20 Bytes - address of the receiver. null when its a contract creation transaction
    @SerialName("blockHash")
    private val _blockHash: String?, // 32 Bytes - hash of the block where this transaction was in. null when its pending
    @SerialName("gas")
    val gasLimit: BigInt, // gas provided by the sender
    val gasPrice: BigInt, // gas price provided by the sender in Wei
    val value: BigInt, // value transferred in Wei
    val blockNumber: BigInt?, // block number where this transaction was in. null when its pending
    val input: String, // the data send along with the transaction
    val nonce: BigInt, // the number of transactions made by the sender prior to this one
    val transactionIndex: BigInt,
    val r: String,
    val s: String,
    val v: String,
    /** there insn't here https://infura.io/docs/ethereum#operation/eth_getTransactionReceipt **/
    val type: BigInt
) {
    @Transient
    val receiverAddress: WalletAddress? = this._to?.let { WalletAddress(it) }

    @Transient
    val senderAddress: WalletAddress = WalletAddress(this._from)

    @Transient
    val txHash: TransactionHash = TransactionHash(this._hash)

    @Transient
    val blockHash: BlockHash? = this._blockHash?.let { BlockHash(it) }
}
