/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.entity.Transaction
import dev.icerock.moko.web3.entity.TransactionReceipt
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement

interface Web3RPC {
    suspend fun getTransaction(transactionHash: TransactionHash): Transaction
    suspend fun getTransactionReceipt(transactionHash: TransactionHash): TransactionReceipt
    suspend fun getEthBalance(
        walletAddress: WalletAddress,
        blockState: BlockState = BlockState.Latest
    ): BigInt

    suspend fun getEthTransactionCount(
        walletAddress: WalletAddress,
        blockState: BlockState = BlockState.Pending
    ): BigInt

    suspend fun <T> call(
        transactionCall: JsonElement,
        responseDataSerializer: KSerializer<T>,
        blockState: BlockState = BlockState.Latest,
    ): T

    suspend fun send(
        signedTransaction: String
    ): TransactionHash
}
