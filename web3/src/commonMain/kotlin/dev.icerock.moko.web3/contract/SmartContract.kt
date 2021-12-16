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
import dev.icerock.moko.web3.contract.ABIEncoder.encodeCallDataForMethod
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.requests.Web3Requests
import dev.icerock.moko.web3.requests.executeBatch
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
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
    @Suppress("UNCHECKED_CAST")
    private fun <T> makeAbiDeserializer(method: String): DeserializationStrategy<List<T>> =
        object : DeserializationStrategy<List<T>> {
            override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
                serialName = "StringAbiPrimitiveDeserializer",
                kind = PrimitiveKind.STRING
            )
            override fun deserialize(decoder: Decoder): List<T> {
                val abiResult = HexString(decoder.decodeString())
                return ABIDecoder.decodeCallDataForOutputs(abiJson, method, abiResult) as List<T>
            }
        }

    fun <T> readRequest(
        method: String,
        params: List<Any>
    ): Web3RpcRequest<JsonElement, List<T>> {
        val data = encodeCallDataForMethod(abiJson, method, params)
        return Web3Requests.call(contractAddress, data, makeAbiDeserializer(method))
    }

    suspend fun <T> read(
        method: String,
        params: List<Any>,
    ): List<T> = executor.executeBatch(readRequest<T>(method, params)).first()

    @Web3Stub
    fun writeRequest(
        method: String,
        params: List<Any>,
        from: WalletAddress? = null,
        value: BigInt?
    ): Web3RpcRequest<String, TransactionHash> {
        TODO("For future releases")
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
        TODO("For future release")
    }
}

