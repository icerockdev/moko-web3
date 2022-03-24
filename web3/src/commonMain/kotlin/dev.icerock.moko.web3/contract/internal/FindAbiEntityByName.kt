/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract.internal

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


internal fun JsonArray.findAbiEntityByName(name: String): JsonObject =
    map(JsonElement::jsonObject)
        .firstOrNull { it["name"]?.jsonPrimitive?.contentOrNull == name }
        ?: throw AbiEntityNotFoundException(name, methodsAbi = this)

class AbiEntityNotFoundException(
    val name: String,
    val methodsAbi: JsonArray
) : Throwable("name $name not found in ABI $methodsAbi")
