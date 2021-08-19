/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.requests

import dev.icerock.moko.web3.BlockState
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.Web3RpcRequest
import dev.icerock.moko.web3.entity.Transaction
import dev.icerock.moko.web3.entity.TransactionReceipt
import dev.icerock.moko.web3.serializer.BigIntSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

object Web3Requests {
    fun send(signedTransaction: String) = Web3RpcRequest(
        method = "eth_sendRawTransaction",
        params = listOf(signedTransaction),
        paramsSerializer = String.serializer(),
        resultSerializer = String.serializer()
    )
    fun <T> call(
        transactionCall: JsonElement,
        responseDataSerializer: KSerializer<T>,
        blockState: BlockState = BlockState.Latest
    ) = Web3RpcRequest(
        method = "eth_call",
        params = listOf(transactionCall, JsonPrimitive(blockState.toString())),
        paramsSerializer = JsonElement.serializer(),
        resultSerializer = responseDataSerializer
    )
    fun getEthTransactionCount(
        walletAddress: WalletAddress,
        blockState: BlockState = BlockState.Pending
    ) = Web3RpcRequest(
        method = "eth_getTransactionCount",
        params = listOf(walletAddress.value, blockState.toString()),
        paramsSerializer = String.serializer(),
        resultSerializer = BigIntSerializer
    )
    fun getTransactionReceipt(
        transactionHash: TransactionHash
    ) = Web3RpcRequest(
        method = "eth_getTransactionReceipt",
        params = listOf(transactionHash.value),
        paramsSerializer = String.serializer(),
        resultSerializer = TransactionReceipt.serializer()
    )
    fun getEthBalance(
        walletAddress: WalletAddress,
        blockState: BlockState = BlockState.Latest
    ) = Web3RpcRequest(
        method = "eth_getBalance",
        params = listOf(walletAddress.value, blockState.toString()),
        paramsSerializer = String.serializer(),
        resultSerializer = BigIntSerializer
    )
    fun getTransaction(
        transactionHash: TransactionHash
    ) = Web3RpcRequest(
        method = "eth_getTransactionByHash",
        params = listOf(transactionHash.value),
        paramsSerializer = String.serializer(),
        resultSerializer = Transaction.serializer()
    )
}
