/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.requests

import dev.icerock.moko.web3.BlockHash
import dev.icerock.moko.web3.BlockInfo
import dev.icerock.moko.web3.BlockState
import dev.icerock.moko.web3.BlockStateSerializer
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3RpcRequest
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.entity.Transaction
import dev.icerock.moko.web3.entity.TransactionReceipt
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.serializer.BigIntSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement

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
    fun getBlockNumber() = Web3RpcRequest(
        method = "eth_blockNumber",
        params = listOf(),
        paramsSerializer = ListSerializer(Unit.serializer()),
        resultSerializer = BigIntSerializer
    )
    fun getBlockByNumber(block: BlockState) = Web3RpcRequest(
        method = "eth_getBlockByNumber",
        params = listOf(Json.encodeToJsonElement(BlockStateSerializer, block), JsonPrimitive(value = true)),
        paramsSerializer = JsonElement.serializer(),
        resultSerializer = BlockInfo.serializer().nullable
    )
    @Serializable
    private data class GetLogsObject(
        val address: EthereumAddress?,
        val fromBlock: BlockState?,
        val toBlock: BlockState?,
        val topics: List<Hex32String>?,
        val blockHash: BlockHash?
    )
    fun getLogs(
        address: EthereumAddress? = null,
        fromBlock: BlockState? = null,
        toBlock: BlockState? = null,
        topics: List<Hex32String>? = null,
        blockHash: BlockHash? = null
    ) = Web3RpcRequest(
        method = "eth_getLogs",
        params = listOf(Json.encodeToJsonElement(GetLogsObject(address, fromBlock, toBlock, topics, blockHash))),
        paramsSerializer = JsonElement.serializer(),
        resultSerializer = ListSerializer(LogEvent.serializer())
    )
}
