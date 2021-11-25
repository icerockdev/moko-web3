/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive

enum class NamedEncoders(
    val typeAnnotation: String,
    val encoder: Encoder<*>
) {
    UInt8(typeAnnotation = "uint8", encoder = UInt256Param),
    UInt256(typeAnnotation = "uint256", encoder = UInt256Param),
    Address(typeAnnotation = "address", encoder = AddressParam),
    StringBytes(typeAnnotation = "string", encoder = StringParam),
    Bytes(typeAnnotation = "bytes", encoder = BytesParam);

    companion object {
        fun forType(typeAnnotation: String) = values()
            .firstOrNull { it.typeAnnotation == typeAnnotation }
            ?: error("There is no such encoder for type $typeAnnotation")
    }
}

private val listTypeRegex = Regex("(.*)\\[]")

fun resolveEncoderForType(param: JsonObject): Encoder<*> {
    val typeAnnotation = param.getValue(key = "type").jsonPrimitive.content

    return when {
        typeAnnotation.matches(listTypeRegex) -> {
            val (subtypeAnnotation) = listTypeRegex.find(typeAnnotation)!!.destructured
            val subParam = param.mutate {
                put("type", JsonPrimitive(subtypeAnnotation))
            }
            ListParam(resolveEncoderForType(subParam))
        }
        typeAnnotation == "tuple" -> TupleParam(param)
        else -> NamedEncoders.forType(typeAnnotation).encoder
    }
}

private fun JsonObject.mutate(block: MutableMap<String, JsonElement>.() -> Unit): JsonObject =
    toMutableMap().apply(block).let(::JsonObject)
