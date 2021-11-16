/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.crypto.KeccakId
import dev.icerock.moko.web3.hex.internal.toHex
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object MethodEncoder {
    @Suppress("UNCHECKED_CAST")
    private fun <T> Encoder<T>.encodeUnchecked(value: Any) = encode(value as T)

    fun createCallData(abi: JsonArray, method: String, params: List<Any>): String {
        val methodAbi: JsonObject = abi.map { it.jsonObject }
            .firstOrNull { it["name"]?.jsonPrimitive?.contentOrNull == method }
            ?: throw AbiMethodNotFoundException(method, abi)

        val inputParams: List<JsonObject> =
            methodAbi.getValue(key = "inputs").jsonArray.map { it.jsonObject }

        val methodSignature: ByteArray = generateMethodSignature(method, inputParams)

        val data = methodSignature + encodeParams(inputParams, params)

        return "0x" + data.toHex().lowercase()
    }

    fun encodeParams(inputParams: List<JsonObject>, params: List<Any>): ByteArray {
        val paramsEncoders = params.indices
            .map { index ->
                val param = inputParams[index]
                val paramName = param.getValue(key = "type").jsonPrimitive.content
                return@map param to paramName
            }.map { (param, typeAnnotation) -> resolveEncoderForType(param, typeAnnotation) }

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

        return headEncoded + dynamicPartEncoded
    }

    private val listTypeRegex = Regex("(.*)\\[]")
    private fun resolveEncoderForType(param: JsonObject, typeAnnotation: String) = when {
        typeAnnotation.matches(listTypeRegex) -> {
            val (subtypeAnnotation) = listTypeRegex.find(typeAnnotation)!!.destructured
            ListParam(StaticEncoders.forType(subtypeAnnotation).encoder)
        }
        typeAnnotation == "tuple" -> TupleParam(param)
        else -> StaticEncoders.forType(typeAnnotation).encoder
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun generateMethodSignature(
        method: String,
        inputParams: List<JsonObject>
    ): ByteArray {
        val signature = "$method(${generateParamsString(inputParams)})".toByteArray()
        return KeccakId.get(signature)
    }

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
