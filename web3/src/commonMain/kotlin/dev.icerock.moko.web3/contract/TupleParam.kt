/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class TupleParam(param: JsonObject) : DynamicEncoder<List<Any?>> {
    private val params = param
        .getValue(key = "components")
        .jsonArray
        .map(JsonElement::jsonObject)

    override fun encode(item: List<Any?>): ByteArray =
        ABIEncoder.encodeCallData(params, item)

    override fun decode(source: ByteArray): List<Any?> =
        ABIDecoder.decodeCallData(params, source)
}
