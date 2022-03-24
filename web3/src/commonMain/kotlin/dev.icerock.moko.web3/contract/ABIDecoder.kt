/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import dev.icerock.moko.web3.contract.ABIEncoder.PART_SIZE
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.hex.internal.toHex
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.jvm.JvmName

/**
 * You can use this object for these general cases:
 * - You want to find method by name (or use already found object) and then decode using types from "outputs" field
 * - Decode callData using raw param types (List<String>)
 */
object ABIDecoder {

    fun decodeLogEvent(abis: JsonArray, event: LogEvent): List<Any?> =
        decodeLogEvent(
            abi = abis
                .map { it.jsonObject }
                .filter { it["type"]?.jsonPrimitive?.contentOrNull == "event" }
                .first { ABIEncoder.hashEventSignature(it) == event.topics[0] },
            event = event
        )

    fun decodeLogEvent(abi: JsonObject, event: LogEvent): List<Any?> {
        val topicsIterator = event.topics.drop(n = 1).iterator()
        val dataIterator = event.data.withoutPrefix
            .chunked(size = PART_SIZE * 2) { HexString(it.toString()) }
            .iterator()

        val actualData = abi["inputs"]!!
            .jsonArray
            .map { it.jsonObject }
            .map { param ->
                when (param["indexed"]?.jsonPrimitive?.boolean ?: false) {
                    true -> topicsIterator.next()
                    false -> dataIterator.next()
                }
            }
            .fold(initial = byteArrayOf()) { acc, hex ->
                acc + hex.byteArray
            }

        return decodeCallDataForObjectInputs(abi, actualData)
    }

    // --------------------- //
    // Decoding with outputs //
    // --------------------- //

    /**
     * Convert [callData] to [ByteArray]
     */
    fun decodeCallDataForOutputs(abis: JsonArray, name: String, callData: HexString): List<Any?> =
        decodeCallDataForOutputs(abis, name, callData.byteArray)

    /**
     * Search for name and then perform decoding using types from "outputs"
     */
    fun decodeCallDataForOutputs(abis: JsonArray, name: String, callData: ByteArray): List<Any?> =
        decodeCallDataForObjectOutputs(
            abi = abis.groupAbisByName().getValue(name),
            callData = callData
        )

    /**
     * This version is same as [decodeCallDataForObject], but when jsonObject was already found by name, so it's
     * not required anymore
     */
    fun decodeCallDataForObjectOutputs(abi: JsonObject, callData: ByteArray): List<Any?> =
        decodeCallDataForObjectByFieldName(abi, fieldName = "outputs", callData)

    // -------------------- //
    // Decoding with inputs //
    // -------------------- //

    /**
     * Convert [callData] to [ByteArray]
     */
    fun decodeCallDataForInputs(abis: JsonArray, name: String, callData: HexString): List<Any?> =
        decodeCallDataForInputs(abis, name, callData.byteArray)

    /**
     * Search for name and then perform decoding using types from "outputs"
     */
    fun decodeCallDataForInputs(abis: JsonArray, name: String, callData: ByteArray): List<Any?> =
        decodeCallDataForObjectInputs(
            abi = abis.groupAbisByName().getValue(name),
            callData = callData
        )

    /**
     * This version is same as [decodeCallDataForObject], but when jsonObject was already found by name, so it's
     * not required anymore
     */
    fun decodeCallDataForObjectInputs(abi: JsonObject, callData: ByteArray): List<Any?> =
        decodeCallDataForObjectByFieldName(abi, fieldName = "inputs", callData)

    // ----------------- //
    // Useful extensions //
    // ----------------- //

    private fun JsonArray.groupAbisByName() = associateBy { it.jsonObject["name"]?.jsonPrimitive?.contentOrNull }
        .mapValues { (_, value) -> value.jsonObject }

    // --------------------- //
    // General decoding case //
    // --------------------- //

    /**
     * Transforming here abi to raw types List<String>
     */
    private fun decodeCallDataForObjectByFieldName(
        abi: JsonObject,
        fieldName: String,
        callData: ByteArray
    ): List<Any?> = try {
        decodeCallData(
            paramTypes = abi.getValue(fieldName)
                .jsonArray
                .map { param ->
                    param.jsonObject
                },
            callData = callData
        )
    } catch (e: IllegalStateException) {
        error("Exception occurred while processing the result of $fieldName.\n\n${e.message}")
    }

    /**
     * Read the warning below
     */
    fun decodeCallData(callData: ByteArray, vararg params: String): List<Any?> =
        decodeCallData(params.toList(), callData)

    /**
     * Be careful while using this method!
     * It is ok to call the method like decodeCallData("string", "bytes"),
     * but some serializers need more info than "type" in json object to be specified.
     * For example tuple also requires "components" parameters to decode correctly
     */
    @OptIn(ExperimentalStdlibApi::class)
    @JvmName("decodeCallDataSimple")
    fun decodeCallData(paramTypes: List<String>, callData: ByteArray): List<Any?> =
        decodeCallData(
            // mapping to json objects like { "type": "string" }
            paramTypes = buildList {
                paramTypes.forEach { typeAnnotation ->
                    add(buildJsonObject {
                        put("type", typeAnnotation)
                    })
                }
            },
            callData = callData
        )

    fun decodeCallData(paramTypes: List<JsonObject>, callData: ByteArray): List<Any?> {
        if (callData.isEmpty())
            return listOf()

        val headSize = paramTypes.size * PART_SIZE

        require(callData.size % PART_SIZE == 0) {
            "Call data should be padded correctly!"
        }
        require(callData.size >= headSize) {
            "Call data size should be at least equals params count multiplied by 32, " +
                    "because it should be the head size." +
                    "Param types: $paramTypes; CallData: ${callData.toHex()}"
        }

        val decoders = paramTypes.map(::resolveEncoderForType)
        val parts = callData.asList().chunked(PART_SIZE).map { it.toByteArray() }
        val partsWithDecoders = decoders.zip(parts)

        val head = partsWithDecoders.map { (decoder, part) ->
            when (decoder) {
                is StaticEncoder<*> -> DecodedPart.Static(decoder.decode(part))
                // there is no reason for offset to be bigger than Int.MAX_VALUE
                // Anyway, array/list size is Int
                is DynamicEncoder<*> -> DecodedPart.Dynamic(decoder, UInt256Param.decode(part).toInt())
            }
        }

        // Here extracting call data for dynamic params with offsets
        // It may be calculated this way:
        //
        // 1. Getting `offset` of current dynamic part (bytes of dynamic param in head)
        //    This is the `start` index of current dynamic part in callData.
        //
        // 2. Taking next dynamic part `offset`, this will be our `end` border
        //    This is the `end` index of current dynamic part in callData.
        //
        //    IF THERE IS NO NEXT PART, current dynamic part is the last one, and then
        //    the `end` index is `callData.size`
        val dynamicParts = head.asSequence()
            .filterIsInstance<DecodedPart.Dynamic>()
            .windowed(size = 2, partialWindows = true)
            // Extracting data of dynamic parts
            .map { neighbourParts ->
                val currentPart = neighbourParts.first()
                val firstOffset = currentPart.offset
                // if there is no second element then we are at the end
                val secondOffset = neighbourParts.getOrNull(index = 1)?.offset ?: callData.size
                // I don't use slice, because I don't want to reallocate data
                val currentPartData = callData
                    .asList()
                    .subList(fromIndex = firstOffset, toIndex = secondOffset)
                    .toByteArray()

                return@map currentPart.decoder to currentPartData
            }
            // Decoding data
            .map { (decoder, data) -> decoder.decode(data) }
            .iterator()

        val decoded = head.map { part ->
            when (part) {
                is DecodedPart.Static -> part.value
                is DecodedPart.Dynamic -> dynamicParts.next()
            }
        }

        return decoded
    }

    private sealed interface DecodedPart {
        class Static(val value: Any?) : DecodedPart
        class Dynamic(val decoder: DynamicEncoder<*>, val offset: Int) : DecodedPart
    }
}
