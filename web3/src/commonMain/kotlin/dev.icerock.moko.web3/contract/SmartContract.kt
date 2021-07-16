/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.crypto.KeccakParameter
import dev.icerock.moko.web3.crypto.digestKeccak
import dev.icerock.moko.web3.crypto.toHex
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SmartContract(
    private val web3: Web3,
    val contractAddress: ContractAddress,
    abiJson: JsonArray
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val methodsAbi: Map<String, JsonObject> = abiJson
        .map { it.jsonObject }
        .filter { it.containsKey("name") }
        .associateBy { it.getValue("name").jsonPrimitive.content }

    suspend fun <T> read(
        method: String,
        params: List<Any>,
        from: WalletAddress? = null,
        dataSerializer: KSerializer<T>
    ): T {
        val transactionCall = encodeTransaction(method, params, from)
        val data = json.encodeToJsonElement(ContractRPC.serializer(), transactionCall)
        // TODO use output format from ABI to correct parse data
        //  for example uniswap respond with more data and we should parse it by ABI spec
        return web3.call(data, dataSerializer)
    }

    suspend fun write(
        method: String,
        params: List<Any>,
        from: WalletAddress? = null,
        value: BigInt?
    ): TransactionHash {
        val transactionCall = encodeTransaction(method, params, from, value)
        val data = json.encodeToJsonElement(ContractRPC.serializer(), transactionCall)
        val signedTransaction: String = signTransaction(data)
        return web3.send(signedTransaction)
    }

    fun signTransaction(data: JsonElement): String {
        return TODO()
    }

    fun encodeTransaction(
        method: String,
        params: List<Any>,
        from: WalletAddress? = null,
        value: BigInt? = null
    ): ContractRPC {
        val callData: String = createCallData(method, params)
        return ContractRPC(
            to = contractAddress.value,
            from = from?.value,
            data = callData,
            value = value
        )
    }

    private fun createCallData(method: String, params: List<Any>): String {
        val methodAbi: JsonObject = methodsAbi[method] ?: throw AbiMethodNotFoundException(
            method,
            methodsAbi
        )
        val inputParams: List<JsonObject> =
            methodAbi.getValue("inputs").jsonArray.map { it.jsonObject }

        val methodSignature: ByteArray = generateMethodSignature(method, inputParams)
        val paramsEncoded: List<ByteArray> = params.mapIndexed { index, data ->
            val typeString: String = inputParams[index].getValue("type").jsonPrimitive.content
            val encoder: Encoder<Any> = createParamByType(typeString)
            encoder.encode(data)
        }

        val data: ByteArray =
            paramsEncoded.fold(methodSignature) { accumulated, item -> accumulated.plus(item) }

        return "0x" + data.toHex().toLowerCase()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun generateMethodSignature(
        method: String,
        inputParams: List<JsonObject>
    ): ByteArray {
        val signature = StringBuilder().apply {
            append(method)
            append("(")
            inputParams.forEachIndexed { index, param ->
                if (index != 0) append(",")
                append(param.getValue("type").jsonPrimitive.content)
            }
            append(")")
        }.toString().toByteArray()
        val sha3 = signature.digestKeccak(KeccakParameter.KECCAK_256)
        return sha3.copyOf(4)
    }

    private fun createParamByType(type: String): Encoder<Any> {
        @Suppress("UNCHECKED_CAST")
        return when (type) {
            "uint256" -> UInt256Param() as Encoder<Any>
            "address" -> AddressParam() as Encoder<Any>
            else -> TODO()
        }
    }
}
