/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.contract.internal.AbiEntityNotFoundException
import dev.icerock.moko.web3.crypto.KeccakId
import dev.icerock.moko.web3.crypto.KeccakParameter
import dev.icerock.moko.web3.crypto.digestKeccak
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.HexString
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object ABIEncoder {
    fun hashEventSignature(abi: JsonArray, event: String): Hex32String {
        val eventAbi = abi
            .map { it.jsonObject }
            .firstOrNull { it["name"]?.jsonPrimitive?.contentOrNull == event }
            ?: throw AbiEntityNotFoundException(event, abi)

        return hashEventSignature(eventAbi)
    }

    fun hashEventSignature(eventAbi: JsonObject): Hex32String {
        val params: List<JsonObject> =
            eventAbi.getValue(key = "inputs").jsonArray.map { it.jsonObject }

        val signature = generateSignature(eventAbi["name"]!!.jsonPrimitive.content, params)

        return Hex32String(signature.digestKeccak(KeccakParameter.KECCAK_256))
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> Encoder<T>.encodeUnchecked(value: Any?) = encode(value as T)

    fun encodeCallDataForMethod(abi: JsonArray, method: String, params: List<Any>): HexString {
        val methodAbi: JsonObject = abi.map { it.jsonObject }
            .firstOrNull { it["name"]?.jsonPrimitive?.contentOrNull == method }
            ?: throw AbiEntityNotFoundException(method, abi)

        val inputParams: List<JsonObject> =
            methodAbi.getValue(key = "inputs").jsonArray.map { it.jsonObject }

        val methodSignature: ByteArray = hashMethodSignature(method, inputParams)

        val data = methodSignature + encodeCallData(inputParams, params)

        return HexString(data)
    }

    fun encodeCallData(inputParams: List<JsonObject>, params: List<Any?>): ByteArray {
        // resolving encoders for every param
        val paramsEncoders = params.indices
            .map { index -> inputParams[index] }
            .map { param -> resolveEncoderForType(param) }

        // every param encoded to byte array
        val encodedParams: List<EncodedPart> = paramsEncoders
            .zip(params)
            .map { (encoder, param) ->
                when(encoder) {
                    // static encoder simply encode the value
                    is StaticEncoder<*> -> EncodedPart.StaticPart(encoder.encodeUnchecked(param))
                    // dynamic encoder encodes the value, but by the spec in head part here should be
                    // offset, but not the value itself
                    is DynamicEncoder<*> -> EncodedPart.DynamicPart(encoder.encodeUnchecked(param))
                }
            }

        // here calculating offset for every dynamic param
        // so first offset is the size of head part (same as encodedParams size)
        // and then adding one-by-one sizes of dynamic parts, so for example
        // third element will have offset equal to head size + first dynamic part size + second dynamic part size
        val dynamicPartsSizes = encodedParams
            .runningFold(initial = encodedParams.size * PART_SIZE) { acc: Int, encodedPart: EncodedPart ->
                when(encodedPart) {
                    is EncodedPart.DynamicPart -> acc + encodedPart.encoded.size
                    is EncodedPart.StaticPart -> acc
                }
            }

        val headEncoded = encodedParams
            .mapIndexed { index, encodedPart ->
                when (encodedPart) {
                    is EncodedPart.StaticPart -> encodedPart.encoded
                    is EncodedPart.DynamicPart -> UInt256Param.encode(dynamicPartsSizes[index].bi)
                }
            }.fold(byteArrayOf()) { acc, part -> acc + part }

        val dynamicPartEncoded = encodedParams
            .filterIsInstance<EncodedPart.DynamicPart>()
            .fold(byteArrayOf()) { acc, part -> acc + part.encoded }

        return headEncoded + dynamicPartEncoded
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun hashMethodSignature(
        method: String,
        inputParams: List<JsonObject>
    ): ByteArray = generateSignature(method, inputParams)
        .toByteArray()
        .let(KeccakId::get)

    fun generateSignature(name: String, params: List<JsonObject>): String =
        "$name(${generateParamsString(params)})"

    private fun generateParamsString(inputParams: List<JsonObject>): String = buildString {
        inputParams.forEachIndexed { index, param ->
            if (index != 0) append(",")
            append(stringifyType(param, param.getValue(key = "type").jsonPrimitive.content))
        }
    }

    private fun stringifyType(param: JsonObject, typeAnnotation: String) = when (typeAnnotation) {
        "tuple" -> {
            val components = param.getValue(key = "components").jsonArray.map(JsonElement::jsonObject)
            "(${generateParamsString(components)})"
        }
        else -> typeAnnotation
    }

    internal const val PART_SIZE = 32

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
}
