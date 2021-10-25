/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.Web3RpcRequest
import dev.icerock.moko.web3.annotation.Web3Stub
import dev.icerock.moko.web3.contract.MethodEncoder.createCallData
import dev.icerock.moko.web3.requests.Web3Requests
import dev.icerock.moko.web3.requests.executeBatch
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SmartContract(
    private val executor: Web3Executor,
    val contractAddress: ContractAddress,
    private val abiJson: JsonArray
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val methodsAbi: Map<String, JsonObject> = abiJson
        .map { it.jsonObject }
        .filter { it.containsKey("name") }
        .associateBy { it.getValue("name").jsonPrimitive.content }

    fun <T> readRequest(
        method: String,
        params: List<Any>,
        from: WalletAddress? = null,
        dataDeserializer: DeserializationStrategy<T>
    ): Web3RpcRequest<JsonElement, T> {
        val transactionCall = encodeTransaction(method, params, from)
        val data = json.encodeToJsonElement(ContractRPC.serializer(), transactionCall)
        return Web3Requests.call(data, dataDeserializer)
    }

    suspend fun <T> read(
        method: String,
        params: List<Any>,
        from: WalletAddress? = null,
        dataSerializer: KSerializer<T>
    ): T = executor.executeBatch(readRequest(method, params, from, dataSerializer)).first()

    @Web3Stub
    fun writeRequest(
        method: String,
        params: List<Any>,
        from: WalletAddress? = null,
        value: BigInt?
    ): Web3RpcRequest<String, TransactionHash> {
        val transactionCall = encodeTransaction(method, params, from, value)
        val data = json.encodeToJsonElement(ContractRPC.serializer(), transactionCall)
        val signedTransaction: String = signTransaction(data)
        return Web3Requests.send(signedTransaction)
    }

    @Web3Stub
    suspend fun write(
        method: String,
        params: List<Any>,
        from: WalletAddress? = null,
        value: BigInt?
    ): TransactionHash = executor.executeBatch(writeRequest(method, params, from, value)).first()

    @Web3Stub
    fun signTransaction(data: JsonElement): String {
        TODO()
    }

    fun encodeTransaction(
        method: String,
        params: List<Any>,
        from: WalletAddress? = null,
        value: BigInt? = null
    ): ContractRPC {
        val callData: String = createCallData(abiJson, method, params)
        return ContractRPC(
            to = contractAddress.prefixed,
            from = from?.prefixed,
            data = callData,
            value = value
        )
    }
}

