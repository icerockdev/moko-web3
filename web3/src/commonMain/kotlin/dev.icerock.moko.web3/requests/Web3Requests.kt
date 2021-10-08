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
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

object Web3Requests {
    fun send(signedTransaction: String) = Web3RpcRequest(
        method = "eth_sendRawTransaction",
        params = listOf(signedTransaction),
        paramsSerializer = String.serializer(),
        resultSerializer = TransactionHash.serializer()
    )
    fun <T> call(
        transactionCall: JsonElement,
        responseDataDeserializer: DeserializationStrategy<T>,
        blockState: BlockState = BlockState.Latest
    ) = Web3RpcRequest(
        method = "eth_call",
        params = listOf(transactionCall, JsonPrimitive(blockState.toString())),
        paramsSerializer = JsonElement.serializer(),
        resultSerializer = responseDataDeserializer
    )
    fun getNativeTransactionCount(
        walletAddress: WalletAddress,
        blockState: BlockState = BlockState.Pending
    ) = Web3RpcRequest(
        method = "eth_getTransactionCount",
        params = listOf(walletAddress.prefixed, blockState.toString()),
        paramsSerializer = String.serializer(),
        resultSerializer = BigIntSerializer
    )
    fun getTransactionReceipt(
        transactionHash: TransactionHash
    ) = Web3RpcRequest(
        method = "eth_getTransactionReceipt",
        params = listOf(transactionHash.prefixed),
        paramsSerializer = String.serializer(),
        resultSerializer = TransactionReceipt.serializer().nullable
    )
    fun getNativeBalance(
        walletAddress: WalletAddress,
        blockState: BlockState = BlockState.Latest
    ) = Web3RpcRequest(
        method = "eth_getBalance",
        params = listOf(walletAddress.prefixed, blockState.toString()),
        paramsSerializer = String.serializer(),
        resultSerializer = BigIntSerializer
    )
    fun getTransaction(
        transactionHash: TransactionHash
    ) = Web3RpcRequest(
        method = "eth_getTransactionByHash",
        params = listOf(transactionHash.prefixed),
        paramsSerializer = String.serializer(),
        resultSerializer = Transaction.serializer()
    )
    fun getGasPrice() = Web3RpcRequest(
        method = "eth_gasPrice",
        params = listOf(),
        paramsSerializer = ListSerializer(Unit.serializer()),
        resultSerializer = BigIntSerializer
    )
}
