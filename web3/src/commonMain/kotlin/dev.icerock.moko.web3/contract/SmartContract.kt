/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3RpcRequest
import dev.icerock.moko.web3.annotation.Web3Stub
import dev.icerock.moko.web3.crypto.KeccakParameter
import dev.icerock.moko.web3.crypto.digestKeccak
import dev.icerock.moko.web3.crypto.toHex
import dev.icerock.moko.web3.requests.Web3Requests
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

    fun <T> readRequest(
        method: String,
        params: List<Any>,
        from: WalletAddress? = null,
        dataSerializer: KSerializer<T>
    ): Web3RpcRequest<JsonElement, T> {
        val transactionCall = encodeTransaction(method, params, from)
        val data = json.encodeToJsonElement(ContractRPC.serializer(), transactionCall)
        return Web3Requests.call(data, dataSerializer)
    }

    suspend fun <T> read(
        method: String,
        params: List<Any>,
        from: WalletAddress? = null,
        dataSerializer: KSerializer<T>
    ): T = web3.executeBatch(readRequest(method, params, from, dataSerializer)).first()

    @Web3Stub
    fun writeRequest(
        method: String,
        params: List<Any>,
        from: WalletAddress? = null,
        value: BigInt?
    ): Web3RpcRequest<String, String> {
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
    ): TransactionHash = web3.executeBatch(
        writeRequest(method, params, from, value)
    ).first().let(::TransactionHash)

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
        val callData: String = createCallData(method, params)
        return ContractRPC(
            to = contractAddress.value,
            from = from?.value,
            data = callData,
            value = value
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> Encoder<T>.encodeUnchecked(value: Any) = encode(value as T)

    private fun createCallData(method: String, params: List<Any>): String {
        val methodAbi: JsonObject = methodsAbi[method] ?: throw AbiMethodNotFoundException(
            method,
            methodsAbi
        )
        val inputParams: List<JsonObject> =
            methodAbi.getValue("inputs").jsonArray.map { it.jsonObject }

        val methodSignature: ByteArray = generateMethodSignature(method, inputParams)

        val paramsEncoders = params.indices
            .map { index -> inputParams[index].getValue("type").jsonPrimitive.content }
            .map(::resolveEncoderForType)

        val headPartiallyEncoded = paramsEncoders
            .zip(params)
            .map { (encoder, param) ->
                when(encoder) {
                    is StaticEncoder<*> -> EncodedPart.StaticPart(encoder.encodeUnchecked(param))
                    is DynamicEncoder<*> -> EncodedPart.DynamicPart(encoder.encodeUnchecked(param))
                }
            }

        val dynamicPartsSizes = headPartiallyEncoded
            .runningFold(initial = headPartiallyEncoded.size * PART_SIZE) { acc: Int, encodedPart: EncodedPart ->
                when(encodedPart) {
                    is EncodedPart.DynamicPart -> acc + encodedPart.encoded.size
                    is EncodedPart.StaticPart -> acc
                }
            }

        val headEncoded = headPartiallyEncoded
            .mapIndexed { index, encodedPart ->
                if(encodedPart is EncodedPart.StaticPart)
                    encodedPart.encoded
                else
                    UInt256Param.encode(dynamicPartsSizes[index].bi)
            }.fold(byteArrayOf()) { acc, part -> acc + part }

        val dynamicPartEncoded = headPartiallyEncoded
            .filterIsInstance<EncodedPart.DynamicPart>()
            .fold(byteArrayOf()) { acc, part -> acc + part.encoded }

        val data = methodSignature + headEncoded + dynamicPartEncoded

        return "0x" + data.toHex().lowercase()
    }

    private val listTypeRegex = Regex("(.*)\\[]")
    private fun resolveEncoderForType(typeAnnotation: String) = when {
        typeAnnotation.matches(listTypeRegex) -> {
            val (subtypeAnnotation) = listTypeRegex.find(typeAnnotation)!!.destructured
            ListParam(StaticEncoders.forType(subtypeAnnotation).encoder)
        }
        else -> StaticEncoders.forType(typeAnnotation).encoder
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

    companion object {
        internal const val PART_SIZE = 32
    }
}

/**
 * First iteration while encoding is an iteration when all static params encoded,
 * while dynamic params replaced with DynamicPass, later it is being replaced with encoded
 * dynamic params
 */
private sealed interface EncodedPart {
    val encoded: ByteArray
    class DynamicPart(override val encoded: ByteArray) : EncodedPart
    class StaticPart(override val encoded: ByteArray) : EncodedPart
}

/**
 * First iteration while decoding is an iteration over params head, at that moment,
 * static types are fully decoded, while for dynamic types only offset decoded
 */
private sealed interface DecodedPart {
    class PartiallyDynamic(val offset: Int) : DecodedPart
    class Fully(val value: Any) : DecodedPart
}
