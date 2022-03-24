/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:UseSerializers(BigIntSerializer::class)

package dev.icerock.moko.web3.entity

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.BlockHash
import dev.icerock.moko.web3.SyncingWeb3SocketEvent
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.serializer.BigIntSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers

@Serializable
data class TransactionReceipt(
    /** 32 Bytes - hash of the block where this transaction was in **/
    @SerialName("blockHash")
    private val _blockHash: String,
    /** 20 Bytes - address of the sender **/
    @SerialName("from")
    private val _from: String,
    /** 20 Bytes - address of the receiver. Null when the transaction is a contract creation transaction **/
    @SerialName("to")
    private val _to: String?,
    /** 32 Bytes - hash of the transaction **/
    @SerialName("transactionHash")
    private val _transactionHash: String,
    /** block number where this transaction was in **/
    val blockNumber: BigInt,
    /** 20 Bytes - the contract address created, if the transaction was a contract creation, otherwise - null **/
    val contractAddress: String?,
    /** the total amount of gas used when this transaction was executed in the block **/
    val cumulativeGasUsed: BigInt,
    /** the amount of gas used by this specific transaction alone **/
    val gasUsed: BigInt,
    /** Array of log objects, which this transaction generated **/
    val logs: List<LogEvent>,
    /** 256 Bytes - Bloom filter for light clients to quickly retrieve related logs **/
    val logsBloom: String,
    /** either 1 (success) or 0 (failure) **/
    val status: Status,
    /** integer of the transactions index position in the block **/
    val transactionIndex: BigInt,
    /** The transaction type: 0x0 for Legacy transactions, 0x1 for Access List transactions, 0x2 for 1559 Transactions **/
    val type: BigInt,
    /** optional, didn't document, found in rinkeby response **/
    val effectiveGasPrice: BigInt? = null
) {
    @Transient
    val receiverAddress: WalletAddress? = this._to?.let { WalletAddress(it) }

    @Transient
    val senderAddress: WalletAddress = WalletAddress(this._from)

    @Transient
    val txHash: TransactionHash = TransactionHash(this._transactionHash)

    @Transient
    val blockHash: BlockHash = BlockHash(this._blockHash)

    @Serializable
    enum class Status {
        @SerialName("0x1")
        SUCCESS,

        @SerialName("0x0")
        FAILURE
    }

    @Serializable
    enum class Type {
        @SerialName("0x0")
        Legacy,

        @SerialName("0x1")
        AccessList,

        @SerialName("0x2")
        EIP1559
    }
}
